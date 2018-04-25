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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.oauth1.signature.OAuth1Request;
import org.glassfish.jersey.server.ContainerRequest;

/**
 * Wraps a Jersey {@link ContainerRequestContext} object, implementing the
 * OAuth signature library {@link org.glassfish.jersey.oauth1.signature.OAuth1Request} interface.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class OAuthServerRequest implements OAuth1Request {

    private final ContainerRequestContext context;

    private static Set<String> EMPTY_SET = Collections.emptySet();

    private static List<String> EMPTY_LIST = Collections.emptyList();
    private final Value<MultivaluedMap<String, String>> formParams = Values.lazy(
            new Value<MultivaluedMap<String, String>>() {
                @Override
                public MultivaluedMap<String, String> get() {
                    MultivaluedMap<String, String> params = null;
                    final MediaType mediaType = context.getMediaType();
                    if (mediaType != null && MediaTypes.typeEqual(mediaType, MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
                        final ContainerRequest jerseyRequest = (ContainerRequest) context;
                        jerseyRequest.bufferEntity();
                        final Form form = jerseyRequest.readEntity(Form.class);
                        params = form.asMap();
                    }
                    return params;
                }
            });

    /**
     * Create a new instance.
     *
     * @param context Container request context.
     */
    public OAuthServerRequest(ContainerRequestContext context) {
        this.context = context;
    }

    @Override
    public String getRequestMethod() {
        return context.getMethod();
    }

    @Override
    public URL getRequestURL() {
        try {
            return context.getUriInfo().getRequestUri().toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(OAuthServerRequest.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private static Set<String> keys(MultivaluedMap<String, String> mvm) {
        if (mvm == null) {
            return EMPTY_SET;
        }
        return mvm.keySet();
    }

    private static List<String> values(MultivaluedMap<String, String> mvm, String key) {
        if (mvm == null) {
            return EMPTY_LIST;
        }
        List<String> v = mvm.get(key);
        if (v == null) {
            return EMPTY_LIST;
        }
        return v;
    }

    @Override
    public Set<String> getParameterNames() {
        HashSet<String> n = new HashSet<String>();
        n.addAll(keys(context.getUriInfo().getQueryParameters()));
        n.addAll(keys(formParams.get()));

        return n;
    }

    @Override
    public List<String> getParameterValues(String name) {
        ArrayList<String> v = new ArrayList<String>();
        v.addAll(values(context.getUriInfo().getQueryParameters(), name));
        v.addAll(values(formParams.get(), name));
        return v;
    }

    @Override
    public List<String> getHeaderValues(String name) {
        return context.getHeaders().get(name);
    }

    @Override
    public void addHeaderValue(String name, String value) throws IllegalStateException {
        throw new IllegalStateException("Modifying OAuthServerRequest unsupported");
    }

}
