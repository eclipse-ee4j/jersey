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

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.spi.Contract;

/**
 * This contract brings support for transforming an internal representation of entity data filtering feature into an object
 * familiar to underlying provider (e.g. message body worker).
 * <p>
 * This interface is supposed to be implemented by modules providing JAX-RS/Jersey providers / configuration object (e.g. message
 * body workers) that can directly affect reading/writing of an entity.
 * </p>
 * <p>
 * Implementations should be registered into client/server runtime via
 * {@link AbstractBinder jersey binder} (for more information and common implementation see
 * {@link AbstractObjectProvider}):
 * <pre>
 * bindAsContract(MyObjectGraphTransformer.class)
 *       // FilteringGraphTransformer.
 *       .to(new TypeLiteral&lt;ObjectGraphTransformer&lt;MyFilteringObject&gt;&gt;() {})
 *       // Scope.
 *       .in(Singleton.class);
 * </pre>
 * The custom transformer can be then {@link javax.inject.Inject injected} as one these injection point types:
 * <ul>
 * <li>{@code MyObjectGraphTransformer}</li>
 * <li>{@code javax.inject.Provider&lt;ObjectGraphTransformer&lt;MyFilteringObject&gt;&gt;}</li>
 * </ul>
 * </p>
 * <p>
 * By default a {@code ObjectGraph} -&gt; {@code ObjectGraph} transformer is available in the runtime. This transformer can be
 * injected (via {@link javax.inject.Inject @Inject}) into the following types:
 * <ul>
 * <li>{@code ObjectGraphTransformer}</li>
 * <li>{@code javax.inject.Provider&lt;ObjectGraphTransformer&lt;Object&gt;&gt;}</li>
 * <li>{@code javax.inject.Provider&lt;ObjectGraphTransformer&lt;ObjectGraph&gt;&gt;}</li>
 * </ul>
 * </p>
 *
 * @param <T> representation of entity data filtering requested by provider.
 * @author Michal Gajdos
 * @see AbstractObjectProvider
 * @see ObjectProvider
 */
@Contract
public interface ObjectGraphTransformer<T> {

    /**
     * Transform a given graph into an entity-filtering object. The entire graph (incl. it's subgraphs) should be processed by
     * this method as this method is invoked only once for a root entity class.
     *
     * @param graph object graph to be transformed.
     * @return entity-filtering object requested by provider.
     */
    public T transform(final ObjectGraph graph);
}
