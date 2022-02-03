module org.glassfish.jersey.container.servlet {
    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.servlet;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.container.servlet.core;

    exports org.glassfish.jersey.servlet.async;
    exports org.glassfish.jersey.servlet.init;
}