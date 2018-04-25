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

import javax.inject.Qualifier;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * Test CDI bean injected using a {@link Qualifier}
 * is setup via JAX-RS interface first.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class QualifiedInjectionSetGetTest extends CdiTest {

    @Test
    public void testSetGet() {

        final WebTarget factorTarget = target().path("stutter-service-factor");
        final WebTarget stutterTarget = target().path("stutter");

        factorTarget.request().put(Entity.text("3"));
        final String shouldBeTrippled = stutterTarget.queryParam("s", "lincoln").request().get(String.class);

        assertThat(shouldBeTrippled, is("lincolnlincolnlincoln"));

        factorTarget.request().put(Entity.text("2"));
        final String shouldBeDoubled = stutterTarget.queryParam("s", "lincoln").request().get(String.class);

        assertThat(shouldBeDoubled, is("lincolnlincoln"));
    }
}
