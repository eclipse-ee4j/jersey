module org.glassfish.jersey.container.grizzly2.http {
    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.inject;
    requires static jakarta.xml.bind;

    requires grizzly.framework;
    requires grizzly.http.server;
    requires grizzly.http;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.grizzly2.httpserver;
    opens org.glassfish.jersey.grizzly2.httpserver;
}