/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;

/**
 * Reproducer for JERSEY-1747.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class TimerTest extends CdiTest {

    @Test
    public void testGet() {

        final WebTarget target = target().path("jcdibean/dependent/timer");

        final Response firstResponse = target.request().get();
        assertThat(firstResponse.getStatus(), is(200));
        long firstMillis = Long.decode(firstResponse.readEntity(String.class));
        sleep(2);

        final Response secondResponse = target.request().get();
        assertThat(secondResponse.getStatus(), is(200));
        long secondMillis = Long.decode(secondResponse.readEntity(String.class));

        assertTrue("Second request should have greater millis!", secondMillis > firstMillis);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(TimerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
