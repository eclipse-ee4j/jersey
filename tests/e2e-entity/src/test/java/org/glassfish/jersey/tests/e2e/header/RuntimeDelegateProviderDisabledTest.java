/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.header;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.glassfish.jersey.tests.e2e.header.HeaderDelegateProviderTest.DISABLED_VALUE;
import static org.glassfish.jersey.tests.e2e.header.HeaderDelegateProviderTest.HEADER_NAME;

public class RuntimeDelegateProviderDisabledTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(HeaderDelegateProviderTest.HeaderSettingResource.class,
                HeaderDelegateProviderTest.HeaderContainerResponseFilter.class)
                .property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_SERVER, true);
    }

    @Test
    public void testClientResponseHeaders() {
        try (Response response = target("/simple").request().get()) {
            Assert.assertEquals(
                    DISABLED_VALUE,
                    response.getHeaderString(HeaderDelegateProviderTest.HeaderContainerResponseFilter.class.getSimpleName())
            );
            Assert.assertEquals(
                    DISABLED_VALUE,
                    response.getStringHeaders().getFirst(HEADER_NAME)
            );
        }
    }

    @Test
    public void testContainerResponseFilter() {
        try (Response response = target("/simple").request().get()) {
            Assert.assertEquals(DISABLED_VALUE, response.getHeaderString(HEADER_NAME));
        }
    }

    @Test
    public void testProviderOnClientDisabled() {
        try (Response response = target("/headers")
                .property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_CLIENT, true).request()
                .header(HEADER_NAME, new HeaderDelegateProviderTest.BeanForHeaderDelegateProviderTest())
                .get()) {
            Assert.assertEquals(
                    DISABLED_VALUE,
                    response.getHeaderString(HeaderDelegateProviderTest.HeaderSettingResource.class.getSimpleName())
            );
        }
    }

    @Test
    public void testProviderOnClientFilter() {
        try (Response response = target("/clientfilter")
                .property(CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE_CLIENT, true)
                .register(HeaderDelegateProviderTest.HeaderClientRequestFilter.class)
                .request().get()) {
            Assert.assertEquals(
                    DISABLED_VALUE,
                    response.readEntity(String.class)
            );
        }
    }
}
