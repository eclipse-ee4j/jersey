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

package org.glassfish.jersey.tests.e2e.entity;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the correct order of providers.
 *
 * @author Paul Sandoz
 */
public class ParameterTypeArgumentOrderTest extends AbstractParameterTypeArgumentOrderTest {
    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(AWriter.class, BWriter.class, CWriter.class, ClassResource.class);
    }

    @Test
    public void testClassResource() {
        assertEquals("AA", target().path("a").request().get(String.class));
        assertEquals("BB", target().path("b").request().get(String.class));
        assertEquals("CC", target().path("c").request().get(String.class));
    }

}
