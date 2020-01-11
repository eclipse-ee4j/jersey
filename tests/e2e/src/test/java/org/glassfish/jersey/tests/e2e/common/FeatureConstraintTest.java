/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.e2e.common;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.Uri;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.concurrent.atomic.AtomicInteger;

public class FeatureConstraintTest extends JerseyTest {

    public static AtomicInteger clientEnvironmentHitCount = new AtomicInteger(0);
    public static AtomicInteger serverEnvironmentHitCount = new AtomicInteger(0);
    public static AtomicInteger clientServerEnvironmentHitCount = new AtomicInteger(0);

    @Override
    protected Application configure() {
        return new ResourceConfig(PropagatedConfigResource.class, ServerConstrainedClassFeature.class,
                ClientConstrainedClassFeature.class, ClientServerConstrainedClassFeature.class)
                .register(new ServerConstrainedInstanceFeature())
                .register(new ClientConstrainedInstanceFeature())
                .register(new ClientServerConstrainedInstanceFeature());
    }

    @ConstrainedTo(RuntimeType.SERVER)
    public static class ServerConstrainedClassFeature implements Feature {
        protected int increment = 10;
        @Override
        public boolean configure(FeatureContext context) {
            if (context.getConfiguration().getRuntimeType().equals(RuntimeType.CLIENT)) {
                clientEnvironmentHitCount.addAndGet(increment);
            }
            return true;
        }
    }

    @ConstrainedTo(RuntimeType.SERVER)
    public static class ServerConstrainedInstanceFeature extends ServerConstrainedClassFeature {
        {
            increment = 100;
        }
    }

    @ConstrainedTo(RuntimeType.CLIENT)
    public static class ClientConstrainedClassFeature implements Feature {
        protected int increment = 10;
        @Override
        public boolean configure(FeatureContext context) {
            if (context.getConfiguration().getRuntimeType().equals(RuntimeType.SERVER)) {
                serverEnvironmentHitCount.addAndGet(increment);
            }
            return true;
        }
    }

    @ConstrainedTo(RuntimeType.CLIENT)
    public static class ClientConstrainedInstanceFeature extends ClientConstrainedClassFeature {
        {
            increment = 100;
        }
    }

    public static class ClientServerConstrainedClassFeature implements Feature {
        protected int increment = 10;
        @Override
        public boolean configure(FeatureContext context) {
            if (context.getConfiguration().getRuntimeType().equals(RuntimeType.SERVER)) {
                clientServerEnvironmentHitCount.addAndGet(increment);
            }
            if (context.getConfiguration().getRuntimeType().equals(RuntimeType.CLIENT)) {
                clientServerEnvironmentHitCount.addAndGet(100 * increment);
            }
            return true;
        }
    }

    public static class ClientServerConstrainedInstanceFeature extends ClientServerConstrainedClassFeature {
        {
            increment = 100;
        }
    }

    @Path("/")
    public static class PropagatedConfigResource {
        @Uri("/isRegistered")
        WebTarget target;

        @Path("isRegistered")
        @GET
        public boolean isRegisteredOnServer(@Context Configuration config) {
            return config.isRegistered(ServerConstrainedClassFeature.class)
                    && config.isRegistered(ServerConstrainedInstanceFeature.class)
                    && config.isRegistered(ClientConstrainedClassFeature.class)
                    && config.isRegistered(ClientConstrainedInstanceFeature.class)
                    && config.isRegistered(ClientServerConstrainedClassFeature.class)
                    && config.isRegistered(ClientServerConstrainedInstanceFeature.class);
        }

        @Path("isInherited")
        @GET
        public boolean isInheritedInInjectedClientConfig() {
            final Configuration config = target.getConfiguration();
            return isRegisteredOnServer(config);
        }

        @Path("featureConfigurationNotInvoked")
        @GET
        public boolean featureConfigurationNotInvoked() {
            return target
                    .register(ServerConstrainedClassFeature.class)
                    .register(new ServerConstrainedInstanceFeature())
                    .register(ClientConstrainedClassFeature.class)
                    .register(new ClientConstrainedInstanceFeature())
                    .register(ClientServerConstrainedClassFeature.class)
                    .register(new ClientServerConstrainedInstanceFeature())
                    .request().get().readEntity(boolean.class);
        }
    }

    @Test
    public void test() {
        assertThat("*Constrained*Feature must be registered in a server configuration",
                target("isRegistered")
                        .register(ServerConstrainedClassFeature.class)
                        .register(new ServerConstrainedInstanceFeature())
                        .register(ClientConstrainedClassFeature.class)
                        .register(new ClientConstrainedInstanceFeature())
                        .register(ClientServerConstrainedClassFeature.class)
                        .register(new ClientServerConstrainedInstanceFeature())
                        .request().get().readEntity(boolean.class),
                is(true));

        assertThat("Server Features should not have been configured on Client", clientEnvironmentHitCount.get(), is(0));
        assertThat("Client Features should not have been configured on Server", serverEnvironmentHitCount.get(), is(0));
        assertThat("ClientSever Features should have been configured", clientServerEnvironmentHitCount.get(), is(11110));
        clientServerEnvironmentHitCount.set(0); //reset configuration invoked on a server, it won't happen again

        assertThat("*Constrained*Feature must be in an application classes set",
                target("isInherited").request().get().readEntity(boolean.class),
                is(true));

        assertThat("Server Features should not have been configured on Client", clientEnvironmentHitCount.get(), is(0));
        assertThat("Client Features should not have been configured on Server", serverEnvironmentHitCount.get(), is(0));
        assertThat("ClientSever Features should not have been configured", clientServerEnvironmentHitCount.get(), is(0));

        assertThat("ServerConstrainedFeature must be in an application classes set",
                target("featureConfigurationNotInvoked")
                        .register(ServerConstrainedClassFeature.class)
                        .register(new ServerConstrainedInstanceFeature())
                        .register(ClientConstrainedClassFeature.class)
                        .register(new ClientConstrainedInstanceFeature())
                        .register(ClientServerConstrainedClassFeature.class)
                        .register(new ClientServerConstrainedInstanceFeature())
                        .request().get().readEntity(boolean.class),
                is(true));

        assertThat("Server Features should not have been configured on Client", clientEnvironmentHitCount.get(), is(0));
        assertThat("Client Features should not have been configured on Server", serverEnvironmentHitCount.get(), is(0));
        assertThat("ClientSever Features should have been configured", clientServerEnvironmentHitCount.get(), is(22000));
    }


}
