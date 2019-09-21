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

package org.glassfish.jersey.server.oauth1.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.oauth1.signature.OAuth1Parameters;
import org.glassfish.jersey.oauth1.signature.OAuth1Signature;
import org.glassfish.jersey.oauth1.signature.OAuth1SignatureException;
import org.glassfish.jersey.oauth1.signature.OAuth1Secrets;
import org.glassfish.jersey.server.oauth1.OAuth1Consumer;
import org.glassfish.jersey.server.oauth1.OAuth1Exception;
import org.glassfish.jersey.server.oauth1.OAuth1Provider;
import org.glassfish.jersey.server.oauth1.OAuth1Token;
import org.glassfish.jersey.server.oauth1.TokenResource;

/**
 * Resource handling request token requests.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Martin Matula
 */

@Path("/requestToken")
public class RequestTokenResource {
    @Inject
    private OAuth1Provider provider;

    @Inject
    private ContainerRequestContext requestContext;

    @Inject
    private OAuth1Signature oAuth1Signature;

    /**
     * POST method for creating a request for a Request Token.
     *
     * @return an HTTP response with content of the updated or created resource.
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/x-www-form-urlencoded")
    @TokenResource
    public Response postReqTokenRequest() {
        OAuthServerRequest request = new OAuthServerRequest(requestContext);
        OAuth1Parameters params = new OAuth1Parameters();
        params.readRequest(request);

        String tok = params.getToken();
        if ((tok != null) && (!tok.contentEquals(""))) {
            throw new OAuth1Exception(Response.Status.BAD_REQUEST, null);
        }

        String consKey = params.getConsumerKey();
        if (consKey == null) {
            throw new OAuth1Exception(Response.Status.BAD_REQUEST, null);
        }

        OAuth1Consumer consumer = provider.getConsumer(consKey);
        if (consumer == null) {
            throw new OAuth1Exception(Response.Status.BAD_REQUEST, null);
        }
        OAuth1Secrets secrets = new OAuth1Secrets().consumerSecret(consumer.getSecret()).tokenSecret("");

        boolean sigIsOk = false;
        try {
            sigIsOk = oAuth1Signature.verify(request, params, secrets);
        } catch (OAuth1SignatureException ex) {
            Logger.getLogger(RequestTokenResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!sigIsOk) {
            throw new OAuth1Exception(Response.Status.BAD_REQUEST, null);
        }

        MultivaluedMap<String, String> parameters = new MultivaluedHashMap<String, String>();
        for (String n : request.getParameterNames()) {
            parameters.put(n, request.getParameterValues(n));
        }

        OAuth1Token rt = provider.newRequestToken(consKey, params.getCallback(), parameters);

        Form resp = new Form();
        resp.param(OAuth1Parameters.TOKEN, rt.getToken());
        resp.param(OAuth1Parameters.TOKEN_SECRET, rt.getSecret());
        resp.param(OAuth1Parameters.CALLBACK_CONFIRMED, "true");
        return Response.ok(resp).build();
    }
}
