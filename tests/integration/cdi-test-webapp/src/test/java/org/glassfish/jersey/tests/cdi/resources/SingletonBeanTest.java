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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.test.external.ExternalTestContainerFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Test for the application scoped resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RunWith(Parameterized.class)
public class SingletonBeanTest extends CdiTest {

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"alpha", "beta"},
                {"1", "2"}
        });
    }

    final String p, x;

    /**
     * Construct instance with the above test data injected.
     *
     * @param p path parameter.
     * @param x query parameter.
     */
    public SingletonBeanTest(String p, String x) {
        this.p = p;
        this.x = x;
    }

    @Test
    public void testGet() {
        final WebTarget singleton = target().path("jcdibean/singleton").path(p).queryParam("x", x);
        String s = singleton.request().get(String.class);
        assertThat(s, containsString(singleton.getUri().toString()));
        assertThat(s, containsString(String.format("p=%s", p)));
        assertThat(s, containsString(String.format("queryParam=%s", x)));
    }

    @Test
    public void testCounter() {

        final WebTarget counter = target().path("jcdibean/singleton").path(p).queryParam("x", x).path("counter");

        if (!ExternalTestContainerFactory.class.isAssignableFrom(getTestContainerFactory().getClass())) {
             // TODO: remove this workaround once JERSEY-2744 is resolved
            counter.request().put(Entity.text("10"));
        }

        String c10 = counter.request().get(String.class);
        assertThat(c10, containsString("10"));

        String c11 = counter.request().get(String.class);
        assertThat(c11, containsString("11"));

        counter.request().put(Entity.text("32"));

        String c32 = counter.request().get(String.class);
        assertThat(c32, containsString("32"));

        counter.request().put(Entity.text("10"));
    }

    @Test
    public void testException() {
        final WebTarget exception = target().path("jcdibean/singleton").path(p).queryParam("x", x).path("exception");
        assertThat(exception.request().get().readEntity(String.class), containsString("JDCIBeanException"));
    }
}
