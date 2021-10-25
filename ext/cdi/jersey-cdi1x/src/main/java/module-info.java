module org.glassfish.jersey.ext.cdi1x {

    requires java.naming;

    requires transitive org.glassfish.jersey.core.server;
    requires transitive org.glassfish.jersey.inject.hk2;

}