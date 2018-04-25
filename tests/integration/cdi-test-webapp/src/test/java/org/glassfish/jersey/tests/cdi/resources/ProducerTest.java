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
import static org.hamcrest.CoreMatchers.is;

/**
 * Check that automatic HK2 bindings do not break CDI producer mechanism.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ProducerTest extends CdiTest {

    @Test
    public void testGet() {

        final WebTarget target = target().path("producer");

        final Response fieldResponse = target.path("f").request().get();
        assertThat(fieldResponse.getStatus(), is(200));
        assertThat(fieldResponse.readEntity(String.class), is("field"));

        final Response methodResponse = target.path("m").request().get();
        assertThat(methodResponse.getStatus(), is(200));
        assertThat(methodResponse.readEntity(String.class), is("method"));
    }
}
