/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 */
public class InvalidFormTest extends AbstractTypeTester {

    public abstract static class Resource<T> {

        @POST
        public void post(T t) {
        }
    }

    @Path("multivaluedmap")
    public static class FormMultivaluedMapResource extends Resource<MultivaluedMap<String, String>> {}

    @Test
    public void testFormMultivaluedMapRepresentation() {
        _test("multivaluedmap");
    }

    @Path("form")
    public static class FormResource extends Resource<Form> {}

    @Test
    public void testRepresentation() {
        _test("form");
    }

    public void _test(String path) {
        String form = "m=%";
        Response cr = target(path).request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        assertEquals(400, cr.getStatus());
    }
}
