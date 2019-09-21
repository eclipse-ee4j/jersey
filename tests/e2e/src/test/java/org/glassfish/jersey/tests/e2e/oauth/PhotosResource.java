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

package org.glassfish.jersey.tests.e2e.oauth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

import javax.inject.Inject;

import org.glassfish.jersey.oauth1.signature.OAuth1Parameters;
import org.glassfish.jersey.oauth1.signature.OAuth1Secrets;
import org.glassfish.jersey.oauth1.signature.OAuth1Signature;
import org.glassfish.jersey.oauth1.signature.OAuth1SignatureException;
import org.glassfish.jersey.server.oauth1.internal.OAuthServerRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Path("/photos")
public class PhotosResource {

    @Inject
    private OAuth1Signature oAuth1Signature;

    @GET
    @Produces("text/plain")
    public String handle(
            @QueryParam("file") String file,
            @QueryParam("size") String size,
            @Context ContainerRequestContext request) {

        OAuthServerRequest osr = new OAuthServerRequest(request);

        OAuth1Secrets secrets = new OAuth1Secrets()
                .consumerSecret("kd94hf93k423kf44").tokenSecret("pfkkdhi9sl3r4s00");

        OAuth1Parameters params = new OAuth1Parameters().readRequest(osr);

        // ensure query parameters are as expected
        assertEquals(file, "vacation.jpg");
        assertEquals(size, "original");

        // ensure query parameters correctly parsed into OAuth parameters object
        assertEquals(params.getConsumerKey(), "dpf43f3p2l4k3l03");
        assertEquals(params.getToken(), "nnch734d00sl2jdk");
        assertEquals(params.getSignatureMethod(), "HMAC-SHA1");
        assertEquals(params.getTimestamp(), "1191242096");
        assertEquals(params.getNonce(), "kllo9940pd9333jh");
        assertEquals(params.getVersion(), "1.0");

        try {
            // verify the HMAC-SHA1 signature
            assertTrue(oAuth1Signature.verify(osr, params, secrets));
        } catch (OAuth1SignatureException ose) {
            fail(ose.getMessage());
        }

        return "PHOTO";
    }
}

