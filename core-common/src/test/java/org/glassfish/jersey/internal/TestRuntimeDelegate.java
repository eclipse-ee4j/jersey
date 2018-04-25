/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.message.internal.MessagingBinders;

import org.junit.Assert;

/**
 * Test runtime delegate.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class TestRuntimeDelegate extends AbstractRuntimeDelegate {

    public TestRuntimeDelegate() {
        super(new MessagingBinders.HeaderDelegateProviders().getHeaderDelegateProviders());
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType)
            throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void testMediaType() {
        MediaType m = new MediaType("text", "plain");
        Assert.assertNotNull(m);
    }

    public void testUriBuilder() {
        UriBuilder ub = RuntimeDelegate.getInstance().createUriBuilder();
        Assert.assertNotNull(ub);
    }

    public void testResponseBuilder() {
        Response.ResponseBuilder rb = RuntimeDelegate.getInstance().createResponseBuilder();
        Assert.assertNotNull(rb);
    }

    public void testVariantListBuilder() {
        Variant.VariantListBuilder vlb = RuntimeDelegate.getInstance().createVariantListBuilder();
        Assert.assertNotNull(vlb);
    }

    public void testLinkBuilder() {
        final Link.Builder linkBuilder = RuntimeDelegate.getInstance().createLinkBuilder();
        Assert.assertNotNull(linkBuilder);
    }

    public void testWebApplicationException() {
        WebApplicationException wae = new WebApplicationException();
        Assert.assertNotNull(wae);
    }
}
