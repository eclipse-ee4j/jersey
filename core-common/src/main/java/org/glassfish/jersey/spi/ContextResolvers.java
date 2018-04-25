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

package org.glassfish.jersey.spi;

import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;

/**
 * An injectable interface providing look-up for {@link ContextResolver ContextResolver&lt;T&gt;}
 * provider instances.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface ContextResolvers {

    /**
     * Get {@link ContextResolver ContextResolver&lt;T&gt;} instance for a given type
     * and media type.
     *
     * @param <T> Java type produced by the context resolver.
     * @param type type supported by the context resolver.
     * @param mediaType media type supported by the context resolver.
     * @return proper context resolver instance if found, otherwise {@code null}.
     */
    <T> ContextResolver<T> resolve(Type type, MediaType mediaType);
}
