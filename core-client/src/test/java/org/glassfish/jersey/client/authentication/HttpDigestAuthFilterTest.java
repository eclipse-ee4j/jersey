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

package org.glassfish.jersey.client.authentication;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.glassfish.jersey.client.authentication.DigestAuthenticator.DigestScheme;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Raphael Jolivet (raphael.jolivet at gmail.com)
 * @author Stefan Katerkamp (stefan at katerkamp.de)
 */
public class HttpDigestAuthFilterTest {

    @Test
    public void testParseHeaders1() throws Exception { // no digest scheme
        final DigestAuthenticator f = new DigestAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"), 10000);
        final Method method = DigestAuthenticator.class.getDeclaredMethod("parseAuthHeaders", List.class);
        method.setAccessible(true);
        final DigestScheme ds = (DigestScheme) method.invoke(f,
                Arrays.asList(new String[] {
                        "basic toto=tutu",
                        "basic toto=\"tutu\""
                }));

        Assert.assertNull(ds);
    }

    @Test
    public void testParseHeaders2() throws Exception { // Two concurrent schemes
        final DigestAuthenticator f = new DigestAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"), 10000);
        final Method method = DigestAuthenticator.class.getDeclaredMethod("parseAuthHeaders", List.class);
        method.setAccessible(true);
        final DigestScheme ds = (DigestScheme) method.invoke(f,
                Arrays.asList(new String[] {
                        "Digest realm=\"tata\"",
                        "basic  toto=\"tutu\""
                }));
        Assert.assertNotNull(ds);

        Assert.assertEquals("tata", ds.getRealm());
    }

    @Test
    public void testParseHeaders3() throws Exception { // Complex case, with comma inside value
        final DigestAuthenticator f = new DigestAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"), 10000);
        final Method method = DigestAuthenticator.class.getDeclaredMethod("parseAuthHeaders", List.class);
        method.setAccessible(true);
        final DigestScheme ds = (DigestScheme) method.invoke(f,
                Arrays.asList(new String[] {
                        "digest realm=\"tata\",nonce=\"foo, bar\""
                }));

        Assert.assertNotNull(ds);
        Assert.assertEquals("tata", ds.getRealm());
        Assert.assertEquals("foo, bar", ds.getNonce());
    }

    @Test
    public void testParseHeaders4() throws Exception { // Spaces
        final DigestAuthenticator f = new DigestAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"), 10000);
        final Method method = DigestAuthenticator.class.getDeclaredMethod("parseAuthHeaders", List.class);
        method.setAccessible(true);
        final DigestScheme ds = (DigestScheme) method.invoke(f,
                Arrays.asList(new String[] {
                        "    digest realm =   \"tata\"  ,  opaque=\"bar\" ,nonce=\"foo, bar\""
                }));

        Assert.assertNotNull(ds);
        Assert.assertEquals("tata", ds.getRealm());
        Assert.assertEquals("foo, bar", ds.getNonce());
        Assert.assertEquals("bar", ds.getOpaque());
    }

    @Test
    public void testParseHeaders5() throws Exception { // Mix of quotes and  non-quotes
        final DigestAuthenticator f = new DigestAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"), 10000);
        final Method method = DigestAuthenticator.class.getDeclaredMethod("parseAuthHeaders", List.class);
        method.setAccessible(true);
        final DigestScheme ds = (DigestScheme) method.invoke(f,
                Arrays.asList(new String[] {
                        "    digest realm =   \"tata\"  ,  opaque =bar ,nonce=\"foo, bar\",   algorithm=md5"
                }));

        Assert.assertNotNull(ds);
        Assert.assertEquals("tata", ds.getRealm());
        Assert.assertEquals("foo, bar", ds.getNonce());
        Assert.assertEquals("bar", ds.getOpaque());
        Assert.assertEquals("MD5", ds.getAlgorithm().name());
    }
}
