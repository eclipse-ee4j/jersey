/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.ejb.resources;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * JERSEY-2320 reproducer. {@link CustomExceptionTwo} will get mapped
 * to an ordinary response. We make sure the mapper gets injected properly
 * by both Jersey runtime and EJB container.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Stateless
public class EjbExceptionMapperTwo extends EjbExceptionMapperBase<CustomExceptionTwo> {

    @Context UriInfo uriInfo;
    @EJB EchoBean echoBean;

    public static final String RESPONSE_BODY = "custom exception two thrown";

    @Override
    public Response toResponse(final CustomExceptionTwo exception) {
        return Response.ok(RESPONSE_BODY)
                .header("My-Location", uriInfo.getPath())
                .header("My-Echo", echoBean.echo("2")).build();
    }
}
