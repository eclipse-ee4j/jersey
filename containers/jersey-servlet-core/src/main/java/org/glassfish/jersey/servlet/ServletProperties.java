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

package org.glassfish.jersey.servlet;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * Jersey servlet container configuration properties.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@PropertiesClass
public final class ServletProperties {

    /**
     * If set, indicates the URL pattern of the Jersey servlet filter context path.
     * <p>
     * If the URL pattern of a filter is set to a base path and a wildcard,
     * such as "/base/*", then this property can be used to declare a filter
     * context path that behaves in the same manner as the Servlet context
     * path for determining the base URI of the application. (Note that with
     * the Servlet 2.x API it is not possible to determine the URL pattern
     * without parsing the {@code web.xml}, hence why this property is necessary.)
     * <p>
     * The property is only applicable when {@link ServletContainer Jersey servlet
     * container} is configured to run as a {@link javax.servlet.Filter}, otherwise this property
     * will be ignored.
     * <p>
     * The value of the property may consist of one or more path segments separate by
     * {@code '/'}.
     * <p></p>
     * A default value is not set.
     * <p></p>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String FILTER_CONTEXT_PATH = "jersey.config.servlet.filter.contextPath";

    /**
     * If set to {@code true} and a 404 response with no entity body is returned
     * from either the runtime or the application then the runtime forwards the
     * request to the next filter in the filter chain. This enables another filter
     * or the underlying servlet engine to process the request. Before the request
     * is forwarded the response status is set to 200.
     * <p>
     * This property is an alternative to setting a {@link #FILTER_STATIC_CONTENT_REGEX
     * static content regular expression} and requires less configuration. However,
     * application code, such as methods corresponding to sub-resource locators,
     * may be invoked when this feature is enabled.
     * <p></p>
     * The property is only applicable when {@link ServletContainer Jersey servlet
     * container} is configured to run as a {@link javax.servlet.Filter}, otherwise
     * this property will be ignored.
     * <p></p>
     * Application code, such as methods corresponding to sub-resource locators
     * may be invoked when this feature is enabled.
     * <p>
     * The default value is {@code false}.
     * <p></p>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String FILTER_FORWARD_ON_404 = "jersey.config.servlet.filter.forwardOn404";

    /**
     * If set the regular expression is used to match an incoming servlet path URI
     * to some web page content such as static resources or JSPs to be handled
     * by the underlying servlet engine.
     * <p></p>
     * The property is only applicable when {@link ServletContainer Jersey servlet
     * container} is configured to run as a {@link javax.servlet.Filter}, otherwise
     * this property will be ignored. If a servlet path matches this regular
     * expression then the filter forwards the request to the next filter in the
     * filter chain so that the underlying servlet engine can process the request
     * otherwise Jersey will process the request. For example if you set the value
     * to {@code /(image|css)/.*} then you can serve up images and CSS files
     * for your Implicit or Explicit Views while still processing your JAX-RS
     * resources.
     * <p></p>
     * The type of this property must be a String and the value must be a valid
     * regular expression.
     * <p></p>
     * A default value is not set.
     * <p></p>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String FILTER_STATIC_CONTENT_REGEX = "jersey.config.servlet.filter.staticContentRegex";

    /**
     * Application configuration initialization property whose value is a fully
     * qualified class name of a class that implements {@link javax.ws.rs.core.Application}.
     * <p></p>
     * A default value is not set.
     * <p></p>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    // TODO implement generic support
    public static final String JAXRS_APPLICATION_CLASS = "javax.ws.rs.Application";

    /**
     * Indicates that Jersey should scan the whole web app for application-specific resources and
     * providers. If the property is present and the value is not {@code false}, the whole web app
     * will be scanned for JAX-RS root resources (annotated with {@link javax.ws.rs.Path @Path})
     * and providers (annotated with {@link javax.ws.rs.ext.Provider @Provider}).
     * <p></p>
     * The property value MUST be an instance of {@link String}. The allowed values are {@code true}
     * and {@code false}.
     * <p></p>
     * A default value is not set.
     * <p></p>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String PROVIDER_WEB_APP = "jersey.config.servlet.provider.webapp";

    /**
     * If {@code true} then query parameters will not be treated as form parameters (e.g. injectable using
     * {@link javax.ws.rs.FormParam}) in case a Form request is processed by server.
     * <p>
     * The default value is {@code false}.
     * </p>
     * <p>
     * The name of the configuration property is <tt>{@value}</tt>.
     * </p>
     *
     * @since 2.16
     */
    public static final String QUERY_PARAMS_AS_FORM_PARAMS_DISABLED = "jersey.config.servlet.form.queryParams.disabled";

    /**
     * Identifies the object that will be used as a parent {@code HK2 ServiceLocator} in the Jersey
     * {@link WebComponent}.
     * <p></p>
     * This property gives a possibility to use HK2 services that are registered and/or created
     * outside of the Jersey server context.
     * <p></p>
     * By default this property is not set.
     * <p></p>
     * The name of the configuration property is <tt>{@value}</tt>.
     */
    public static final String SERVICE_LOCATOR = "jersey.config.servlet.context.serviceLocator";

    private ServletProperties() {
        // prevents instantiation
    }
}
