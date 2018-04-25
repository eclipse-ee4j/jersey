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

package org.glassfish.jersey.server.internal.inject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * A parameter value provider that provides parameter value factories
 * which are using {@link MultivaluedParameterExtractorProvider} to extract parameter
 * values from the supplied {@link javax.ws.rs.core.MultivaluedMap multivalued
 * parameter map}.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class AbstractValueParamProvider implements ValueParamProvider {

    private final Provider<MultivaluedParameterExtractorProvider> mpep;
    private final Set<Parameter.Source> compatibleSources;

    /**
     * Initialize the provider.
     *
     * @param mpep              multivalued map parameter extractor provider.
     * @param compatibleSources compatible parameter sources.
     */
    protected AbstractValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep,
                                            Parameter.Source... compatibleSources) {
        this.mpep = mpep;
        this.compatibleSources = new HashSet<>(Arrays.asList(compatibleSources));
    }

    /**
     * Get a parameter extractor.
     * The extractor returned from this method will use the default value
     * set on the parameter, in case the parameter is not found in the supplied
     * {@link javax.ws.rs.core.MultivaluedMap multivalued parameter map}.
     *
     * @param parameter parameter supported by the returned extractor.
     * @return extractor supporting the parameter. The returned instance ignores
     * any default values set on the parameter.
     */
    protected final MultivaluedParameterExtractor<?> get(Parameter parameter) {
        return mpep.get().get(parameter);
    }

    /**
     * Create a value provider for the parameter. May return {@code null} in case
     * the parameter is not supported by the value provider.
     *
     * @param parameter       parameter requesting the value provider instance.
     * @return parameter value supplier. Returns {@code null} if parameter is not supported.
     */
    protected abstract Function<ContainerRequest, ?> createValueProvider(Parameter parameter);

    /**
     * Get an injected value provider for the parameter. May return {@code null}
     * in case the parameter is not supported by the value provider.
     *
     * @param parameter parameter requesting the value provider instance.
     * @return injected parameter value supplier. Returns {@code null} if parameter
     * is not supported.
     */
    @Override
    public final Function<ContainerRequest, ?> getValueProvider(Parameter parameter) {
        if (!compatibleSources.contains(parameter.getSource())) {
            // not compatible
            return null;
        }
        return createValueProvider(parameter);
    }

    @Override
    public PriorityType getPriority() {
        return Priority.NORMAL;
    }
}
