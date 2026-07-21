/*
 * Copyright (c) 2015, 2023, 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson3.internal;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.Providers;

import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.EndpointConfigBase;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectWriterInjector;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.cfg.ObjectWriterModifier;
import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.json.JsonEndpointConfig;
import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedField;
import tools.jackson.databind.introspect.AnnotatedMethod;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.PropertyFilter;

/**
 * Entity Data Filtering provider based on Jackson JSON provider.
 *
 * @author Michal Gajdos
 */
@Singleton
public final class FilteringJacksonXmlBindJsonProvider extends DefaultJacksonXmlBindJsonProvider {

    private final Provider<ObjectProvider<FilterProvider>> provider;

    @Inject
    public FilteringJacksonXmlBindJsonProvider(@Context Provider<ObjectProvider<FilterProvider>> provider,
                                               @Context Providers providers,
                                               @Context Configuration config) {
        super(providers, config);
        this.provider = provider;
    }


    @Override
    protected JsonEndpointConfig _configForWriting(final JsonMapper mapper, final Annotation[] annotations,
                                                   final Class<?> defaultView) {
        final AnnotationIntrospector customIntrospector = mapper.serializationConfig().getAnnotationIntrospector();
        // Set the custom (user) introspector to be the primary one.
        AnnotationIntrospector forFilteringMapper = AnnotationIntrospector.pair(customIntrospector,
                new JacksonAnnotationIntrospector() {
                    @Override
                    public Object findFilterId(MapperConfig<?> config, final Annotated a) {
                        final Object filterId = super.findFilterId(config, a);

                        if (filterId != null) {
                            return filterId;
                        }

                        if (a instanceof AnnotatedMethod) {
                            final Method method = ((AnnotatedMethod) a).getAnnotated();

                            // Interested only in getters - trying to obtain "field" name from them.
                            if (ReflectionHelper.isGetter(method)) {
                                return ReflectionHelper.getPropertyName(method);
                            }
                        }
                        if (a instanceof AnnotatedField || a instanceof AnnotatedClass) {
                            return a.getName();
                        }

                        return null;
                    }
                });
        final JsonMapper filteringMapper = mapper.rebuild().annotationIntrospector(forFilteringMapper).build();
        return super._configForWriting(filteringMapper, annotations, defaultView);
    }

    @Override
    public void writeTo(final Object value,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) {
        final FilterProvider filterProvider = provider.get().getFilteringObject(genericType, true, annotations);
        if (filterProvider != null) {
            ObjectWriterInjector.set(new FilteringObjectWriterModifier(filterProvider, ObjectWriterInjector.getAndClear()));
        }

        super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    private static final class FilteringObjectWriterModifier extends ObjectWriterModifier {

        private final ObjectWriterModifier original;
        private final FilterProvider filterProvider;

        private FilteringObjectWriterModifier(final FilterProvider filterProvider, final ObjectWriterModifier original) {
            this.original = original;
            this.filterProvider = filterProvider;
        }

        @Override
        public ObjectWriter modify(final EndpointConfigBase<?> endpoint,
                                   final MultivaluedMap<String, Object> responseHeaders,
                                   final Object valueToWrite,
                                   final ObjectWriter w) throws JacksonException {
            final ObjectWriter writer = original == null ? w : original.modify(endpoint, responseHeaders, valueToWrite, w);
            final FilterProvider customFilterProvider = writer.getConfig().getFilterProvider();
//public abstract PropertyFilter findPropertyFilter(SerializationContext ctxt, Object filterId, Object valueToFilter);
            // Try the custom (user) filter provider first.
            return customFilterProvider == null
                    ? writer.with(filterProvider)
                    : writer.with(new FilterProvider() {
                        @Override
                        public FilterProvider snapshot() {
                            return this;
                        }

                        @Override
                        public PropertyFilter findPropertyFilter(SerializationContext ctxt,
                                                                 final Object filterId, final Object valueToFilter) {
                            final PropertyFilter filter = customFilterProvider.findPropertyFilter(ctxt, filterId, valueToFilter);
                            if (filter != null) {
                                return filter;
                            }

                            return filterProvider.findPropertyFilter(ctxt, filterId, valueToFilter);
                        }
                    });
        }
    }
}
