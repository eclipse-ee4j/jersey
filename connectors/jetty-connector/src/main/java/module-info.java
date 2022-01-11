module org.glassfish.jersey.jetty.connector {
    requires java.logging;

    requires jakarta.ws.rs;

    requires org.eclipse.jetty.client;
    requires org.eclipse.jetty.http;
    requires org.eclipse.jetty.io;
    requires org.eclipse.jetty.util;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;

    exports org.glassfish.jersey.jetty.connector;
    opens org.glassfish.jersey.jetty.connector;
}