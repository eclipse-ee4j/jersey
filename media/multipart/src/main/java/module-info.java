module org.glassfish.jersey.media.multipart {

    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.inject;

    requires jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.core.client;

    exports org.glassfish.jersey.media.multipart;
    exports org.glassfish.jersey.media.multipart.internal;

    opens org.glassfish.jersey.media.multipart.internal;
}