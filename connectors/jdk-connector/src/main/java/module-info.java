module org.glassfish.jersey.jdk.connector {
    requires java.logging;

    requires jakarta.ws.rs;
    requires static jakarta.activation;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;

    exports org.glassfish.jersey.jdk.connector;
}