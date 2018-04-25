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

package org.glassfish.jersey.server.internal.routing;

import java.util.regex.MatchResult;

import javax.ws.rs.container.ResourceInfo;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.internal.process.Endpoint;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.RuntimeResource;
import org.glassfish.jersey.uri.UriTemplate;

/**
 * Jersey request matching and routing context.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Martin Matula
 */
public interface RoutingContext extends ResourceInfo, ExtendedUriInfo {

    /**
     * Push the result of the successful request URI routing pattern match.
     *
     * @param matchResult successful request URI routing pattern
     *                    {@link java.util.regex.MatchResult match result}.
     */
    public void pushMatchResult(MatchResult matchResult);

    /**
     * Push the resource that matched the request URI.
     *
     * @param resource instance of the resource that matched the request URI.
     */
    public void pushMatchedResource(Object resource);

    /**
     * Peek the last resource object that successfully matched the request URI.
     *
     * @return last resource matched as previously set by {@link #pushMatchedResource}
     */
    public Object peekMatchedResource();

    /**
     * Push matched request URI routing pattern {@link org.glassfish.jersey.uri.UriTemplate templates}
     * for a single matched method.
     * <p>
     * In case only a single path matching has been performed on the resource (in case of resource methods,
     * only the resource path is matched), the method template should be passed as {@code null}.
     * In case a path matching has been performed on both a resource and method paths
     * (in case of sub-resource methods and locators), both templates (resource and method) must be specified.
     * </p>
     *
     * @param resourceTemplate resource URI template that should be pushed.
     * @param methodTemplate   (sub-resource) method or locator URI template that should be pushed.
     */
    public void pushTemplates(UriTemplate resourceTemplate, UriTemplate methodTemplate);

    /**
     * Get the final matching group of the last successful request URI routing
     * pattern {@link java.util.regex.MatchResult match result}. Also known as right-hand path.
     * <p>
     * May be empty but is never {@code null}.
     * </p>
     *
     * @return final matching group of the last successful request URI routing pattern match result.
     */
    public String getFinalMatchingGroup();

    /**
     * Add currently matched left-hand side part of request path to the list of
     * matched paths returned by {@link javax.ws.rs.core.UriInfo#getMatchedURIs()}.
     * <p/>
     * Left-hand side request path is the request path excluding the suffix
     * part of the path matched by the {@link #getFinalMatchingGroup() final
     * matching group} of the last successful request URI routing pattern.
     */
    public void pushLeftHandPath();

    /**
     * Set the matched server-side endpoint.
     * <p/>
     * This method can be used in a non-terminal stage to set the server-side endpoint that
     * can be retrieved and processed by a subsequent stage.
     *
     * @param endpoint matched server-side endpoint.
     */
    public void setEndpoint(Endpoint endpoint);

    /**
     * Get the matched server-side endpoint if present, or {@code null} otherwise.
     *
     * @return matched server-side endpoint, or {@code null} if not available.
     */
    public Endpoint getEndpoint();

    /**
     * Set the matched {@link ResourceMethod resource method}. This method needs to be called only if the method was
     * matched. This method should be called only for setting the final resource method and not for setting sub resource
     * locators invoked during matching.
     *
     * @param resourceMethod Resource method that was matched.
     */
    public void setMatchedResourceMethod(ResourceMethod resourceMethod);

    /**
     * Push the matched {@link ResourceMethod sub resource locator method}.
     *
     * @param resourceLocator Sub resource locator method.
     */
    public void pushMatchedLocator(ResourceMethod resourceLocator);

    /**
     * Push a matched {@link RuntimeResource runtime resource} that was visited during matching phase. This method must
     * be called for any matched runtime resource.
     *
     * @param runtimeResource Runtime resource that was matched during matching.
     */
    public void pushMatchedRuntimeResource(RuntimeResource runtimeResource);

    /**
     * Push {@link Resource sub resource} returned from a sub resource locator method. The pushed
     * {@code subResourceFromLocator} is the final model of a sub resource which is already enhanced by
     * {@link org.glassfish.jersey.server.model.ModelProcessor model processors} and
     * validated.
     *
     * @param subResourceFromLocator Resource constructed from result of sub resource locator method.
     */
    public void pushLocatorSubResource(Resource subResourceFromLocator);

    /**
     * Set the throwable that was mapped to a response.
     *
     * @param throwable throwable that was mapped to a response.
     * @see #getMappedThrowable()
     */
    public void setMappedThrowable(Throwable throwable);
}
