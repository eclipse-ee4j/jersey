module org.glassfish.jersey.container.servlet.core {
    requires java.logging;
    requires java.naming;

    requires jakarta.ws.rs;
    requires jakarta.inject;
    requires jakarta.persistence;
    requires static jakarta.servlet;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.servlet;
    exports org.glassfish.jersey.servlet.internal;
    exports org.glassfish.jersey.servlet.internal.spi;
    exports org.glassfish.jersey.servlet.spi;
}