/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.validation.validateonexecution;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.util.runner.ConcurrentRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Michal Gajdos
 */
// @RunWith(ConcurrentRunner.class)
public class ValidateOnExecutionInheritanceGenericsTest extends ValidateOnExecutionAbstractTest {

    /**
     * On METHOD.
     */

    /**
     * {@link ValidateOnExecution} annotations from this interface should be considered during validating phase.
     */
    @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
    public static interface ValidateExecutableOnMethodsValidation<T extends Number> {

        @Min(0)
        @ValidateOnExecution
        public T validateExecutableDefault(@Max(10) final T value);

        @Min(0)
        @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
        public T validateExecutableMatch(@Max(10) final T value);

        @Min(0)
        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        public T validateExecutableMiss(@Max(10) final T value);

        @Min(0)
        @ValidateOnExecution(type = ExecutableType.NONE)
        public T validateExecutableNone(@Max(10) final T value);
    }

    /**
     * Wrong generic types. {@link ValidateOnExecution} annotations should not be considered at all.
     *
     * @param <T>
     */
    @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
    public static interface ValidateExecutableOnMethodsCharSequenceValidation<T extends CharSequence> {

        @Min(10)
        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        public T validateExecutableDefault(@Max(0) final T value);

        @Min(10)
        @ValidateOnExecution(type = ExecutableType.NONE)
        public T validateExecutableMatch(@Max(0) final T value);

        @Min(10)
        @ValidateOnExecution
        public T validateExecutableMiss(@Max(0) final T value);

        @Min(10)
        @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
        public T validateExecutableNone(@Max(0) final T value);
    }

    @ValidateOnExecution(type = ExecutableType.ALL)
    public static interface ValidateExecutableOnMethodsJaxRs extends ValidateExecutableOnMethodsValidation<Integer> {

        @POST
        @Path("validateExecutableDefault")
        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        Integer validateExecutableDefault(final Integer value);

        @POST
        @Path("validateExecutableMatch")
        @ValidateOnExecution(type = ExecutableType.GETTER_METHODS)
        Integer validateExecutableMatch(final Integer value);

        @POST
        @Path("validateExecutableMiss")
        @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
        Integer validateExecutableMiss(final Integer value);

        @POST
        @Path("validateExecutableNone")
        @ValidateOnExecution(type = ExecutableType.ALL)
        Integer validateExecutableNone(final Integer value);
    }

    public abstract static class ValidateExecutableOnMethodsAbstractResource
            implements ValidateExecutableOnMethodsJaxRs, ValidateExecutableOnMethodsCharSequenceValidation<String> {

        @ValidateOnExecution(type = ExecutableType.NONE)
        public abstract Integer validateExecutableDefault(final Integer value);

        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        public abstract Integer validateExecutableMatch(final Integer value);

        @ValidateOnExecution(type = ExecutableType.ALL)
        public abstract Integer validateExecutableMiss(final Integer value);

        @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
        public abstract Integer validateExecutableNone(final Integer value);
    }

    @Path("on-method")
    public static class ValidateExecutableOnMethodsResource extends ValidateExecutableOnMethodsAbstractResource {

        public Integer validateExecutableDefault(final Integer value) {
            return value;
        }

        public Integer validateExecutableMatch(final Integer value) {
            return value;
        }

        public Integer validateExecutableMiss(final Integer value) {
            return value;
        }

        public Integer validateExecutableNone(final Integer value) {
            return value;
        }

        public String validateExecutableDefault(final String value) {
            return value;
        }

        public String validateExecutableMatch(final String value) {
            return value;
        }

        public String validateExecutableMiss(final String value) {
            return value;
        }

        public String validateExecutableNone(final String value) {
            return value;
        }
    }

    /**
     * On TYPE.
     */

    @SuppressWarnings("JavaDoc")
    public static interface ValidateExecutableOnType<T extends Number> {

        @Min(0)
        public T validateExecutable(@Max(10) final T value);
    }

    @SuppressWarnings("JavaDoc")
    public static interface ValidateExecutableCharSequenceOnType<X extends CharSequence> {

        @Min(10)
        public X validateExecutable(@Max(0) final X value);
    }

    @ValidateOnExecution
    public static interface ValidateExecutableOnTypeDefault extends ValidateExecutableOnType<Integer> {
    }

    @ValidateOnExecution
    public static interface ValidateExecutableCharSequenceOnTypeDefault extends ValidateExecutableCharSequenceOnType<String> {

        @ValidateOnExecution
        public String validateExecutable(final String value);
    }

    /**
     * This {@link ValidateOnExecution} annotation should be considered during validating phase.
     */
    @ValidateOnExecution(type = ExecutableType.GETTER_METHODS)
    public abstract static class ValidateExecutableOnTypeDefaultAbstractResource implements ValidateExecutableOnTypeDefault {

        @POST
        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Path("on-type-default")
    @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
    public static class ValidateExecutableOnTypeDefaultResource extends ValidateExecutableOnTypeDefaultAbstractResource
            implements ValidateExecutableCharSequenceOnTypeDefault {

        @POST
        @Path("another")
        public String validateExecutable(final String value) {
            return value;
        }
    }

    /**
     * This {@link ValidateOnExecution} annotation should be considered during validating phase.
     */
    @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
    public static interface ValidateExecutableOnTypeMatch extends ValidateExecutableOnType<Integer> {
    }

    @ValidateOnExecution
    public static interface ValidateExecutableCharSequenceOnTypeMatch extends ValidateExecutableCharSequenceOnType<String> {

        @ValidateOnExecution
        public String validateExecutable(final String value);
    }

    @ValidateOnExecution(type = ExecutableType.GETTER_METHODS)
    public abstract static class ValidateExecutableOnTypeMatchAbstractResource implements ValidateExecutableOnTypeMatch {

        @POST
        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Path("on-type-match")
    @ValidateOnExecution(type = ExecutableType.NONE)
    public static class ValidateExecutableOnTypeMatchResource extends ValidateExecutableOnTypeMatchAbstractResource
            implements ValidateExecutableCharSequenceOnTypeMatch {

        @POST
        @Path("another")
        public String validateExecutable(final String value) {
            return value;
        }
    }

    /**
     * This {@link ValidateOnExecution} annotation should be considered during validating phase.
     */
    @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
    public static interface ValidateExecutableOnTypeMiss extends ValidateExecutableOnType<Integer> {
    }

    @ValidateOnExecution
    public static interface ValidateExecutableCharSequenceOnTypeMiss extends ValidateExecutableCharSequenceOnType<String> {

        @ValidateOnExecution
        public String validateExecutable(final String value);
    }

    @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
    public abstract static class ValidateExecutableOnTypeMissAbstractResource implements ValidateExecutableOnTypeMiss {

        @POST
        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Path("on-type-miss")
    @ValidateOnExecution
    public static class ValidateExecutableOnTypeMissResource extends ValidateExecutableOnTypeMissAbstractResource
            implements ValidateExecutableCharSequenceOnTypeMiss {

        @POST
        @Path("another")
        public String validateExecutable(final String value) {
            return value;
        }
    }

    /**
     * This {@link ValidateOnExecution} annotation should be considered during validating phase.
     */
    @ValidateOnExecution(type = ExecutableType.NONE)
    public static interface ValidateExecutableOnTypeNone extends ValidateExecutableOnType<Integer> {
    }

    @ValidateOnExecution
    public static interface ValidateExecutableCharSequenceOnTypeNone extends ValidateExecutableCharSequenceOnType<String> {

        @ValidateOnExecution
        public String validateExecutable(final String value);
    }

    @ValidateOnExecution(type = ExecutableType.ALL)
    public abstract static class ValidateExecutableOnTypeNoneAbstractResource implements ValidateExecutableOnTypeNone {

        @POST
        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Path("on-type-none")
    @ValidateOnExecution(type = {ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS})
    public static class ValidateExecutableOnTypeNoneResource extends ValidateExecutableOnTypeNoneAbstractResource
            implements ValidateExecutableCharSequenceOnTypeNone {

        @POST
        @Path("another")
        public String validateExecutable(final String value) {
            return value;
        }
    }

    /**
     * MIXED.
     */

    @ValidateOnExecution(type = ExecutableType.NONE)
    public static interface ValidateExecutableMixedDefault<T extends Number> {

        @Min(0)
        @ValidateOnExecution
        public T validateExecutable(@Max(10) final T value);
    }

    @ValidateOnExecution(type = ExecutableType.NONE)
    public static interface ValidateExecutableCharSequenceMixedDefault<T extends CharSequence> {

        @Min(10)
        @ValidateOnExecution(type = ExecutableType.NONE)
        public T validateExecutable(@Max(0) final T value);
    }

    @Path("mixed-default")
    public static class ValidateExecutableMixedDefaultResource
            implements ValidateExecutableMixedDefault<Integer>, ValidateExecutableCharSequenceMixedDefault<String> {

        @POST
        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        public Integer validateExecutable(final Integer value) {
            return value;
        }

        @POST
        @Path("another")
        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        public String validateExecutable(final String value) {
            return value;
        }
    }

    @ValidateOnExecution
    public static interface ValidateExecutableMixedNone<T extends Number> {

        @Min(0)
        @ValidateOnExecution(type = ExecutableType.NONE)
        public T validateExecutable(@Max(10) final T value);
    }

    @ValidateOnExecution
    public static interface ValidateExecutableCharSequenceMixedNone<T extends CharSequence> {

        @Min(10)
        @ValidateOnExecution
        public T validateExecutable(@Max(0) final T value);
    }

    @Path("mixed-none")
    public static class ValidateExecutableMixedNoneResource
            implements ValidateExecutableMixedNone<Integer>, ValidateExecutableCharSequenceMixedNone<String> {

        @POST
        @ValidateOnExecution(type = ExecutableType.ALL)
        public Integer validateExecutable(final Integer value) {
            return value;
        }

        @POST
        @Path("another")
        @ValidateOnExecution(type = ExecutableType.ALL)
        public String validateExecutable(final String value) {
            return value;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ValidateExecutableOnMethodsResource.class,
                ValidateExecutableOnTypeDefaultResource.class,
                ValidateExecutableOnTypeMatchResource.class,
                ValidateExecutableOnTypeMissResource.class,
                ValidateExecutableOnTypeNoneResource.class,
                ValidateExecutableMixedDefaultResource.class,
                ValidateExecutableMixedNoneResource.class)
                .property(ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK, true);
    }

    @Test
    public void testOnTypeValidateInputPassValidateExecutableDefault() throws Exception {
        _testOnType("default", 15, 200);
    }

    @Test
    public void testOnTypeValidateResultPassNoValidateExecutableDefault() throws Exception {
        _testOnType("default", -15, 200);
    }
}
