module org.glassfish.jersey.media.json.jettison {
    requires java.logging;
    requires java.xml;

    requires jakarta.ws.rs;
    requires static jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.media.jaxb;

    exports org.glassfish.jersey.jettison;
}