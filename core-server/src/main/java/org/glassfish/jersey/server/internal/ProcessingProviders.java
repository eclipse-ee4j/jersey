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

package org.glassfish.jersey.server.internal;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.model.internal.RankedProvider;

/**
 * Injectable encapsulating class containing processing providers like filters, interceptors,
 * name bound providers, dynamic features.
 *
 * @author Miroslav Fuksa
 */
public class ProcessingProviders {

    private final MultivaluedMap<Class<? extends Annotation>, RankedProvider<ContainerRequestFilter>> nameBoundRequestFilters;
    private final MultivaluedMap<Class<? extends Annotation>, RankedProvider<ContainerResponseFilter>> nameBoundResponseFilters;
    private final MultivaluedMap<Class<? extends Annotation>, RankedProvider<ReaderInterceptor>> nameBoundReaderInterceptors;
    private final MultivaluedMap<Class<? extends Annotation>, RankedProvider<WriterInterceptor>> nameBoundWriterInterceptors;
    private final MultivaluedMap<RankedProvider<ContainerRequestFilter>, Class<? extends Annotation>>
            nameBoundRequestFiltersInverse;
    private final MultivaluedMap<RankedProvider<ContainerResponseFilter>, Class<? extends Annotation>>
            nameBoundResponseFiltersInverse;
    private final MultivaluedMap<RankedProvider<ReaderInterceptor>, Class<? extends Annotation>>
            nameBoundReaderInterceptorsInverse;
    private final MultivaluedMap<RankedProvider<WriterInterceptor>, Class<? extends Annotation>>
            nameBoundWriterInterceptorsInverse;
    private final Iterable<RankedProvider<ContainerRequestFilter>> globalRequestFilters;
    private final Iterable<ContainerRequestFilter> sortedGlobalRequestFilters;
    private final List<RankedProvider<ContainerRequestFilter>> preMatchFilters;
    private final Iterable<RankedProvider<ContainerResponseFilter>> globalResponseFilters;
    private final Iterable<ContainerResponseFilter> sortedGlobalResponseFilters;

    private final Iterable<RankedProvider<ReaderInterceptor>> globalReaderInterceptors;
    private final Iterable<ReaderInterceptor> sortedGlobalReaderInterceptors;
    private final Iterable<RankedProvider<WriterInterceptor>> globalWriterInterceptors;
    private final Iterable<WriterInterceptor> sortedGlobalWriterInterceptors;
    private final Iterable<DynamicFeature> dynamicFeatures;

    /**
     * Creates new instance of the processing providers.
     *
     * @param nameBoundRequestFilters Name bound {@link ContainerRequestFilter request filters}.
     * @param nameBoundRequestFiltersInverse Inverse map with name bound  {@link ContainerRequestFilter request filters}.
     * @param nameBoundResponseFilters Name bound {@link ContainerResponseFilter response filters}.
     * @param nameBoundResponseFiltersInverse Inverse map with name bound {@link ContainerResponseFilter response filters}.
     * @param nameBoundReaderInterceptors Name bound {@link ReaderInterceptor reader interceptors}.
     * @param nameBoundReaderInterceptorsInverse Inverse map with name bound {@link ReaderInterceptor reader interceptors}.
     * @param nameBoundWriterInterceptors Name bound {@link WriterInterceptor writer interceptors}.
     * @param nameBoundWriterInterceptorsInverse Inverse map with name bound {@link WriterInterceptor writer interceptors}.
     * @param globalRequestFilters Global {@link ContainerRequestFilter request filters}.
     * @param preMatchFilters {@link javax.ws.rs.container.PreMatching Pre-matching}
     *                          {@link ContainerRequestFilter request filters}.
     * @param globalResponseFilters Global {@link ContainerResponseFilter response filters}.
     * @param globalReaderInterceptors Global {@link ReaderInterceptor reader interceptors}.
     * @param globalWriterInterceptors Global {@link WriterInterceptor writer interceptors}.
     * @param dynamicFeatures {@link DynamicFeature Dynamic features}.
     */
    public ProcessingProviders(
            MultivaluedMap<Class<? extends Annotation>, RankedProvider<ContainerRequestFilter>> nameBoundRequestFilters,
            MultivaluedMap<RankedProvider<ContainerRequestFilter>, Class<? extends Annotation>> nameBoundRequestFiltersInverse,
            MultivaluedMap<Class<? extends Annotation>, RankedProvider<ContainerResponseFilter>> nameBoundResponseFilters,
            MultivaluedMap<RankedProvider<ContainerResponseFilter>, Class<? extends Annotation>> nameBoundResponseFiltersInverse,
            MultivaluedMap<Class<? extends Annotation>, RankedProvider<ReaderInterceptor>> nameBoundReaderInterceptors,
            MultivaluedMap<RankedProvider<ReaderInterceptor>, Class<? extends Annotation>> nameBoundReaderInterceptorsInverse,
            MultivaluedMap<Class<? extends Annotation>, RankedProvider<WriterInterceptor>> nameBoundWriterInterceptors,
            MultivaluedMap<RankedProvider<WriterInterceptor>, Class<? extends Annotation>> nameBoundWriterInterceptorsInverse,
            Iterable<RankedProvider<ContainerRequestFilter>> globalRequestFilters,
            List<RankedProvider<ContainerRequestFilter>> preMatchFilters,
            Iterable<RankedProvider<ContainerResponseFilter>> globalResponseFilters,
            Iterable<RankedProvider<ReaderInterceptor>> globalReaderInterceptors,
            Iterable<RankedProvider<WriterInterceptor>> globalWriterInterceptors,
            Iterable<DynamicFeature> dynamicFeatures) {

        this.nameBoundReaderInterceptors = nameBoundReaderInterceptors;
        this.nameBoundReaderInterceptorsInverse = nameBoundReaderInterceptorsInverse;
        this.nameBoundRequestFilters = nameBoundRequestFilters;
        this.nameBoundRequestFiltersInverse = nameBoundRequestFiltersInverse;
        this.nameBoundResponseFilters = nameBoundResponseFilters;
        this.nameBoundResponseFiltersInverse = nameBoundResponseFiltersInverse;
        this.nameBoundWriterInterceptors = nameBoundWriterInterceptors;
        this.nameBoundWriterInterceptorsInverse = nameBoundWriterInterceptorsInverse;
        this.globalRequestFilters = globalRequestFilters;
        this.preMatchFilters = preMatchFilters;
        this.globalResponseFilters = globalResponseFilters;
        this.globalReaderInterceptors = globalReaderInterceptors;
        this.globalWriterInterceptors = globalWriterInterceptors;
        this.dynamicFeatures = dynamicFeatures;
        this.sortedGlobalReaderInterceptors = Providers.sortRankedProviders(new RankedComparator<>(), globalReaderInterceptors);
        this.sortedGlobalWriterInterceptors = Providers.sortRankedProviders(new RankedComparator<>(), globalWriterInterceptors);
        this.sortedGlobalRequestFilters = Providers.sortRankedProviders(new RankedComparator<>(), globalRequestFilters);
        this.sortedGlobalResponseFilters = Providers.sortRankedProviders(new RankedComparator<>(), globalResponseFilters);
    }

    /**
     * Get name bound request filters.
     *
     * @return Name bound {@link ContainerRequestFilter request filter} map. Keys are request filters and
     *         values are {@link javax.ws.rs.NameBinding name bound annotations} attached to these filters.
     */
    public MultivaluedMap<Class<? extends Annotation>, RankedProvider<ContainerRequestFilter>> getNameBoundRequestFilters() {
        return nameBoundRequestFilters;
    }

    /**
     * Get name bound request filter inverse map.
     *
     * @return Name bound {@link ContainerRequestFilter request filter} map. Keys are request filters and
     *         values are {@link javax.ws.rs.NameBinding name bound annotations} attached to these filters.
     */
    public
    MultivaluedMap<RankedProvider<ContainerRequestFilter>, Class<? extends Annotation>> getNameBoundRequestFiltersInverse() {
        return nameBoundRequestFiltersInverse;
    }

    /**
     * Get name bound response filters.
     *
     * @return Name bound {@link ContainerResponseFilter response filter} map. Keys are response filters and
     *         values are {@link javax.ws.rs.NameBinding name bound annotations} attached to these filters.
     */
    public MultivaluedMap<Class<? extends Annotation>, RankedProvider<ContainerResponseFilter>> getNameBoundResponseFilters() {
        return nameBoundResponseFilters;
    }

    /**
     * Get name bound response filter inverse map.
     *
     * @return Name bound {@link ContainerRequestFilter response filter} map. Keys are response filters and
     *         values are {@link javax.ws.rs.NameBinding name bound annotations} attached to these filters.
     */
    public
    MultivaluedMap<RankedProvider<ContainerResponseFilter>, Class<? extends Annotation>> getNameBoundResponseFiltersInverse() {
        return nameBoundResponseFiltersInverse;
    }

    /**
     * Get name bound reader interceptor map.
     *
     * @return Returns Name bound {@link ReaderInterceptor reader interceptor} map. Keys are {@link javax.ws.rs.NameBinding name
     *         bound annotations} and values are providers which are annotated with these annotations.
     */
    public MultivaluedMap<Class<? extends Annotation>, RankedProvider<ReaderInterceptor>> getNameBoundReaderInterceptors() {
        return nameBoundReaderInterceptors;
    }

    /**
     * Get name bound reader interceptor inverse map.
     *
     * @return Name bound {@link ReaderInterceptor reader interceptor} map. Keys are reader interceptors and
     *         values are {@link javax.ws.rs.NameBinding name bound annotations} attached to these interceptors.
     */
    public
    MultivaluedMap<RankedProvider<ReaderInterceptor>, Class<? extends Annotation>> getNameBoundReaderInterceptorsInverse() {
        return nameBoundReaderInterceptorsInverse;
    }

    /**
     * Get name bound writer interceptor map.
     *
     * @return Returns Name bound {@link WriterInterceptor writer interceptor} map. Keys are {@link javax.ws.rs.NameBinding name
     *         bound annotations} and values are interceptors which are annotated with these annotations.
     */
    public MultivaluedMap<Class<? extends Annotation>, RankedProvider<WriterInterceptor>> getNameBoundWriterInterceptors() {
        return nameBoundWriterInterceptors;
    }

    /**
     * Get name bound writer interceptor inverse map.
     *
     * @return Name bound {@link WriterInterceptor writer interceptor} map. Keys are reader interceptors and
     *         values are {@link javax.ws.rs.NameBinding name bound annotations} attached to these interceptors.
     */
    public
    MultivaluedMap<RankedProvider<WriterInterceptor>, Class<? extends Annotation>> getNameBoundWriterInterceptorsInverse() {
        return nameBoundWriterInterceptorsInverse;
    }

    /**
     * Get global request filters.
     *
     * @return Global request filter ranked providers.
     */
    public Iterable<RankedProvider<ContainerRequestFilter>> getGlobalRequestFilters() {
        return globalRequestFilters;
    }

    /**
     * Get global response filters.
     *
     * @return Global response filter ranked providers.
     */
    public Iterable<RankedProvider<ContainerResponseFilter>> getGlobalResponseFilters() {
        return globalResponseFilters;
    }

    /**
     * Get global request filters sorted by priority.
     *
     * @return Sorted global request filters.
     */
    public Iterable<ContainerRequestFilter> getSortedGlobalRequestFilters() {
        return sortedGlobalRequestFilters;
    }

    /**
     * Get global response filters sorted by priority.
     *
     * @return Sorted global response filters.
     */
    public Iterable<ContainerResponseFilter> getSortedGlobalResponseFilters() {
        return sortedGlobalResponseFilters;
    }

    /**
     * Get global reader interceptors.
     *
     * @return Global reader interceptors ranked providers.
     */
    public Iterable<RankedProvider<ReaderInterceptor>> getGlobalReaderInterceptors() {
        return globalReaderInterceptors;
    }

    /**
     * Get global writer interceptors.
     *
     * @return Global writer interceptors ranked providers.
     */
    public Iterable<RankedProvider<WriterInterceptor>> getGlobalWriterInterceptors() {
        return globalWriterInterceptors;
    }

    /**
     * Get global reader interceptors sorted by priority.
     *
     * @return Global reader interceptors.
     */
    public Iterable<ReaderInterceptor> getSortedGlobalReaderInterceptors() {
        return sortedGlobalReaderInterceptors;
    }

    /**
     * Get global writer interceptors sorted by priority.
     *
     * @return Global writer interceptors.
     */
    public Iterable<WriterInterceptor> getSortedGlobalWriterInterceptors() {
        return sortedGlobalWriterInterceptors;
    }

    /**
     * Get dynamic features.
     *
     * @return Dynamic features.
     */
    public Iterable<DynamicFeature> getDynamicFeatures() {
        return dynamicFeatures;
    }

    /**
     * Get {@link javax.ws.rs.container.PreMatching pre-matching} request filters.
     * @return Pre-matching request filter ranked providers.
     */
    public List<RankedProvider<ContainerRequestFilter>> getPreMatchFilters() {
        return preMatchFilters;
    }
}
