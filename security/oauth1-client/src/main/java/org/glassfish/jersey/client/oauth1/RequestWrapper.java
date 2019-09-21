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

package org.glassfish.jersey.client.oauth1;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.oauth1.signature.OAuth1Request;

/**
 * Implements the OAuth signature library Request interface, wrapping a Jersey
 * client request object.
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 * @since 2.3
 */
class RequestWrapper implements OAuth1Request {

    /** The wrapped Jersey client request. */
    private final ClientRequestContext clientRequest;

    private final MessageBodyWorkers messageBodyWorkers;


    /** Form and query parameters from the request (lazily initialized). */
    private MultivaluedMap<String, String> parameters = null;

    private void setParameters() {
        parameters = new MultivaluedHashMap<String, String>();
        parameters.putAll(RequestUtil.getQueryParameters(clientRequest));
        parameters.putAll(RequestUtil.getEntityParameters(clientRequest, messageBodyWorkers));
    }

    /**
     * Constructs a new OAuth client request wrapper around the specified
     * Jersey client request object.
     *
     * @param clientRequest Client request.
     * @param messageBodyWorkers Message body workers.
     */
    public RequestWrapper(final ClientRequestContext clientRequest, MessageBodyWorkers messageBodyWorkers) {
        this.clientRequest = clientRequest;
        this.messageBodyWorkers = messageBodyWorkers;
        setParameters(); // stored because parsing query/entity parameters too much work for each value-get
    }

    @Override
    public String getRequestMethod() {
        return clientRequest.getMethod();
    }

    @Override
    public URL getRequestURL() {
        try {
            final URI uri = clientRequest.getUri();
            return uri.toURL();
        } catch (MalformedURLException ex) {
            Logger.getLogger(RequestWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Set<String> getParameterNames() {
        return parameters.keySet();
    }

    @Override
    public List<String> getParameterValues(final String name) {
        return parameters.get(name);
    }

    @Override
    public List<String> getHeaderValues(final String name) {

        ArrayList<String> list = new ArrayList<>();

        for (String header : clientRequest.getStringHeaders().get(name)) {
            list.add(header);
        }

        return list;
    }

    @Override
    public void addHeaderValue(final String name, final String value) {
        clientRequest.getHeaders().add(name, value);
    }
}

