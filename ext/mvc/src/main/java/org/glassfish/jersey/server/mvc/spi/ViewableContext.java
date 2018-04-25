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

package org.glassfish.jersey.server.mvc.spi;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.spi.Contract;

/**
 * The context for resolving an instance of {@link org.glassfish.jersey.server.mvc.Viewable} to an instance of {@link
 * ResolvedViewable}.
 * <p/>
 * Note:
 * {@link ViewableContext#resolveViewable(org.glassfish.jersey.server.mvc.Viewable, javax.ws.rs.core.MediaType, Class, TemplateProcessor)}
 * method may be called multiple times (combination of all the calculated possible media types of the response with all found
 * {@link TemplateProcessor template processors}).
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ViewableContext {

    /**
     * Resolve given {@link org.glassfish.jersey.server.mvc.Viewable viewable} using {@link javax.ws.rs.core.MediaType mediaType},
     * {@code resourceClass} and {@link TemplateProcessor templateProcessor}.
     * <p/>
     * If the template name of the viewable is not absolute then the given {@code resourceClass} may be utilized to resolve
     * the relative template name into an absolute template name.
     * <br/>
     * <ul>
     * {@code resourceClass} contains class of the matched resource.
     * </ul>
     *
     * @param viewable viewable to be resolved.
     * @param mediaType media type the viewable may be transformed into.
     * @param resourceClass resource class.
     * @param templateProcessor template processor to be used.
     * @return resolved viewable or {@code null} if the viewable cannot be resolved.
     */
    public ResolvedViewable resolveViewable(final Viewable viewable, final MediaType mediaType, final Class<?> resourceClass,
                                            final TemplateProcessor templateProcessor);
}
