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

package org.glassfish.jersey.tests.e2e.container;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class MatrixParamTest extends JerseyContainerTest {

    @Path("/")
    public static class Resource {

        @GET
        public String get(@MatrixParam("x") final String x, @MatrixParam("y") final String y) {
            return y;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test
    public void testMatrixParam() {
        assertThat(target().matrixParam("y", "1").request().get(String.class), is("1"));
        assertThat(target().matrixParam("x", "1").matrixParam("y", "1%20%2B%202").request().get(String.class), is("1 + 2"));
        assertThat(target().matrixParam("x", "1").matrixParam("y", "1%20%26%202").request().get(String.class), is("1 & 2"));
        assertThat(target().matrixParam("x", "1").matrixParam("y", "1%20%7C%7C%202").request().get(String.class), is("1 || 2"));
    }
}
