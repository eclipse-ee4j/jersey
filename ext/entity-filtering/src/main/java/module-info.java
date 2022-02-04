module org.glassfish.jersey.ext.entity.filtering {
    requires java.logging;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.ws.rs;
    requires jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.message.filtering;
    exports org.glassfish.jersey.message.filtering.spi;

}