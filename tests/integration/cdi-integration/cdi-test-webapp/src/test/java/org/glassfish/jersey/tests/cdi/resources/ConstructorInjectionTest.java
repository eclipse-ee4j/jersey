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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Part of JERSEY-2526 reproducer. Without the fix, the application would
 * not deploy at all. This is just to make sure the JAX-RS parameter producer
 * keeps working as expected without regressions.
 *
 * @author Jakub Podlesak (jakub.podlesak at oralc.com)
 */
public class ConstructorInjectionTest extends CdiTest {

    @Test
    public void testConstructorInjectedResource() {

        final WebTarget target = target().path("ctor-injected");

        final Response pathParamResponse = target.path("pathParam").request().get();
        assertThat(pathParamResponse.getStatus(), is(200));
        assertThat(pathParamResponse.readEntity(String.class), is("pathParam"));

        final Response queryParamResponse = target.path("queryParam").queryParam("q", "123").request().get();
        assertThat(queryParamResponse.getStatus(), is(200));
        assertThat(queryParamResponse.readEntity(String.class), is("123"));

        final Response matrixParamResponse = target.path("matrixParam").matrixParam("m", "456").request().get();
        assertThat(matrixParamResponse.getStatus(), is(200));
        assertThat(matrixParamResponse.readEntity(String.class), is("456"));

        final Response headerParamResponse = target.path("headerParam").request().header("Custom-Header", "789").get();
        assertThat(headerParamResponse.getStatus(), is(200));
        assertThat(headerParamResponse.readEntity(String.class), is("789"));

        final Response cdiParamResponse = target.path("cdiParam").request().get();
        assertThat(cdiParamResponse.getStatus(), is(200));
        assertThat(cdiParamResponse.readEntity(String.class), is("cdi-produced"));
    }
}
