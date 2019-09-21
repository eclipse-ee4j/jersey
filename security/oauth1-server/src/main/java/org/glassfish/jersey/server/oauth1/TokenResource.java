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

package org.glassfish.jersey.server.oauth1;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be placed on resource classes or resource methods
 * that should be ignored by {@link OAuth1ServerFilter OAuth server filter}.
 * Filter will not perform authentication and authorization check for resources with this annotation and will
 * leave the {@link javax.ws.rs.core.SecurityContext} unchanged. This annotation is intended to be placed
 * on resource providing Request and Access Token during Authorization process.
 * <p/>
 * The annotation can be placed on a resource method directly or resource class.
 *
 * @author Miroslav Fuksa
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TokenResource {
}
