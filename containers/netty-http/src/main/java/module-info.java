module org.glassfish.jersey.container.netty.servlet {
    requires java.logging;
    requires jakarta.ws.rs;
    requires io.netty.all;

    requires static jakarta.xml.bind;

    requires org.glassfish.jersey.netty.connector;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.core.common;

    exports org.glassfish.jersey.netty.httpserver;
    opens org.glassfish.jersey.netty.httpserver;
}