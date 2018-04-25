/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.internal;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.ExceptionMapperFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test of {@link ExceptionMapperFactory}.
 */
public class ExceptionMapperFactoryTest {

    private static class ExtendedExceptionMappers extends AbstractBinder {

        @Override
        protected void configure() {
            bind(IllegalArgumentExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class);
            bind(IllegalStateExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class);
        }

    }

    private static class AllMappers extends AbstractBinder {

        @Override
        protected void configure() {
            bind(IllegalArgumentExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class);
            bind(IllegalStateExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class);
            bind(RuntimeExceptionMapper.class).to(ExceptionMapper.class).in(Singleton.class);
        }

    }

    /**
     * Test spec:
     * <p/>
     * setup:<br/>
     * - have two extended exception mappers, order matters<br/>
     * - both using the same generic type (RuntimeException)<br/>
     * - first mapper return isMappable true only to IllegalArgumentException<br/>
     * - second mapper return isMappable true only to IllegalStateException<br/>
     * <br/>
     * when:<br/>
     * - {@link ExceptionMapperFactory#findMapping(Throwable)} with IllegalArgumentException instance<br/>
     * <br/>
     * then:<br/>
     * - exception mapper factory returns IllegalArgumentExceptionMapper<br/>
     * <p/>
     * why:<br/>
     * - IllegalArgumentException has the same distance (1) for both exception mappers generic type (RuntimeException),
     * but IllegalArgumentException's isMappable return true, so it is the winner
     *
     * @throws Exception unexpected - if anything goes wrong, the test fails
     */
    @Test
    public void testFindMappingExtendedExceptions() throws Exception {
        final InjectionManager injectionManager = Injections.createInjectionManager(new ExtendedExceptionMappers());
        injectionManager.completeRegistration();
        final ExceptionMapperFactory mapperFactory = new ExceptionMapperFactory(injectionManager);

        final ExceptionMapper mapper = mapperFactory.findMapping(new IllegalArgumentException());

        Assert.assertTrue("IllegalArgumentExceptionMapper should be returned",
                mapper instanceof IllegalArgumentExceptionMapper);
    }

    /**
     * Test spec:
     * <p/>
     * setup:<br/>
     * - have 3 exception mappers, order matters<br/>
     * - first is *not* extended mapper typed to RuntimeException
     * - second and third are extended mappers type to RuntimeException
     * <br/>
     * when:<br/>
     * - {@link ExceptionMapperFactory#findMapping(Throwable)} invoked with RuntimeException instance<br/>
     * then: <br/>
     * - exception mapper factory returns RuntimeExceptionMapper<br/>
     * <p/>
     * why:<br/>
     * - RuntimeException mapper has distance 0 for RuntimeException, it is not extended mapper, so it will be chosen
     * immediately, cause there is no better option possible
     *
     * @throws Exception unexpected - if anything goes wrong, the test fails
     */
    @Test
    public void testFindMapping() throws Exception {
        final InjectionManager injectionManager = Injections.createInjectionManager(new AllMappers());
        injectionManager.completeRegistration();
        final ExceptionMapperFactory mapperFactory = new ExceptionMapperFactory(injectionManager);

        final ExceptionMapper<RuntimeException> mapper = mapperFactory.findMapping(new RuntimeException());

        Assert.assertTrue("RuntimeExceptionMapper should be returned", mapper instanceof RuntimeExceptionMapper);
    }

    /**
     * Test spec: <br/>
     * <p/>
     * setup:<br/>
     * - have 2 extended mappers, order matters<br/>
     * - first mapper return isMappable true only to IllegalArgumentException<br/>
     * - second mapper return isMappable true only to IllegalStateException<br/>
     * <br/>
     * when:<br/>
     * - {@link ExceptionMapperFactory#find(Class)} invoked with IllegalArgumentException.class<br/>
     * then:<br/>
     * - exception mapper factory returns IllegalArgumentExceptionMapper<br/>
     * <p/>
     * why:<br/>
     * - both exception mappers have distance 1 to IllegalArgumentException, we don't have instance of the
     * IllegalArgumentException, so the isMappable check is not used and both are accepted, the later accepted is
     * the winner
     *
     * @throws Exception unexpected - if anything goes wrong, the test fails
     */
    @Test
    public void testFindExtendedExceptions() throws Exception {
        final InjectionManager injectionManager = Injections.createInjectionManager(new ExtendedExceptionMappers());
        injectionManager.completeRegistration();
        final ExceptionMapperFactory mapperFactory = new ExceptionMapperFactory(injectionManager);

        final ExceptionMapper mapper = mapperFactory.find(IllegalArgumentException.class);

        Assert.assertTrue("IllegalStateExceptionMapper should be returned",
                mapper instanceof IllegalStateExceptionMapper);
    }

    /**
     * Extended Exception Mapper which has RuntimeException as generic type and isMappable returns true if the
     * exception is instance of IllegalArgumentException.
     */
    private static class IllegalArgumentExceptionMapper implements ExtendedExceptionMapper<RuntimeException> {

        @Override
        public boolean isMappable(final RuntimeException exception) {
            return exception instanceof IllegalArgumentException;
        }

        @Override
        public Response toResponse(final RuntimeException exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .build();
        }

    }

    /**
     * Extended Exception Mapper which has RuntimeException as generic type and isMappable returns true if the
     * exception is instance of IllegalStateException.
     */
    private static class IllegalStateExceptionMapper implements ExtendedExceptionMapper<RuntimeException> {

        @Override
        public boolean isMappable(final RuntimeException exception) {
            return exception instanceof IllegalStateException;
        }

        @Override
        public Response toResponse(final RuntimeException exception) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .build();
        }

    }

    /**
     * Exception Mapper which has RuntimeException as generic type.
     */
    private static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

        @Override
        public Response toResponse(final RuntimeException exception) {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .build();
        }

    }
}
