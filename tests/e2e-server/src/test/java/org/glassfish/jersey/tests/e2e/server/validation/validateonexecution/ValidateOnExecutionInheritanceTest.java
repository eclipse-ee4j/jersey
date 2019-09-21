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

import org.junit.Test;

/**
 * @author Michal Gajdos
 */
// @RunWith(ConcurrentRunner.class)
public class ValidateOnExecutionInheritanceTest extends ValidateOnExecutionAbstractTest {

    /**
     * On METHOD.
     */

    /**
     * {@link ValidateOnExecution} annotations from this interface should be considered during validating phase.
     */
    @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
    public static interface ValidateExecutableOnMethodsValidation {

        @Min(0)
        @ValidateOnExecution
        public Integer validateExecutableDefault(@Max(10) final Integer value);

        @Min(0)
        @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
        public Integer validateExecutableMatch(@Max(10) final Integer value);

        @Min(0)
        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        public Integer validateExecutableMiss(@Max(10) final Integer value);

        @Min(0)
        @ValidateOnExecution(type = ExecutableType.NONE)
        public Integer validateExecutableNone(@Max(10) final Integer value);
    }

    @ValidateOnExecution(type = ExecutableType.ALL)
    public static interface ValidateExecutableOnMethodsJaxRs extends ValidateExecutableOnMethodsValidation {

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

    public abstract static class ValidateExecutableOnMethodsAbstractResource implements ValidateExecutableOnMethodsJaxRs {

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
    }

    /**
     * On TYPE.
     */

    @SuppressWarnings("JavaDoc")
    public static interface ValidateExecutableOnType {

        @POST
        @Min(0)
        public Integer validateExecutable(@Max(10) final Integer value);
    }

    @ValidateOnExecution
    public static interface ValidateExecutableOnTypeDefault extends ValidateExecutableOnType {
    }

    /**
     * This {@link ValidateOnExecution} annotation should be considered during validating phase.
     */
    @ValidateOnExecution(type = ExecutableType.GETTER_METHODS)
    public abstract static class ValidateExecutableOnTypeDefaultAbstractResource implements ValidateExecutableOnTypeDefault {

        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Path("on-type-default")
    @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
    public static class ValidateExecutableOnTypeDefaultResource extends ValidateExecutableOnTypeDefaultAbstractResource {
    }

    /**
     * This {@link ValidateOnExecution} annotation should be considered during validating phase.
     */
    @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
    public static interface ValidateExecutableOnTypeMatch extends ValidateExecutableOnType {
    }

    @ValidateOnExecution(type = ExecutableType.GETTER_METHODS)
    public abstract static class ValidateExecutableOnTypeMatchAbstractResource implements ValidateExecutableOnTypeMatch {

        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Path("on-type-match")
    @ValidateOnExecution(type = ExecutableType.NONE)
    public static class ValidateExecutableOnTypeMatchResource extends ValidateExecutableOnTypeMatchAbstractResource {
    }

    /**
     * This {@link ValidateOnExecution} annotation should be considered during validating phase.
     */
    @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
    public static interface ValidateExecutableOnTypeMiss extends ValidateExecutableOnType {
    }

    @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
    public abstract static class ValidateExecutableOnTypeMissAbstractResource implements ValidateExecutableOnTypeMiss {

        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Path("on-type-miss")
    @ValidateOnExecution
    public static class ValidateExecutableOnTypeMissResource extends ValidateExecutableOnTypeMissAbstractResource {
    }

    /**
     * This {@link ValidateOnExecution} annotation should be considered during validating phase.
     */
    @ValidateOnExecution(type = ExecutableType.NONE)
    public static interface ValidateExecutableOnTypeNone extends ValidateExecutableOnType {
    }

    @ValidateOnExecution(type = ExecutableType.ALL)
    public abstract static class ValidateExecutableOnTypeNoneAbstractResource implements ValidateExecutableOnTypeNone {

        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Path("on-type-none")
    @ValidateOnExecution(type = {ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS})
    public static class ValidateExecutableOnTypeNoneResource extends ValidateExecutableOnTypeNoneAbstractResource {
    }

    /**
     * MIXED.
     */

    @ValidateOnExecution(type = ExecutableType.NONE)
    public static interface ValidateExecutableMixedDefault {

        @Min(0)
        @ValidateOnExecution
        public Integer validateExecutable(@Max(10) final Integer value);
    }

    @Path("mixed-default")
    public static class ValidateExecutableMixedDefaultResource implements ValidateExecutableMixedDefault {

        @POST
        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @ValidateOnExecution
    public static interface ValidateExecutableMixedNone {

        @Min(0)
        @ValidateOnExecution(type = ExecutableType.NONE)
        public Integer validateExecutable(@Max(10) final Integer value);
    }

    @Path("mixed-none")
    public static class ValidateExecutableMixedNoneResource implements ValidateExecutableMixedNone {

        @POST
        @ValidateOnExecution(type = ExecutableType.ALL)
        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @ValidateOnExecution
    public static interface ValidateExecutableMixedClassDefault {

        @Min(0)
        public Integer validateExecutable(@Max(10) final Integer value);
    }

    @Path("mixed-class-default")
    @ValidateOnExecution(type = ExecutableType.NONE)
    public static class ValidateExecutableMixedClassDefaultResource implements ValidateExecutableMixedClassDefault {

        @POST
        @ValidateOnExecution(type = ExecutableType.CONSTRUCTORS)
        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @ValidateOnExecution(type = ExecutableType.NONE)
    public static interface ValidateExecutableMixedClassNone {

        @Min(0)
        public Integer validateExecutable(@Max(10) final Integer value);
    }

    @Path("mixed-class-none")
    @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
    public static class ValidateExecutableMixedClassNoneResource implements ValidateExecutableMixedClassNone {

        @POST
        @ValidateOnExecution(type = ExecutableType.ALL)
        public Integer validateExecutable(final Integer value) {
            return value;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(ValidateExecutableOnMethodsResource.class,
                ValidateExecutableOnTypeNoneResource.class,
                ValidateExecutableOnTypeMissResource.class,
                ValidateExecutableOnTypeMatchResource.class,
                ValidateExecutableOnTypeDefaultResource.class,
                ValidateExecutableMixedDefaultResource.class,
                ValidateExecutableMixedNoneResource.class,
                ValidateExecutableMixedClassNoneResource.class,
                ValidateExecutableMixedClassDefaultResource.class)
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

    @Test
    public void testMixedClassValidatePassDefault() throws Exception {
        _test("mixed-class-default", 0, 200);
    }

    @Test
    public void testMixedClassValidateInputPassValidateDefault() throws Exception {
        _test("mixed-class-default", 15, 200);
    }

    @Test
    public void testMixedClassValidateResultPassNoValidateDefault() throws Exception {
        _test("mixed-class-default", -15, 200);
    }

    @Test
    public void testMixedClassValidatePassNone() throws Exception {
        _test("mixed-class-none", 0, 200);
    }

    @Test
    public void testMixedClassValidateInputPassNone() throws Exception {
        _test("mixed-class-none", 15, 200);
    }

    @Test
    public void testMixedClassValidateResultPassNone() throws Exception {
        _test("mixed-class-none", -15, 200);
    }
}
