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

package org.glassfish.jersey.oauth1.signature;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Paul C. Bryan <pbryan@sun.com>
 */
class DummyRequest implements OAuth1Request {

    private HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();

    private HashMap<String, ArrayList<String>> params = new HashMap<String, ArrayList<String>>();

    private String requestMethod;

    private String requestURL;

    public DummyRequest() {
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String method) {
        requestMethod = method;
    }

    public DummyRequest requestMethod(String method) {
        setRequestMethod(method);
        return this;
    }

    public URL getRequestURL() {
        try {
            return new URL(requestURL);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DummyRequest.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void setRequestURL(String url) {
        requestURL = url;
    }

    public DummyRequest requestURL(String url) {
        setRequestURL(url);
        return this;
    }

    public List<String> getHeaderValues(String name) {
        return headers.get(name);
    }

    public void addHeaderValue(String name, String value) {
        ArrayList<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            headers.put(name, values);
        }
        values.add(value);
    }

    public DummyRequest headerValue(String name, String value) {
        addHeaderValue(name, value);
        return this;
    }

    public Set<String> getParameterNames() {
        return params.keySet();
    }

    public List<String> getParameterValues(String name) {
        return params.get(name);
    }

    public synchronized void addParameterValue(String name, String value) {
        ArrayList<String> values = params.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            params.put(name, values);
        }
        values.add(value);
    }

    public DummyRequest parameterValue(String name, String value) {
        addParameterValue(name, value);
        return this;
    }
}
