open module org.glassfish.jersey.container.grizzly2.http {

    requires java.net.http;
    requires java.logging;

    requires org.eclipse.jetty.http2.client;
    requires org.eclipse.jetty.http2.http.client.transport;

    requires org.bouncycastle.provider;
    requires org.bouncycastle.pkix;
    requires org.eclipse.jetty.client;

    requires grizzly.http.server;
    requires grizzly.http2;
    requires grizzly.framework;

    requires org.glassfish.hk2.api;
    requires org.glassfish.hk2.locator;

    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.inject.hk2;

    exports org.glassfish.jersey.grizzly2.httpserver.test.application
            to  org.glassfish.jersey.core.server;
}