
package com.example.myproject;

import java.util.logging.Logger;

import com.example.myproject.components.pokemon.PokemonListService;
import io.helidon.logging.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.http.Status;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.webserver.staticcontent.StaticContentService;
import io.helidon.cors.CrossOriginConfig;
import io.helidon.webserver.cors.CorsSupport;
import io.helidon.faulttolerance.BulkheadException;
import io.helidon.faulttolerance.CircuitBreakerOpenException;
import io.helidon.faulttolerance.TimeoutException;
import io.helidon.webserver.observe.ObserveFeature;
import io.helidon.webserver.observe.health.HealthObserver;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.common.context.Contexts;
import io.helidon.dbclient.DbClient;




/**
 * The application main class.
 */
public class Main {


    /**
     * Cannot be instantiated.
     */
    private Main() {
    }


    /**
     * Application main entry point.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        
        // load logging configuration
        LogConfig.configureRuntime();

        // initialize global config from default configuration
        Config config = Config.create();
        Config.global(config);

        
        DbClient dbClient = DbClient.create(config.get("db"));
        Contexts.globalContext().register(dbClient);

        ObserveFeature observe = ObserveFeature.builder()
        .addObserver(HealthObserver.builder()
                             .addCheck(DbClientHealthCheck.create(dbClient, config.get("db.health-check")))
                             .build())
        .build();

        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .addFeature(observe)
                .routing(Main::routing)
                .build()
                .start();


        System.out.println("WEB server is up! http://localhost:" + server.port() + "/simple-greet");

    }


    /**
     * Updates HTTP Routing.
     */
    static void routing(HttpRouting.Builder routing) {
        routing
               .register("/greet", new GreetService())
                       .register("/cors-greet", corsSupportForGreeting(), new GreetService())

               .register("/", new FtService())
                .error(BulkheadException.class,
                        (req, res, ex) -> res.status(Status.SERVICE_UNAVAILABLE_503).send("bulkhead"))
                .error(CircuitBreakerOpenException.class,
                        (req, res, ex) -> res.status(Status.SERVICE_UNAVAILABLE_503).send("circuit breaker"))
                .error(TimeoutException.class,
                        (req, res, ex) -> res.status(Status.REQUEST_TIMEOUT_408).send("timeout"))
                .error(Throwable.class,
                        (req, res, ex) -> res.status(Status.INTERNAL_SERVER_ERROR_500)
                                .send(ex.getClass().getName() + ": " + ex.getMessage()))
               .register("/traced", new TracedService())
               .register("/db", new PokemonService())
               .get("/simple-greet", (req, res) -> res.send("Hello World!"))
               .any("/", (req, res) -> {
                    res.status(Status.MOVED_PERMANENTLY_301);
                    res.header(HeaderValues.createCached(HeaderNames.LOCATION, "/ui"));
                    res.send();
                })
                .register("/ui", StaticContentService.builder("WEB")
                        .welcomeFileName("index.html")
                        .build())
                .register("/api", new FileService())

                // this is where the experimentation starts based on the examples
                .register("/poketest", new PokemonListService());
    }

    private static CorsSupport corsSupportForGreeting() {
        Config restrictiveConfig = Config.global().get("restrictive-cors");
        if (!restrictiveConfig.exists()) {
            Logger.getLogger(Main.class.getName())
                    .warning("Missing restrictive config; continuing with default CORS support");
        }

        CorsSupport.Builder corsBuilder = CorsSupport.builder();

        Config.global().get("cors")
                .ifExists(c -> {
                    Logger.getLogger(Main.class.getName()).info("Using the override configuration");
                    corsBuilder.mappedConfig(c);
                });
        corsBuilder
                .config(restrictiveConfig) // restricted sharing for PUT, DELETE
                .addCrossOrigin(CrossOriginConfig.create()) // open sharing for other methods
                .build();

        return corsBuilder.build();
    }

}