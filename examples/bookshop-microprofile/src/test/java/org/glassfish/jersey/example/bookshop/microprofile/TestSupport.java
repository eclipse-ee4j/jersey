package org.glassfish.jersey.example.bookshop.microprofile;

import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import javax.ws.rs.core.Application;

public abstract class TestSupport extends JerseyTest {

    @Override
    protected Application configure() {
        final App application = new App();
        application.property(ServletProperties.FILTER_FORWARD_ON_404, true);
        set(TestProperties.CONTAINER_PORT, 8080);
        return application;
    }

}
