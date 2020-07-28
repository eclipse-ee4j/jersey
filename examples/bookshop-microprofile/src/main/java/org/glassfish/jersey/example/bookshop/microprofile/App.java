package org.glassfish.jersey.example.bookshop.microprofile;

import org.glassfish.jersey.server.ResourceConfig;

public class App extends ResourceConfig {

    public App() {
        // Resources.
        packages(App.class.getPackage().getName());
    }
}
