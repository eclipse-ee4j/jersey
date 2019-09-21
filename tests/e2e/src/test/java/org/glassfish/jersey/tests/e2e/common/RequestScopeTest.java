/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import javax.inject.Inject;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.DisposableSupplier;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * E2E Request Scope Tests.
 *
 * @author Michal Gajdos
 */
@Ignore("Test Supplier Injection -> this test require dispose() method from Factory")
public class RequestScopeTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(RemoveResource.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bindFactory(CloseMeFactory.class, RequestScoped.class).to(CloseMe.class).in(RequestScoped.class);
                    }
                });
    }

    public interface CloseMe {

        String eval();

        void close();
    }

    public static class CloseMeFactory implements DisposableSupplier<CloseMe> {

        private static final CountDownLatch CLOSED_LATCH = new CountDownLatch(1);

        @Override
        public CloseMe get() {
            return new CloseMe() {
                @Override
                public String eval() {
                    return "foo";
                }

                @Override
                public void close() {
                    CLOSED_LATCH.countDown();
                }
            };
        }

        @Override
        public void dispose(final CloseMe instance) {
            instance.close();
        }
    }

    @Path("remove")
    public static class RemoveResource {

        private CloseMe closeMe;

        @Inject
        public RemoveResource(final CloseMe closeMe) {
            this.closeMe = closeMe;
        }

        @GET
        public String get() {
            return closeMe.eval();
        }
    }

    /**
     * Test that Factory.dispose method is called during release of Request Scope.
     */
    @Test
    public void testRemove() throws Exception {
        final Response response = target().path("remove").request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("foo"));

        assertTrue(CloseMeFactory.CLOSED_LATCH.await(3, TimeUnit.SECONDS));
    }
}
