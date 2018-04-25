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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Gajdos
 */
public class LanguageVariantResourceITCase extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(LanguageVariantResource.class);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testMediaTypesAndLanguages() {
        _test("english", "foo", "en", "application/foo");
        _test("french", "foo", "fr", "application/foo");

        _test("english", "bar", "en", "application/bar");
        _test("french", "bar", "fr", "application/bar");
    }

    private void _test(String ul, String um, String l, String m) {
        Response r = target().path("abc." + ul + "." + um).request().get();
        assertEquals(m + ", " + l, r.readEntity(String.class));
        assertEquals(l, r.getLanguage().toString());
        assertEquals(m, r.getMediaType().toString());

        r = target().path("abc." + um + "." + ul).request().get();
        assertEquals(m + ", " + l, r.readEntity(String.class));
        assertEquals(l, r.getLanguage().toString());
        assertEquals(m, r.getMediaType().toString());

        r = target().path("abc").request(m).header(HttpHeaders.ACCEPT_LANGUAGE, l).get();
        assertEquals(m + ", " + l, r.readEntity(String.class));
        assertEquals(l, r.getLanguage().toString());
        assertEquals(m, r.getMediaType().toString());
    }
}
