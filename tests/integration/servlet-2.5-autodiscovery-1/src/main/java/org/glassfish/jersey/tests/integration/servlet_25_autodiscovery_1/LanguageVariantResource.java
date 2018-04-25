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

package org.glassfish.jersey.tests.integration.servlet_25_autodiscovery_1;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

/**
 * @author Martin Matula
 * @author Michal Gajdos
 */
@Path("/abc")
public class LanguageVariantResource {

    @GET
    public Response doGet(@Context Request r) {
        List<Variant> vs = Variant.VariantListBuilder.newInstance()
                .mediaTypes(MediaType.valueOf("application/foo"))
                .languages(new Locale("en")).languages(new Locale("fr")).add()
                .mediaTypes(MediaType.valueOf("application/bar"))
                .languages(new Locale("en")).languages(new Locale("fr")).add()
                .build();

        Variant v = r.selectVariant(vs);
        if (v == null) {
            return Response.notAcceptable(vs).build();
        } else {
            return Response.ok(v.getMediaType().toString() + ", " + v.getLanguage(), v).build();
        }
    }
}
