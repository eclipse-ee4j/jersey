module org.glassfish.jersey.media.jsonb {
    requires jakarta.annotation;
    requires jakarta.ws.rs;
    requires jakarta.json.bind;

    requires org.glassfish.jersey.core.common;

    exports org.glassfish.jersey.jsonb;

}