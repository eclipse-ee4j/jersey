import org.glassfish.jersey.media.sse.internal.JerseySseEventSource;

module org.glassfish.jersey.media.sse {
    requires java.logging;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.ws.rs;

    requires jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.media.sse;
    exports org.glassfish.jersey.media.sse.internal;

    opens org.glassfish.jersey.media.sse;
    opens org.glassfish.jersey.media.sse.internal;

    provides jakarta.ws.rs.sse.SseEventSource.Builder with
            JerseySseEventSource.Builder;
}