/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Injects a {@link javax.ws.rs.client.WebTarget resource target} pointing at
 * a resource identified by the resolved URI into a method parameter,
 * class field or a bean property.
 * <p/>
 * Injected variable must be of type {@link javax.ws.rs.client.WebTarget}.
 *
 * @author Marek Potociar
 * @see javax.ws.rs.client.WebTarget
 * @since 2.0
 */
@java.lang.annotation.Target({PARAMETER, FIELD, METHOD})
@Retention(RUNTIME)
@Documented
public @interface Uri {

    /**
     * Specifies the URI of the injected {@link javax.ws.rs.client.WebTarget
     * resource target}.
     *
     * The value must be in the form of absolute URI if not used from inside of
     * a JAX-RS component class. For example:
     * <pre>
     * public class AuditingFilter implements RequestFilter {
     *    &#64;Uri("users/{name}/orders")
     *    WebTarget userOrders;
     *
     *    // An external resource target
     *    &#64;Uri("http://mail.acme.com/accounts/{name}")
     *    WebTarget userEmailAccount;
     *
     *    // An external, template-based resource target
     *    &#64;Uri("http://{audit-host}:{audit-port}/auditlogs/")
     *    WebTarget auditLogs;
     *    ...
     * }
     * </pre>
     *
     * If used from within a JAX-RS component class (e.g. resource, filter, provider&nbsp;&hellip;&nbsp;),
     * the value can take a form of absolute or relative URI.
     * A relative URI is resolved using the context path of the application as the base URI.
     * For example:
     * <pre>
     * public class AuditingFilter implements RequestFilter {
     *    &#64;Uri("audit/logs")
     *    WebTarget applicationLogs;
     *
     *    &#64;Uri("http://sales.acme.com/audit/logs")
     *    WebTarget domainLogs;
     *
     *    ...
     * }
     * </pre>
     *
     * In case the annotation is used from a JAX-RS resource class, an absolute
     * or relative URI template value may be provided. The template parameter (e.g. {@code {id}})
     * values are automatically resolved in the context of the enclosing resource class
     * {@link javax.ws.rs.Path path template} as well as the context of the processed request.
     * Other defined template parameters have to be resolved before invocation of managed web target.
     * For example:
     * <pre>
     * &#64;Path("users/{name}")
     * public class MyResource {
     *    &#64;Uri("users/{name}/orders")
     *    WebTarget userOrders;
     *
     *    &#64;Uri("http://mail.acme.com/accounts/{name}")
     *    WebTarget userEmailAccount;
     *
     *    ...
     * }
     * </pre>
     *
     * @see javax.ws.rs.client.WebTarget
     * @see javax.ws.rs.Path
     */
    String value();
}
