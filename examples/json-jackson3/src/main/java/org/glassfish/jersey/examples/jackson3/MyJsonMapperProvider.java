/*
 * Copyright (c) 2010, 2022, 2026 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson3;

import tools.jackson.databind.json.JsonMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;

/**
 * TODO javadoc.
 *
 * @author Jakub Podlesak
 */
@Provider
public class MyJsonMapperProvider implements ContextResolver<JsonMapper> {

    final JsonMapper defaultJsonMapper;
    final JsonMapper combinedJsonMapper;

    public MyJsonMapperProvider() {
        defaultJsonMapper = createDefaultMapper();
        combinedJsonMapper = createCombinedMapper();
    }

    @Override
    public JsonMapper getContext(final Class<?> type) {

        if (type == CombinedAnnotationBean.class) {
            return combinedJsonMapper;
        } else {
            return defaultJsonMapper;
        }
    }

    private static JsonMapper createCombinedMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.WRAP_ROOT_VALUE)
                .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .annotationIntrospector(createJaxbJacksonAnnotationIntrospector())
                .build();
    }

    private static JsonMapper createDefaultMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    private static AnnotationIntrospector createJaxbJacksonAnnotationIntrospector() {

        final AnnotationIntrospector jaxbIntrospector = new JakartaXmlBindAnnotationIntrospector();
        final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();

        return AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
    }
}
