module ${package}.module {
    requires jakarta.ws.rs;

    requires grizzly.http.server;

    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.container.grizzly2.http;

    exports ${package};
}