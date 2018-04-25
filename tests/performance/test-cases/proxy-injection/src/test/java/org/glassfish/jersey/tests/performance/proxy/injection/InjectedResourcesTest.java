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

package org.glassfish.jersey.tests.performance.proxy.injection;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test for field/method injected resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class InjectedResourcesTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new JaxRsApplication();
    }

    @Test
    public void testSameTypesInjected() {
        final String methodInjectedResponse = target().path("method-injected").path("all-parameters").request().get(String.class);
        final String fieldInjectedResponse = target().path("field-injected").path("without-parameters").request()
                .get(String.class);
        assertEquals(methodInjectedResponse, fieldInjectedResponse);
    }
}
