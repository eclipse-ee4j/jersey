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

package org.glassfish.jersey.linking;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.linking.contributing.ResourceLinkContributionContext;
import org.glassfish.jersey.linking.mapping.ResourceMappingContext;

/**
 * Filter that processes {@link Link} annotated fields in returned response
 * entities.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 * @see Link
 */
class ResponseLinkFilter implements ContainerResponseFilter {

    @Context
    private UriInfo uriInfo;

    @Context
    private ResourceMappingContext rmc;

    @Context
    private ResourceLinkContributionContext rlcc;

    @Override
    @SuppressWarnings("unchecked")
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        final Object entity = response.getEntity();

        if (entity != null && !uriInfo.getMatchedResources().isEmpty()) {
            Class<?> entityClass = entity.getClass();
            HeaderProcessor lhp = new HeaderProcessor(entityClass);
            lhp.processLinkHeaders(entity, uriInfo, rmc, response.getHeaders());
            FieldProcessor lp = new FieldProcessor(entityClass);
            lp.processLinks(entity, uriInfo, rmc, rlcc);
        }

    }
}
