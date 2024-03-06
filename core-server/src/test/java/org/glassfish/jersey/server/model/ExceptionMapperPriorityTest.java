/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.inject.hk2.AbstractBinder;
import org.glassfish.jersey.internal.ExceptionMapperFactory;
import org.glassfish.jersey.internal.inject.CustomAnnotationLiteral;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.TestInjectionManagerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class ExceptionMapperPriorityTest {
    @Test
    void testExceptionMapperOverridePriority() {
        for (int order = 0; order != 2; order++) {
            final ResourceConfig rc = new ResourceConfig();
            if (order == 0) {
                rc.register(HigherPriorityExceptionMapper.class, 200);
                rc.register(LowerPriorityExceptionMapper.class, 100);
            } else {
                rc.register(LowerPriorityExceptionMapper.class, 100);
                rc.register(HigherPriorityExceptionMapper.class, 200);
            }

            final TestInjectionManagerFactory.BootstrapResult bootstrap = TestInjectionManagerFactory.createInjectionManager(rc);
            final ExceptionMapperFactory mapperFactory = new ExceptionMapperFactory(bootstrap.injectionManager);

            final ExceptionMapper mapper = mapperFactory.findMapping(new NullPointerException());
            Assertions.assertTrue(mapper instanceof LowerPriorityExceptionMapper,
                    "LowerPriorityExceptionMapper should be returned, got " + mapper.getClass().getSimpleName());
        }
    }

    @Test
    void testExceptionMapperPriority() {
        for (int order = 0; order != 2; order++) {
            final ResourceConfig rc = new ResourceConfig();

            if (order == 0) {
                rc.register(HigherPriorityExceptionMapper.class);
                rc.register(LowerPriorityExceptionMapper.class);
            } else {
                rc.register(LowerPriorityExceptionMapper.class);
                rc.register(HigherPriorityExceptionMapper.class);
            }

            final TestInjectionManagerFactory.BootstrapResult bootstrap = TestInjectionManagerFactory.createInjectionManager(rc);
            final ExceptionMapperFactory mapperFactory = new ExceptionMapperFactory(bootstrap.injectionManager);

            final ExceptionMapper mapper = mapperFactory.findMapping(new NullPointerException());
            Assertions.assertTrue(mapper instanceof HigherPriorityExceptionMapper,
                    "HigherPriorityExceptionMapper should be returned, got " + mapper.getClass().getSimpleName());
        }
    }

    @Test
    public void testFindPriorityExceptionMapper() {
        for (int order = 0; order != 2; order++) {
            int finalOrder = order;
            final InjectionManager injectionManager = Injections.createInjectionManager(new AbstractBinder() {
                @Override
                protected void configure() {
                    if (finalOrder == 0) {
                        bind(LowerPriorityExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                        bind(HigherPriorityExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                        bind(HighDistanceExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                    } else if (finalOrder == 1) {
                        bind(HighDistanceExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                        bind(HigherPriorityExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                        bind(LowerPriorityExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                    }
                }
            });
            injectionManager.completeRegistration();
            final ExceptionMapperFactory mapperFactory = new ExceptionMapperFactory(injectionManager);

            final ExceptionMapper mapper = mapperFactory.findMapping(new NullPointerException());
            Assertions.assertTrue(mapper instanceof HigherPriorityExceptionMapper,
                    "HigherPriorityExceptionMapper should be returned, got " + mapper.getClass().getSimpleName());
        }
    }

    @Test
    public void testFindPriorityExceptionMapperPrioritiesOverUSER() {
        for (int order = 0; order != 2; order++) {
            int finalOrder = order;
            final InjectionManager injectionManager = Injections.createInjectionManager(new AbstractBinder() {
                @Override
                protected void configure() {
                    if (finalOrder == 0) {
                        bind(LowerOverUSERPriorityExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                        bind(HigherOverUSERPriorityExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                    } else {
                        bind(HigherOverUSERPriorityExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                        bind(LowerOverUSERPriorityExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class)
                                .qualifiedBy(CustomAnnotationLiteral.INSTANCE);
                    }
                }
            });
            injectionManager.completeRegistration();
            final ExceptionMapperFactory mapperFactory = new ExceptionMapperFactory(injectionManager);

            final ExceptionMapper mapper = mapperFactory.findMapping(new NullPointerException());
            Assertions.assertTrue(mapper instanceof HigherOverUSERPriorityExceptionMapper,
                    "HigherPriorityExceptionMapper should be returned, got " + mapper.getClass().getSimpleName());
        }
    }

    abstract static class NPEExceptionMapper implements ExceptionMapper<NullPointerException> {
        @Override
        public Response toResponse(NullPointerException exception) {
            return null;
        }
    }

    @Priority(100)
    static class HigherPriorityExceptionMapper extends NPEExceptionMapper {
    }

    @Priority(200)
    static class LowerPriorityExceptionMapper extends NPEExceptionMapper {
    }

    @Priority(5500)
    static class HigherOverUSERPriorityExceptionMapper extends NPEExceptionMapper {
    }

    @Priority(6000)
    static class LowerOverUSERPriorityExceptionMapper extends NPEExceptionMapper {
    }

    @Priority(50)
    static class HighDistanceExceptionMapper implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException exception) {
            return null;
        }
    }
}
