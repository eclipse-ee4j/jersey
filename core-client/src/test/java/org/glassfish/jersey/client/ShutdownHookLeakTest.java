/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Collection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;

/**
 * Reproducer for JERSEY-2786.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ShutdownHookLeakTest {

    private static final int ITERATIONS = 4000;
    private static final int THRESHOLD = ITERATIONS * 2 / 3;

    @SuppressWarnings("unchecked")
    @Test
    public void testShutdownHookDoesNotLeak() throws Exception {
        final Client client = ClientBuilder.newClient();
        final WebTarget target = client.target("http://example.com");

        final Collection shutdownHooks = getShutdownHooks(client);

        for (int i = 0; i < ITERATIONS; i++) {
            // Create/Initialize client runtime.
            target.property("Washington", "Irving")
                    .request()
                    .property("how", "now")
                    .buildGet()
                    .property("Irving", "Washington");
        }

        System.gc();

        int notEnqueued = 0;
        int notNull = 0;
        for (final Object o : shutdownHooks) {
            if (((WeakReference<JerseyClient.ShutdownHook>) o).get() != null) {
                notNull++;
            }
            if (!((WeakReference<JerseyClient.ShutdownHook>) o).isEnqueued()) {
                notEnqueued++;
            }
        }

        assertThat(
                "Non-null shutdown hook references count should not copy number of property invocation",
                // 66 % seems like a reasonable threshold for this test to keep it stable
                notNull, is(lessThan(THRESHOLD)));

        assertThat(
                "Shutdown hook references count not enqueued in the ReferenceQueue should not copy number of property invocation",
                // 66 % seems like a reasonable threshold for this test to keep it stable
                notEnqueued, is(lessThan(THRESHOLD)));
    }

    private Collection getShutdownHooks(final Client client) throws NoSuchFieldException, IllegalAccessException {
        final JerseyClient jerseyClient = (JerseyClient) client;
        final Field shutdownHooksField = JerseyClient.class.getDeclaredField("shutdownHooks");
        shutdownHooksField.setAccessible(true);
        return (Collection) shutdownHooksField.get(jerseyClient);
    }
}
