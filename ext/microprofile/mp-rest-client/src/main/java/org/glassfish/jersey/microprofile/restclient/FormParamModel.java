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
import java.util.Collection;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Form;

/**
 * Contains information about method parameter or class field which is annotated by {@link FormParam}.
 *
 * @author David Kral
 */
class FormParamModel extends ParamModel<Form> {

    private final String formParamName;

    FormParamModel(Builder builder) {
        super(builder);
        formParamName = builder.formParamName();
    }

    @Override
    Form handleParameter(Form form, Class<?> annotationClass, Object instance) {
        Object resolvedValue = interfaceModel.resolveParamValue(instance, parameter);
        if (resolvedValue instanceof Collection) {
            for (final Object v : ((Collection) resolvedValue)) {
                form.param(formParamName, v.toString());
            }
        } else {
            form.param(formParamName, resolvedValue.toString());
        }
        return form;
    }

    @Override
    boolean handles(Class<Annotation> annotation) {
        return FormParam.class.equals(annotation);
    }

}
