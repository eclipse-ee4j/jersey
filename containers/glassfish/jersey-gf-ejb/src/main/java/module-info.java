module org.glasfish.jersey.gf.ejb {
    requires java.logging;
    requires java.naming;

    requires jakarta.ws.rs;
    requires static jakarta.activation;
    requires jakarta.annotation;
    requires jakarta.ejb.api;
    requires jakarta.inject;
    requires jakarta.interceptor.api;

    requires ejb.container;
    requires internal.api;
    requires config.api;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.ext.cdi1x;
    requires org.glassfish.jersey.inject.hk2;


    exports org.glassfish.jersey.gf.ejb.internal;
    opens org.glassfish.jersey.gf.ejb.internal;
}