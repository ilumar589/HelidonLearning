
module com.example.myproject {
    requires io.helidon.webserver;
    requires io.helidon.http;
    requires io.helidon.config;
    requires io.helidon.http.media.multipart;
    requires io.helidon.webserver.staticcontent;
    requires jakarta.json;
    requires io.helidon.webclient;
    requires io.helidon.webserver.cors;
    requires io.helidon.cors;
    requires java.logging;
    requires io.helidon.faulttolerance;
    requires io.helidon.webserver.observe.metrics;
    requires io.helidon.metrics.api;
    requires io.helidon.webserver.observe.health;
    requires io.helidon.health.checks;
    requires io.helidon.webserver.http2;
    requires io.helidon.webserver.observe.tracing;
    requires io.helidon.tracing;
    requires io.helidon.webserver.observe;
    requires io.helidon.dbclient;
    requires io.helidon.dbclient.metrics;
    requires io.helidon.dbclient.tracing;
    requires io.helidon.logging.common;
    requires io.helidon.dbclient.health;
    requires rocker.runtime;

    exports com.example.myproject;

    opens WEB;
}