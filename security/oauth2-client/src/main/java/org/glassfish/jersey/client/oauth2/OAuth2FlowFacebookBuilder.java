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

package org.glassfish.jersey.client.oauth2;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import org.glassfish.jersey.message.internal.ReaderWriter;

/**
 * Class that provides methods to build {@link OAuth2CodeGrantFlow} pre-configured for usage
 * with Facebook provider.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
class OAuth2FlowFacebookBuilder {

    /**
     * Get a builder that can be directly used to perform Authorization Code Grant flow defined by
     * Facebook documentation.
     *
     * @param clientIdentifier Client identifier.
     * @param redirectUri Redirect URI
     * @param client Client instance that should be used to perform Access token request.
     * @return Builder instance.
     */
    public static OAuth2CodeGrantFlow.Builder getFacebookAuthorizationBuilder(ClientIdentifier clientIdentifier,
                                                                              String redirectUri, Client client) {

        final AuthCodeGrantImpl.Builder builder = new AuthCodeGrantImpl.Builder();
        builder.accessTokenUri("https://graph.facebook.com/oauth/access_token");
        builder.authorizationUri("https://www.facebook.com/dialog/oauth");
        builder.redirectUri(redirectUri);
        builder.clientIdentifier(clientIdentifier);
        client.register(FacebookTokenMessageBodyReader.class);
        builder.client(client);
        return builder;
    }


    /**
     * Entity provider that deserializes entity returned from Access Token request into {@link TokenResult}.
     * The format of data is in query param style: "access_token=45a64a654&expires_in=3600".
     */
    @Consumes("text/plain")
    static class FacebookTokenMessageBodyReader implements MessageBodyReader<TokenResult> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type.equals(TokenResult.class);
        }

        @Override
        public TokenResult readFrom(Class<TokenResult> type, Type genericType, Annotation[] annotations,
                                    MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                    InputStream entityStream) throws IOException, WebApplicationException {

            Map<String, Object> map = new HashMap<>();
            final String str = ReaderWriter.readFromAsString(entityStream, mediaType);
            final String[] splitArray = str.split("&");
            for (String s : splitArray) {
                final String[] keyValue = s.split("=");
                map.put(keyValue[0], keyValue[1]);
            }

            return new TokenResult(map);
        }
    }


}
