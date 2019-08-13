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
import java.util.IdentityHashMap;
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
 * @author Tomas Langer
 */
class BeanParamModel extends ParamModel<Object> {

    private static final Map<Class<?>, ParamHandler> PARAM_HANDLERS = new IdentityHashMap<>();

    static {
        PARAM_HANDLERS.put(PathParam.class,
                           (model, requestPart, instance) -> model.resolvePath((WebTarget) requestPart, instance));
        PARAM_HANDLERS.put(HeaderParam.class,
                           (model, requestPart, instance) -> model.resolveHeaders((MultivaluedMap<String, Object>) requestPart,
                                                                                  instance));
        PARAM_HANDLERS.put(CookieParam.class,
                           (model, requestPart, instance) -> model.resolveCookies((Map<String, String>) requestPart, instance));
        PARAM_HANDLERS.put(QueryParam.class,
                           (model, requestPart, instance) -> model.resolveQuery((Map<String, Object[]>) requestPart,
                                                                                  instance));
        PARAM_HANDLERS.put(MatrixParam.class,
                           (model, requestPart, instance) -> model.resolveMatrix((WebTarget) requestPart, instance));
        PARAM_HANDLERS.put(FormParam.class,
                           (model, requestPart, instance) -> model.resolveForm((Form) requestPart,
                                                                                  instance));
    }

    private BeanClassModel beanClassModel;

    BeanParamModel(Builder builder) {
        super(builder);
        beanClassModel = BeanClassModel.fromClass(interfaceModel, (Class<?>) getType());
    }

    @Override
    public Object handleParameter(Object requestPart, Class<? extends Annotation> annotationClass, Object instance) {
        ParamHandler handler = PARAM_HANDLERS.get(annotationClass);

        if (null == handler) {
            throw new UnsupportedOperationException(annotationClass.getName() + " is not supported!");
        }

        return handler.handle(beanClassModel, requestPart, instance);
    }

    @Override
    public boolean handles(Class<? extends Annotation> annotation) {
        return PARAM_HANDLERS.containsKey(annotation);
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
