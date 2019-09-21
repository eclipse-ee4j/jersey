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

package org.glassfish.jersey.message.filtering;

import javax.ws.rs.core.GenericType;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.message.filtering.spi.EntityGraphProvider;
import org.glassfish.jersey.message.filtering.spi.EntityInspector;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;
import org.glassfish.jersey.message.filtering.spi.ObjectGraphTransformer;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

/**
 * Binder for Entity Data Filtering feature.
 *
 * @author Michal Gajdos
 */
final class EntityFilteringBinder extends AbstractBinder {

    @Override
    protected void configure() {
        // Entity Inspector.
        bind(EntityInspectorImpl.class)
                .to(EntityInspector.class)
                .in(Singleton.class);

        // Entity Graph Provider.
        bind(EntityGraphProviderImpl.class)
                .to(EntityGraphProvider.class)
                .in(Singleton.class);

        // Object Provider & Object Graph Transformer.
        bindAsContract(ObjectGraphProvider.class)
                // FilteringObjectProvider.
                .to(ObjectProvider.class)
                .to(new GenericType<ObjectProvider<Object>>() {})
                .to(new GenericType<ObjectProvider<ObjectGraph>>() {})
                // FilteringGraphTransformer.
                .to(ObjectGraphTransformer.class)
                .to(new GenericType<ObjectGraphTransformer<Object>>() {})
                .to(new GenericType<ObjectGraphTransformer<ObjectGraph>>() {})
                // Scope.
                .in(Singleton.class);
    }
}
