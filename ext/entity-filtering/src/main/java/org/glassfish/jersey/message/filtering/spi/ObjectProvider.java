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
import java.lang.reflect.Type;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.spi.Contract;

/**
 * Entry point of Entity Data Filtering feature for providers working with custom entities and media types (reading/writing).
 * Exposed methods are supposed to provide an entity-filtering object of defined type (generic parameter type
 * {@code &lt;T&gt;}) for given types/classes that is requested by underlying provider (e.g. message body worker).
 * <p>
 * Methods are also accepting a list of entity annotations which entity-filtering scopes and then particular entity-filtering
 * object are determined from. Entity annotations can be passed to the runtime via:
 * <ul>
 * <li>{@link javax.ws.rs.client.Entity#entity(Object, javax.ws.rs.core.MediaType, java.lang.annotation.Annotation[])} on the
 * client, or</li>
 * <li>{@link javax.ws.rs.core.Response.ResponseBuilder#entity(Object, java.lang.annotation.Annotation[])} on the server</li>
 * </ul>
 * </p>
 * <p>
 * Custom implementations should, during processing, look up for available {@link EntityProcessor entity processors} to examine
 * given entity classes and {@link ScopeResolver scope providers} to determine the current entity-filtering scope. Entity class
 * and entity-filtering scopes determine the {@link ObjectGraph object graph} passed to {@link ObjectGraphTransformer object graph
 * transformer} and hence the resulting entity-filtering object.
 * </p>
 * <p>
 * Implementations should be registered into client/server runtime via
 * {@link AbstractBinder jersey binder} (for more information and common implementation see
 * {@link AbstractObjectProvider}):
 * <pre>
 * bindAsContract(MyObjectProvider.class)
 *       // FilteringGraphTransformer.
 *       .to(new TypeLiteral&lt;ObjectGraphTransformer&lt;MyFilteringObject&gt;&gt;() {})
 *       // Scope.
 *       .in(Singleton.class);
 * </pre>
 * The custom provider can be then {@link javax.inject.Inject injected} as one these injection point types:
 * <ul>
 * <li>{@code MyObjectProvider}</li>
 * <li>{@code javax.inject.Provider&lt;ObjectProvider&lt;MyFilteringObject&gt;&gt;}</li>
 * </ul>
 * </p>
 * <p>
 * By default a {@code ObjectGraph} provider is available in the runtime. This object provider can be injected (via
 * {@link javax.inject.Inject @Inject}) into the following types:
 * <ul>
 * <li>{@code ObjectProvider}</li>
 * <li>{@code javax.inject.Provider&lt;ObjectProvider&lt;Object&gt;&gt;}</li>
 * <li>{@code javax.inject.Provider&lt;ObjectProvider&lt;ObjectGraph&gt;&gt;}</li>
 * </ul>
 * </p>
 * <p>
 * Note: For most of the cases it is sufficient that users implement {@link ObjectGraphTransformer object graph transformer} by
 * extending {@link AbstractObjectProvider} class.
 * </p>
 *
 * @param <T> representation of entity data filtering requested by provider.
 * @author Michal Gajdos
 * @see AbstractObjectProvider
 * @see ObjectGraphTransformer
 */
@Contract
public interface ObjectProvider<T> {

    /**
     * Get reader/writer entity-filtering object for given type.
     *
     * @param genericType type for which the object is requested.
     * @param forWriter flag to determine whether to create object for reading/writing purposes.
     * @param annotations entity annotations to determine the runtime scope.
     * @return entity-filtering object.
     */
    public T getFilteringObject(Type genericType, boolean forWriter, final Annotation... annotations);
}
