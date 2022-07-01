module ${package}.module {
    requires jakarta.ws.rs;

    requires org.glassfish.jersey.container.servlet.core;
    requires org.glassfish.jersey.inject.hk2;

    exports ${package};
}