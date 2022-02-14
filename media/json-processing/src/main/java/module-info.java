module org.glassfish.jersey.media.json.processing {
    requires jakarta.annotation;
    requires jakarta.ws.rs;

    requires org.glassfish.jersey.core.common;

    exports org.glassfish.jersey.jsonp;
    exports org.glassfish.jersey.jsonp.internal;
}