module org.glassfish.jersey.container.simple.http {
    requires java.logging;

    requires jakarta.inject;
    requires jakarta.ws.rs;

    requires static jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.simple;
    opens org.glassfish.jersey.simple;
}