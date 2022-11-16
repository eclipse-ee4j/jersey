/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.ext.cdi1x.inject.internal;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *  Qualifier used for injecting the CDI beans using {@code @Inject}. Used only for beans that originate in Specification other
 *  than Jakarta RESTful Web Services (such as Servlet). Such beans must not be produced with {@code @Default} qualifier so that
 *  there is no ambiguity for the CDI injection.
 * </p>
 * <p>
 *     Jakarta REST Spec. Section 11 demands {@code HttpServletRequest}, {@code HttpServletResponse}, {@code ServletContext},
 *     {@code ServletConfig}, and {@code FilterConfig} to be available by injections using {@code @Context}. For CDI, these
 *     servlet classes are available with {@link JerseyContext} qualifier.
 * </p>
 * <p>
 *     Note that {@code @Context} injection is not aware of the qualifier and using {@code &#64;Context} in conjuction with
 *     {@code &#64;JerseyContext} will not work.
 * </p>
 * <p>
 *  Can be used as e.g.
 *
 * <pre>
 * &#64;Inject
 * &#64;JerseyContext //internal
 * HttpServletRequest httpServletRequest;
 * </pre>
 * or as iterable of all {@code HttpServletRequest} beans
 * <pre>
 * &#64;Inject
 * &#64;Any
 * Instance&lt;HttpServletRequest&gt; httpServletRequests;
 * </pre>
 * </p>
 * @since 2.38
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
public @interface JerseyContext {
}
