module org.glassfish.jersey.ext.cdi1x {

    requires java.naming;
    requires java.logging;

    requires transitive org.glassfish.jersey.core.server;
    requires transitive org.glassfish.jersey.core.common;
    requires transitive org.glassfish.jersey.inject.hk2;

    exports org.glassfish.jersey.ext.cdi1x.internal;
    opens org.glassfish.jersey.ext.cdi1x.internal;

}