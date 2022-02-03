module org.glassfish.jersey.container.grizzly2.servlet {
    requires jakarta.ws.rs;
    requires transitive jakarta.servlet;

    requires grizzly.http.servlet;
    requires grizzly.http.server;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.container.servlet.core;
    requires org.glassfish.jersey.container.grizzly2.http;

    exports org.glassfish.jersey.grizzly2.servlet;
}