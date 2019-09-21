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

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Martin Matula
 */
public class OAuth1SignatureTest {

    @Test
    public void testNormalizeParameters() {
        final MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
        params.add("org-country", "US");
        params.add("org", "acme");
        params.add("a", "b");
        params.add("org", "dummy");

        String normalizedParams = OAuth1Signature.normalizeParameters(new OAuth1Request() {
            @Override
            public String getRequestMethod() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public URL getRequestURL() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Set<String> getParameterNames() {
                return params.keySet();
            }

            @Override
            public List<String> getParameterValues(String name) {
                return params.get(name);
            }

            @Override
            public List<String> getHeaderValues(String name) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void addHeaderValue(String name, String value) throws IllegalStateException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, new OAuth1Parameters());
        assertEquals("a=b&org=acme&org=dummy&org-country=US", normalizedParams);
    }

    @Test
    public void testNullParamValue() {
        final MultivaluedMap<String, String> params = new MultivaluedHashMap<String, String>();
        params.add("org-country", "US");
        params.put("org", Arrays.asList(new String[]{null}));
        params.add("a", "b");

        String normalizedParams = OAuth1Signature.normalizeParameters(new OAuth1Request() {
            @Override
            public String getRequestMethod() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public URL getRequestURL() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Set<String> getParameterNames() {
                return params.keySet();
            }

            @Override
            public List<String> getParameterValues(String name) {
                return params.get(name);
            }

            @Override
            public List<String> getHeaderValues(String name) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void addHeaderValue(String name, String value) throws IllegalStateException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, new OAuth1Parameters());
        assertEquals("a=b&org=&org-country=US", normalizedParams);
    }
}
