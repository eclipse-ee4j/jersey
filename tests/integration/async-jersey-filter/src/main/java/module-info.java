module org.glassfish.jersey.tests.integration.async.jersey.filter {
    requires jakarta.inject;
    requires jakarta.persistence;
    requires jakarta.servlet;
    requires jakarta.ws.rs;

    requires jakarta.xml.bind;

    requires java.logging;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.container.servlet.core;

    exports org.glassfish.jersey.tests.integration.async;
    exports org.glassfish.jersey.tests.integration.jersey2730;
    exports org.glassfish.jersey.tests.integration.jersey2812;

    opens org.glassfish.jersey.tests.integration.async;
    opens org.glassfish.jersey.tests.integration.jersey2812;
    opens org.glassfish.jersey.tests.integration.jersey2730;
}