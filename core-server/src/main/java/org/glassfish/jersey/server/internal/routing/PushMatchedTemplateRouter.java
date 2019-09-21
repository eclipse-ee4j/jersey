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

package org.glassfish.jersey.server.internal.routing;

import org.glassfish.jersey.server.internal.process.RequestProcessingContext;
import org.glassfish.jersey.uri.UriTemplate;

/**
 * Router that pushes {@link UriTemplate uri template} of matched resource of subResource
 * to {@link org.glassfish.jersey.server.internal.routing.RoutingContext routing context}.
 * Before calling this router the {@link PathMatchingRouter} must be called which matches the path
 * and pushes the {@link java.util.regex.MatchResult matched result} into the routing context.
 *
 * @author Miroslav Fuksa
 * @see RoutingContext#pushTemplates(org.glassfish.jersey.uri.UriTemplate, org.glassfish.jersey.uri.UriTemplate)
 */
final class PushMatchedTemplateRouter implements Router {

    private final UriTemplate resourceTemplate;
    private final UriTemplate methodTemplate;

    /**
     * Create a new instance of the push matched template router.
     * <p>
     * This constructor should be used in case a path matching has been performed on both a resource and method paths
     * (in case of sub-resource methods and locators).
     * </p>
     *
     * @param resourceTemplate resource URI template that should be pushed.
     * @param methodTemplate   (sub-resource) method or locator URI template that should be pushed.
     */
    PushMatchedTemplateRouter(final UriTemplate resourceTemplate,
                              final UriTemplate methodTemplate) {
        this.resourceTemplate = resourceTemplate;
        this.methodTemplate = methodTemplate;
    }

    /**
     * Create a new instance of the push matched template router.
     * <p>
     * This constructor should be used in case a single path matching has been performed (in case of resource methods,
     * only the resource path is matched).
     * </p>
     *
     * @param resourceTemplate resource URI template that should be pushed.
     */
    PushMatchedTemplateRouter(final UriTemplate resourceTemplate) {
        this.resourceTemplate = resourceTemplate;
        this.methodTemplate = null;
    }

    @Override
    public Continuation apply(final RequestProcessingContext context) {
        context.routingContext().pushTemplates(resourceTemplate, methodTemplate);

        return Continuation.of(context);
    }
}
