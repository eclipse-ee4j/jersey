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
import java.util.Collections;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Contains information about method parameter or class field which is annotated by {@link HeaderParam}.
 *
 * @author David Kral
 * @author Tomas Langer
 */
class HeaderParamModel extends ParamModel<MultivaluedMap<String, Object>> {

    private String headerParamName;

    HeaderParamModel(Builder builder, HeaderParam annotation) {
        super(builder);
        this.headerParamName = annotation.value();
    }

    @Override
    MultivaluedMap<String, Object> handleParameter(MultivaluedMap<String, Object> requestPart,
                                                          Class<? extends Annotation> annotationClass, Object instance) {
        Object resolvedValue = interfaceModel.resolveParamValue(instance, parameter);
        requestPart.put(headerParamName, Collections.singletonList(resolvedValue));
        return requestPart;
    }

    @Override
    boolean handles(Class<? extends Annotation> annotation) {
        return HeaderParam.class.equals(annotation);
    }
}
