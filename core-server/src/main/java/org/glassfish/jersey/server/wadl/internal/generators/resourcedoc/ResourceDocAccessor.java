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

package org.glassfish.jersey.server.wadl.internal.generators.resourcedoc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.AnnotationDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ClassDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.MethodDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.NamedValueType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ParamDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.RepresentationDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ResourceDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ResponseDocType;

/**
 * A class providing access to information stored in a {@link ResourceDocType}.<br>
 * Created on: Jun 16, 2008<br>
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public class ResourceDocAccessor {

    private static final Logger LOGGER = Logger.getLogger(ResourceDocAccessor.class.getName());

    private ResourceDocType _resourceDoc;

    public ResourceDocAccessor(ResourceDocType resourceDoc) {
        _resourceDoc = resourceDoc;
    }

    public ClassDocType getClassDoc(Class<?> resourceClass) {
        if (resourceClass == null) {
            return null;
        }

        for (ClassDocType classDocType : _resourceDoc.getDocs()) {
            if (resourceClass.getName().equals(classDocType.getClassName())) {
                return classDocType;
            }
        }
        return null;
    }

    public MethodDocType getMethodDoc(Class<?> resourceClass, Method method) {
        if (resourceClass == null || method == null) {
            return null;
        }

        final ClassDocType classDoc = getClassDoc(resourceClass);
        if (classDoc == null) {
            return null;
        }

        MethodDocType candidate = null;
        int candidateCount = 0;

        final String methodName = method.getName();
        final String methodSignature = computeSignature(method);
        for (MethodDocType methodDocType : classDoc.getMethodDocs()) {
            if (methodName.equals(methodDocType.getMethodName())) {
                candidateCount++;
                if (candidate == null) {
                    candidate = methodDocType;
                }
                final String docMethodSignature = methodDocType.getMethodSignature();
                if (docMethodSignature != null && docMethodSignature.equals(methodSignature)) {
                    return methodDocType;
                }
            }
        }

        if (candidate != null && candidateCount > 1 && LOGGER.isLoggable(Level.CONFIG)) {
            LOGGER.config(LocalizationMessages.WADL_RESOURCEDOC_AMBIGUOUS_METHOD_ENTRIES(
                    resourceClass.getName(),
                    methodName,
                    methodSignature,
                    candidateCount));
        }

        return candidate;
    }

    private String computeSignature(final Method method) {
        final String methodAsString = method.toGenericString();
        return methodAsString.substring(methodAsString.indexOf('('), methodAsString.lastIndexOf(')') + 1);
    }

    /**
     * @param resourceClass
     * @param method
     * @param p
     * @return param doc type
     */
    public ParamDocType getParamDoc(Class<?> resourceClass, Method method,
                                    Parameter p) {
        final MethodDocType methodDoc = getMethodDoc(resourceClass, method);
        if (methodDoc != null) {
            for (ParamDocType paramDocType : methodDoc.getParamDocs()) {
                for (AnnotationDocType annotationDocType : paramDocType.getAnnotationDocs()) {
                    final Class<? extends Annotation> annotationType = p.getSourceAnnotation().annotationType();
                    if (annotationType != null) {
                        final String sourceName = getSourceName(annotationDocType);
                        if (sourceName != null && sourceName.equals(p.getSourceName())) {
                            return paramDocType;
                        }
                    }
                }
            }
        }
        return null;
    }

    public RepresentationDocType getRequestRepresentation(Class<?> resourceClass, Method method, String mediaType) {
        if (mediaType == null) {
            return null;
        }
        final MethodDocType methodDoc = getMethodDoc(resourceClass, method);
        return methodDoc != null
                && methodDoc.getRequestDoc() != null
                && methodDoc.getRequestDoc().getRepresentationDoc() != null
                // && mediaType.equals( methodDoc.getRequestDoc().getRepresentationDoc().getMediaType() )
                ? methodDoc.getRequestDoc().getRepresentationDoc() : null;
    }

    public ResponseDocType getResponse(Class<?> resourceClass, Method method) {
        final MethodDocType methodDoc = getMethodDoc(resourceClass, method);
        return methodDoc != null && methodDoc.getResponseDoc() != null
                ? methodDoc.getResponseDoc() : null;
    }

    private String getSourceName(AnnotationDocType annotationDocType) {
        if (annotationDocType.hasAttributeDocs()) {
            for (NamedValueType namedValueType : annotationDocType.getAttributeDocs()) {
                /* the value of the "value"-attribute is the param.sourceName...
                 */
                if ("value".equals(namedValueType.getName())) {
                    return namedValueType.getValue();
                }
            }
        }
        return null;
    }

}
