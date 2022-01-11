module org.glassfish.jersey.grizzly.connector {
    requires java.logging;

    requires jakarta.ws.rs;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;

    exports org.glassfish.jersey.grizzly.connector;
    opens org.glassfish.jersey.grizzly.connector;
}