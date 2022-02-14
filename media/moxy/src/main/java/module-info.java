module org.glassfish.jersey.media.moxy {
    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.annotation;
    requires jakarta.inject;

    requires static jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.ext.entity.filtering;

    requires org.eclipse.persistence.core;
    requires org.eclipse.persistence.moxy;

    exports org.glassfish.jersey.moxy.xml;
    exports org.glassfish.jersey.moxy.json;
}