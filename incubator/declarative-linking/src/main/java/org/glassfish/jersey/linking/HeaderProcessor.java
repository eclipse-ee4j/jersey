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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.linking.mapping.ResourceMappingContext;

/**
 * Processes @Link and @LinkHeaders annotations on entity classes and
 * adds appropriate HTTP Link headers.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class HeaderProcessor<T> {

    private EntityDescriptor instanceDescriptor;

    HeaderProcessor(Class<T> c) {
        instanceDescriptor = EntityDescriptor.getInstance(c);
    }

    /**
     * Process any {@link InjectLink} annotations on the supplied entity.
     * @param entity the entity object returned by the resource method
     * @param uriInfo the uriInfo for the request
     * @param headers the map into which the headers will be added
     */
    void processLinkHeaders(T entity,
                            UriInfo uriInfo,
                            ResourceMappingContext rmc,
                            MultivaluedMap<String, Object> headers) {
        List<String> headerValues = getLinkHeaderValues(entity, uriInfo, rmc);
        for (String headerValue : headerValues) {
            headers.add("Link", headerValue);
        }
    }

    List<String> getLinkHeaderValues(Object entity, UriInfo uriInfo, ResourceMappingContext rmc) {
        final List<Object> matchedResources = uriInfo.getMatchedResources();

        if (!matchedResources.isEmpty()) {
            final Object resource = matchedResources.get(0);
            final List<String> headerValues = new ArrayList<>();

            for (LinkHeaderDescriptor desc : instanceDescriptor.getLinkHeaders()) {
                if (ELLinkBuilder.evaluateCondition(desc.getCondition(), entity, resource, entity)) {
                    String headerValue = getLinkHeaderValue(desc, entity, resource, uriInfo, rmc);
                    headerValues.add(headerValue);
                }
            }
            return headerValues;
        }

        return Collections.emptyList();
    }

    private static String getLinkHeaderValue(LinkHeaderDescriptor desc, Object entity, Object resource, UriInfo uriInfo,
                                             ResourceMappingContext rmc) {
        URI uri = ELLinkBuilder.buildURI(desc, entity, resource, entity, uriInfo, rmc);
        InjectLink link = desc.getLinkHeader();
        return InjectLink.Util.buildLinkFromUri(uri, link).toString();
    }

}
