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

package org.glassfish.jersey.tests.integration.jersey3670;

import javax.annotation.Priority;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Priority(0)
public class MyConverterProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        if (rawType.equals(Integer.class)) {

            /*
             * Dummy ParamConverter which always returns "42".
             */
            return (ParamConverter<T>) new ParamConverter<Integer>() {
                @Override
                public Integer fromString(String value) {
                    return 42;
                }

                @Override
                public String toString(Integer value) {
                    throw new UnsupportedOperationException();
                }
            };

        }
        return null;
    }
}