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

package org.glassfish.jersey.internal.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * {@link Qualifier Qualifier annotation} used to annotate HK2 injections and
 * bindings for user custom providers. Providers are classes which implement one
 * of the provider interfaces (for example {@link javax.ws.rs.ext.MessageBodyReader
 * Message body reader interface}).
 * <p>
 * Custom providers are bound in the HK2 injection manager using {@code &#64;Custom}
 * annotation. Once bound, the custom providers can be injected using {@code &#64;Custom}
 * qualifier annotation again.
 * </p>
 * <p>
 * For example:
 * <pre>
 *  &#064;Inject
 *  &#064;Custom
 *  MessageBodyReader messageBodyReader;
 * </pre>
 * </p>
 *
 * @author Miroslav Fuksa
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @see org.glassfish.jersey.internal.inject.CustomAnnotationLiteral
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Custom {

}
