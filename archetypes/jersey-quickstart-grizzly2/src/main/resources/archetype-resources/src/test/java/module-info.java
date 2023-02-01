module ${package}.module.test {
    requires jakarta.ws.rs;

    requires org.junit.jupiter.api;

    requires com.example.archetype.grizzly.module;

    exports ${package}.test;
}