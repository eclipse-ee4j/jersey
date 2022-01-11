module org.glassfish.jersey.apache.connector {
    requires java.logging;

    requires jakarta.ws.rs;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;

    exports org.glassfish.jersey.apache.connector;

    opens org.glassfish.jersey.apache.connector;
}