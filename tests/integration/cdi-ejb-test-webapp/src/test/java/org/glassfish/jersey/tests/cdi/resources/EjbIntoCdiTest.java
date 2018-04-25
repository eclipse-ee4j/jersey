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

import javax.ws.rs.client.WebTarget;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Test EJB timers injected into CDI beans.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class EjbIntoCdiTest extends TestBase {

    @Test
    public void testInjection() {

        final WebTarget cdiResource = target().path("request-scoped");
        final WebTarget ejbInjectedTimer = cdiResource.path("ejb-injected-timer");
        final WebTarget jsr330InjectedTimer = cdiResource.path("jsr330-injected-timer");

        String firstResource = cdiResource.request().get(String.class);
        long firstMillis = _getMillis(ejbInjectedTimer);
        sleep(2);
        String secondResource = cdiResource.request().get(String.class);
        long secondMillis = _getMillis(jsr330InjectedTimer);

        assertThat(firstMillis, equalTo(secondMillis));
        assertThat(firstResource, not(equalTo(secondResource)));
    }
}
