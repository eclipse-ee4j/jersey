/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.opentracing;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import io.opentracing.Span;
import io.opentracing.tag.Tags;

/**
 * Retrieves stored span from the {@link ClientRequestContext} and adds response details to the tracing info.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 * @since 2.26
 */
public class OpenTracingClientResponseFilter implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        final Object spanProperty = requestContext.getProperty(OpenTracingFeature.SPAN_CONTEXT_PROPERTY);
        if (spanProperty != null && spanProperty instanceof Span) {
            ((Span) spanProperty)
                    .setTag(Tags.HTTP_STATUS.getKey(), responseContext.getStatus())
                    .setTag(LocalizationMessages.OPENTRACING_TAG_HAS_RESPONSE_ENTITY(), responseContext.hasEntity())
                    .setTag(LocalizationMessages.OPENTRACING_TAG_RESPONSE_LENGTH(), responseContext.getLength())
                    .setTag(LocalizationMessages.OPENTRACING_TAG_RESPONSE_HEADERS(),
                            OpenTracingUtils.headersAsString(responseContext.getHeaders()))
                    .finish();
        }
    }
}
