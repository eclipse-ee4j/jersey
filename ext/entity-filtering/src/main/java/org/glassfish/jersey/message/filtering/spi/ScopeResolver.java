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

package org.glassfish.jersey.message.filtering.spi;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.glassfish.jersey.spi.Contract;

/**
 * Class used to resolve entity-filtering scopes from annotations. Annotations passed to {@code #resolve()} method
 * can be one of the following: entity annotations (provided when creating request/response entity),
 * annotations obtained from {@link javax.ws.rs.core.Configuration configuration}, resource method / resource class annotations.
 * <p/>
 * Entity-filtering scope is supposed to be an unique string that can be derived from an annotations and that can be further used
 * in internal entity data filtering structures. Examples of such unique strings are:
 * <ul>
 * <li><code>@MyDetailedView</code> -&gt; <code>my.package.MyDetailedView</code></li>
 * <li>
 * <code>@RolesAllowed({"manager", "user"})</code> -&gt; <code>javax.annotation.security.RolesAllowed_manager</code> and
 * <code>javax.annotation.security.RolesAllowed_user</code>
 * </li>
 * </ul>
 * <p/>
 * {@link ScopeResolver Scope resolvers} are invoked from {@link ScopeProvider scope provider} instance.
 *
 * @author Michal Gajdos
 */
@Contract
public interface ScopeResolver {

    /**
     * Resolve entity-filtering scopes for given annotations.
     *
     * @param annotations list of arbitrary annotations.
     * @return non-null set of entity-filtering scopes.
     */
    public Set<String> resolve(final Annotation[] annotations);
}
