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

package org.glassfish.jersey.tests.integration.jersey1960;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.message.MessageUtils;

/**
 * Filter testing injection support for of servlet artifacts.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@PreMatching
public class RequestFilter implements ContainerRequestFilter {
    public static final String REQUEST_NUMBER = "request-number";
    @Context
    private HttpServletRequest hsReq;
    @Context
    private HttpServletResponse hsResp;
    @Context
    private ServletContext sCtx;
    @Context
    private ServletConfig sCfg;

    @Override
    public void filter(final ContainerRequestContext ctx) throws IOException {
        final StringBuilder sb = new StringBuilder();

        // First, make sure there are no null injections.
        if (hsReq == null) {
            sb.append("HttpServletRequest is null.\n");
        }
        if (hsResp == null) {
            sb.append("HttpServletResponse is null.\n");
        }
        if (sCtx == null) {
            sb.append("ServletContext is null.\n");
        }
        if (sCfg == null) {
            sb.append("ServletConfig is null.\n");
        }

        if (sb.length() > 0) {
            ctx.abortWith(Response.serverError().entity(sb.toString()).build());
        }

        // let's also test some method calls
        int flags = 0;

        if ("/jersey-1960".equals(hsReq.getServletPath())) {
            flags += 1;
        }
        if (!hsResp.isCommitted()) {
            flags += 10;
        }
        if (!sCtx.getServerInfo().isEmpty()) {
            flags += 100;
        }
        if (sCfg.getServletContext() == sCtx) {
            flags += 1000;
        }
        final String header = hsReq.getHeader(REQUEST_NUMBER);

        ctx.setEntityStream(new ByteArrayInputStream(("filtered-" + flags + "-" + header).getBytes(
                MessageUtils.getCharset(ctx.getMediaType()))));
    }
}
