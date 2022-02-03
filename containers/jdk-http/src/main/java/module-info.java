module org.glassfish.jersey.container.jdk.http {
    requires java.logging;

    requires jdk.httpserver;

    requires jakarta.ws.rs;
    requires static jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.jdkhttp;
    opens org.glassfish.jersey.jdkhttp;
    opens org.glassfish.jersey.jdkhttp.internal;
}