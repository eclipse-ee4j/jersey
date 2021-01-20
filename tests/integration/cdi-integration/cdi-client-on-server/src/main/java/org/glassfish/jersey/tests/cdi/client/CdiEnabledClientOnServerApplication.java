package org.glassfish.jersey.tests.cdi.client;

import org.glassfish.jersey.server.ResourceConfig;

class CdiEnabledClientOnServerApplication extends ResourceConfig {
    CdiEnabledClientOnServerApplication() {
        register(CdiEnabledClientOnServerResource.class);
    }
}
