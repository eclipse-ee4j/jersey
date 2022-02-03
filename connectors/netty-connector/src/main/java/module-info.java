module org.glassfish.jersey.netty.connector {
    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.inject;

    requires io.netty.all;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;

    exports org.glassfish.jersey.netty.connector;
    exports org.glassfish.jersey.netty.connector.internal;
//    opens org.glassfish.jersey.netty.connector;
}