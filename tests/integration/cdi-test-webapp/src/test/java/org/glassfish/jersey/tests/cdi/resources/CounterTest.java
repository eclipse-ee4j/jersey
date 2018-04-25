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

package org.glassfish.jersey.tests.cdi.resources;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;

/**
 * Part of JERSEY-2641 reproducer. Accessing CDI bean that has custom CDI
 * extension injected.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class CounterTest extends CdiTest {

    @Test
    public void testGet() {

        final WebTarget target = target().path("counter");

        final Response firstResponse = target.request().get();
        assertThat(firstResponse.getStatus(), is(200));
        int firstNumber = Integer.decode(firstResponse.readEntity(String.class));

        final Response secondResponse = target.request().get();
        assertThat(secondResponse.getStatus(), is(200));
        int secondNumber = Integer.decode(secondResponse.readEntity(String.class));

        assertTrue("Second request should have greater number!", secondNumber > firstNumber);
    }
}
