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

package org.glassfish.jersey.moxy.json.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.glassfish.jersey.message.filtering.spi.ObjectProvider;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.ObjectGraph;

/**
 * Entity Data Filtering provider based on MOXy JSON provider.
 *
 * @author Michal Gajdos
 */
@Singleton
public class FilteringMoxyJsonProvider extends ConfigurableMoxyJsonProvider {

    @Inject
    private Provider<ObjectProvider<ObjectGraph>> provider;

    @Override
    protected void preWriteTo(final Object object, final Class<?> type, final Type genericType, final Annotation[] annotations,
                              final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
                              final Marshaller marshaller) throws JAXBException {
        super.preWriteTo(object, type, genericType, annotations, mediaType, httpHeaders, marshaller);

        // Entity Filtering.
        if (marshaller.getProperty(MarshallerProperties.OBJECT_GRAPH) == null) {
            final Object objectGraph = provider.get().getFilteringObject(genericType, true, annotations);

            if (objectGraph != null) {
                marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, objectGraph);
            }
        }
    }

    @Override
    protected void preReadFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders,
                               final Unmarshaller unmarshaller) throws JAXBException {
        super.preReadFrom(type, genericType, annotations, mediaType, httpHeaders, unmarshaller);

        // Entity Filtering.
        if (unmarshaller.getProperty(MarshallerProperties.OBJECT_GRAPH) == null) {
            final Object objectGraph = provider.get().getFilteringObject(genericType, false, annotations);

            if (objectGraph != null) {
                unmarshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, objectGraph);
            }
        }
    }
}
