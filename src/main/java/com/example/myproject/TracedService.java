package com.example.myproject;

import io.helidon.tracing.Span;
import io.helidon.tracing.Tracer;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.http1.Http1Route;
import io.helidon.webserver.http2.Http2Route;

import static io.helidon.http.Method.GET;

/**
 * An {@link HttpService} that uses tracing.
 */
public class TracedService implements HttpService {
    @Override
    public void routing(HttpRules httpRules) {
        httpRules.route(Http1Route.route(GET, "/", new TracedHandler("HTTP/1.1 route")))
                .route(Http2Route.route(GET, "/", new TracedHandler("HTTP/2 route")));
    }

    private record TracedHandler(String message) implements Handler {

        @Override
        public void handle(ServerRequest req, ServerResponse res) {
            Tracer tracer = req.context().get(Tracer.class)
                    .orElseGet(Tracer::global);
            Span span = tracer.spanBuilder("custom-span")
                    .start();
            try {
                span.addEvent("my nice log");
                res.send(message);
            } finally {
                span.end();
            }
        }
    }
}
