/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.QueryParam;

/**
 * Model which contains information about query parameter
 *
 * @author David Kral
 * @author Tomas Langer
 */
class QueryParamModel extends ParamModel<Map<String, Object[]>> {

    private final String queryParamName;

    QueryParamModel(Builder builder, QueryParam annotation) {
        super(builder);
        queryParamName = annotation.value();
    }

    @Override
    public Map<String, Object[]> handleParameter(Map<String, Object[]> requestPart,
                                                 Class<? extends Annotation> annotationClass,
                                                 Object instance) {
        Object resolvedValue = interfaceModel.resolveParamValue(instance, parameter);
        if (resolvedValue instanceof Object[]) {
            requestPart.put(queryParamName, (Object[]) resolvedValue);
        } else if (resolvedValue instanceof Collection) {
            requestPart.put(queryParamName, ((Collection) resolvedValue).toArray());
        } else {
            requestPart.put(queryParamName, new Object[]{resolvedValue});
        }
        return requestPart;
    }

    @Override
    public boolean handles(Class<? extends Annotation> annotation) {
        return QueryParam.class.equals(annotation);
    }

}
