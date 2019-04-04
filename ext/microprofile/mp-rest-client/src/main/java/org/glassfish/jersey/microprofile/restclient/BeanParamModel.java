/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Contains information about method parameter or class field which is annotated by {@link BeanParam}.
 *
 * @author David Kral
 */
class BeanParamModel extends ParamModel<Object> {

    private BeanClassModel beanClassModel;

    BeanParamModel(Builder builder) {
        super(builder);
        beanClassModel = BeanClassModel.fromClass(interfaceModel, (Class<?>) getType());
    }

    @Override
    public Object handleParameter(Object requestPart, Class<?> annotationClass, Object instance) {
        if (PathParam.class.equals(annotationClass)) {
            return beanClassModel.resolvePath((WebTarget) requestPart, instance);
        } else if (HeaderParam.class.equals(annotationClass)) {
            return beanClassModel.resolveHeaders((MultivaluedMap<String, Object>) requestPart, instance);
        } else if (CookieParam.class.equals(annotationClass)) {
            return beanClassModel.resolveCookies((Map<String, String>) requestPart, instance);
        } else if (QueryParam.class.equals(annotationClass)) {
            return beanClassModel.resolveQuery((Map<String, Object[]>) requestPart, instance);
        } else if (MatrixParam.class.equals(annotationClass)) {
            return beanClassModel.resolveMatrix((WebTarget) requestPart, instance);
        } else if (FormParam.class.equals(annotationClass)) {
            return beanClassModel.resolveForm((Form) requestPart, instance);
        }
        throw new UnsupportedOperationException(annotationClass.getName() + " is not supported!");
    }

    @Override
    public boolean handles(Class<Annotation> annotation) {
        return PathParam.class.equals(annotation)
                || HeaderParam.class.equals(annotation)
                || CookieParam.class.equals(annotation)
                || QueryParam.class.equals(annotation)
                || MatrixParam.class.equals(annotation)
                || FormParam.class.equals(annotation);
    }

    /**
     * Returns {@link List} of all parameters annotated by searched annotation.
     *
     * @param paramAnnotation searched annotation
     * @return filtered list
     */
    List<ParamModel> getAllParamsWithType(Class<? extends Annotation> paramAnnotation) {
        return beanClassModel.getParameterModels().stream()
                .filter(paramModel -> paramModel.handles(paramAnnotation))
                .collect(Collectors.toList());
    }

}
