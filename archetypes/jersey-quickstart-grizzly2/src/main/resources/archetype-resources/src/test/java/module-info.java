module ${package}.module.test {
    requires jakarta.ws.rs;

    requires junit;

    requires com.example.archetype.grizzly.module;

    exports ${package}.test;
}