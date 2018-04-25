/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spi.internal;

import java.util.function.Function;
import java.util.function.Supplier;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;

/**
 * Extends {@link Supplier} interface with
 * {@link org.glassfish.jersey.server.model.Parameter.Source} information.
 *
 * @param <T> This must be the type of entity for which this is a factory.
 * @author Petr Bouda
 */
public final class ParamValueFactoryWithSource<T> implements Function<ContainerRequest, T> {

    private final Function<ContainerRequest, T> parameterFunction;
    private final Parameter.Source parameterSource;

    /**
     * Wrap provided param supplier.
     *
     * @param paramFunction   param supplier to be wrapped.
     * @param parameterSource param source.
     */
    public ParamValueFactoryWithSource(Function<ContainerRequest, T> paramFunction, Parameter.Source parameterSource) {
        this.parameterFunction = paramFunction;
        this.parameterSource = parameterSource;
    }

    @Override
    public T apply(ContainerRequest request) {
        return parameterFunction.apply(request);
    }

    /**
     * Returns {@link org.glassfish.jersey.server.model.Parameter.Source}
     * which closely determines a function of the current supplier.
     *
     * @return Source which a given parameter belongs to.
     **/
    public Parameter.Source getSource() {
        return parameterSource;
    }

}
