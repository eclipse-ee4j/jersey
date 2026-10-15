/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.jersey.server.validation.internal;

import java.lang.reflect.Method;

import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that {@link ValidateOnExecutionHandler} takes the superclasses of a resource class into account.
 */
public class ValidateOnExecutionHandlerTest {

    public static class AnnotatedMethodBase {

        @ValidateOnExecution(type = ExecutableType.NONE)
        public String post(final String value) {
            return value;
        }
    }

    public static class OverridingResource extends AnnotatedMethodBase {

        @Override
        public String post(final String value) {
            return value;
        }
    }

    public static class InheritingResource extends AnnotatedMethodBase {
    }

    public static class AnnotatedOverridingResource extends AnnotatedMethodBase {

        @Override
        @ValidateOnExecution(type = ExecutableType.ALL)
        public String post(final String value) {
            return value;
        }
    }

    @ValidateOnExecution(type = ExecutableType.NONE)
    public static class AnnotatedClassBase {

        public String post(final String value) {
            return value;
        }
    }

    public static class OverridingAnnotatedClassResource extends AnnotatedClassBase {

        @Override
        public String post(final String value) {
            return value;
        }
    }

    public static class Base {
    }

    public static class AnnotatedMethodResource extends Base {

        @ValidateOnExecution(type = ExecutableType.NONE)
        public String post(final String value) {
            return value;
        }
    }

    @Test
    public void testAnnotationOnSuperclassMethodApplies() throws Exception {
        assertFalse(validateMethod(OverridingResource.class));
    }

    @Test
    public void testAnnotationOnSuperclassApplies() throws Exception {
        assertFalse(validateMethod(OverridingAnnotatedClassResource.class));
    }

    @Test
    public void testAnnotatedMethodOfSubclassIsNotAnOverride() throws Exception {
        assertFalse(validateMethod(AnnotatedMethodResource.class));
    }

    @Test
    public void testInheritedAnnotatedMethodIsNotAnOverride() throws Exception {
        assertFalse(validateMethod(InheritingResource.class));
    }

    @Test
    public void testAnnotatedOverridingMethodIsAnOverride() {
        assertThrows(ValidationException.class, () -> validateMethod(AnnotatedOverridingResource.class));
    }

    private static boolean validateMethod(final Class<?> resourceClass) throws NoSuchMethodException {
        final ValidateOnExecutionHandler handler =
                new ValidateOnExecutionHandler(Validation.byDefaultProvider().configure(), true);
        final Method method = resourceClass.getMethod("post", String.class);
        return handler.validateMethod(resourceClass, method, method);
    }
}
