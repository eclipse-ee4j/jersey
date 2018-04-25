/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.PathParam;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Checks that Parameters work fine with multiple annotations.
 */
public class ParameterWithMultipleAnnotationsTest {

    @Test
    public void testParametersWithMultiple() throws Exception {
        checkMyResourceMethod("processTrailingUnknown");
        checkMyResourceMethod("processLeadingUnknown");
        checkMyResourceMethod("processLeadingAndTrailingUnknown");
        checkMyResourceMethod("processSingleUnknown");
        checkMyResourceMethod("processDoubleUnknown");
    }

    private void checkMyResourceMethod(String methodName) throws Exception {
        final Method method = MyResource.class.getMethod(methodName, String.class);
        final List<Parameter> parameters = Parameter.create(MyResource.class, MyResource.class, method, false);

        assertEquals(1, parameters.size());

        final Parameter parameter = parameters.get(0);
        assertEquals(methodName, String.class, parameter.getRawType());
        assertEquals(methodName, String.class, parameter.getType());
        assertEquals(methodName, "correct", parameter.getSourceName());
    }

    @Target({java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.METHOD,
            java.lang.annotation.ElementType.FIELD})
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface LeadAnnotation {

        String value() default "lead";
    }

    @Target({java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.METHOD,
            java.lang.annotation.ElementType.FIELD})
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface TrailAnnotation {

        String value() default "trail";
    }

    private static class MyResource {

        public void processTrailingUnknown(@PathParam("correct") @TrailAnnotation String id) {
        }

        public void processLeadingUnknown(@LeadAnnotation @PathParam("correct") String id) {
        }

        public void processLeadingAndTrailingUnknown(@LeadAnnotation @PathParam("correct") @TrailAnnotation String id) {
        }

        public void processSingleUnknown(@LeadAnnotation("correct") String id) {
        }

        public void processDoubleUnknown(@LeadAnnotation @TrailAnnotation("correct") String id) {
        }
    }
}
