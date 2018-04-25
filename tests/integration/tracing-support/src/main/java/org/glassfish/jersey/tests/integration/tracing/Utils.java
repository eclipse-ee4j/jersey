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

package org.glassfish.jersey.tests.integration.tracing;

import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configurable;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public final class Utils {

    public static final String HEADER_TRACING_PREFIX = "X-Jersey-Tracing-";

    public static final String APPLICATION_X_JERSEY_TEST = "application/x-jersey-test";
    public static final String FORMAT_PREFIX = "-=#[";
    public static final String FORMAT_SUFFIX = "]#=-";
    public static final String HEADER_TEST_ACTION = "test-action";

    private Utils() {
    }

    public static void configure(ResourceConfig configurable) {
        configurable.packages(Utils.class.getPackage().getName());
//        OR:
//        configure((Configurable)configurable);
//        configurable.register(PreMatchingContainerRequestFilter23.class);
//        configurable.register(PreMatchingContainerRequestFilter42.class);
//        configurable.register(ContainerRequestFilter68.class);
//        configurable.register(ContainerRequestFilterNoPriority.class);
//        configurable.register(ContainerResponseFilter5001.class);
//        configurable.register(ContainerResponseFilterNoPriority.class);
//        configurable.register(TestExceptionMapper.class);
//        configurable.register(TestExtendedExceptionMapperGeneric.class);
//        configurable.register(TestExtendedExceptionMapperRuntime.class);
//        configurable.register(Resource.class);
//        configurable.register(SubResource.class);
        configurable.register(new LoggingFeature(Logger.getAnonymousLogger(), LoggingFeature.Verbosity.PAYLOAD_ANY));
    }

    public static void configure(ClientConfig configurable) {
        configure((Configurable) configurable);
    }

    public static void configure(Configurable configurable) {
        configurable.register(ReaderInterceptor14.class);
        configurable.register(ReaderInterceptor18.class);
        configurable.register(WriterInterceptor39.class);
        configurable.register(WriterInterceptor45.class);
        configurable.register(new MessageBodyReaderTestFormat(false));
        configurable.register(MessageBodyReaderGeneric.class);
        configurable.register(new MessageBodyWriterTestFormat(false));
        configurable.register(MessageBodyWriterGeneric.class);
    }

    public static void throwException(final ContainerRequestContext requestContext,
                                      final Object fromContext,
                                      final TestAction throwWebApplicationException,
                                      final TestAction throwProcessingException,
                                      final TestAction throwAnyException) {
        final Utils.TestAction testAction = Utils.getTestAction(requestContext);
        throwExceptionImpl(testAction, fromContext, throwWebApplicationException, throwProcessingException, throwAnyException);
    }

    public static void throwException(final String testActionName,
                                      final Object fromContext,
                                      final TestAction throwWebApplicationException,
                                      final TestAction throwProcessingException,
                                      final TestAction throwAnyException) {
        Utils.TestAction testAction;
        try {
            testAction = TestAction.valueOf(testActionName);
        } catch (IllegalArgumentException ex) {
            try {
                testAction = TestAction.valueOf(new StringBuffer(testActionName).reverse().toString());
            } catch (IllegalArgumentException ex2) {
                testAction = null;
            }
        }
        throwExceptionImpl(testAction, fromContext, throwWebApplicationException, throwProcessingException, throwAnyException);
    }

    private static void throwExceptionImpl(final Utils.TestAction testAction,
                                           final Object fromContext,
                                           final TestAction throwWebApplicationException,
                                           final TestAction throwProcessingException,
                                           final TestAction throwAnyException) {
        final String message = "Test Exception from " + fromContext.getClass().getName();
        if (testAction == null) {
            // do nothing
        } else if (testAction == throwWebApplicationException) {
            throw new WebApplicationException(message);
        } else if (testAction == throwProcessingException) {
            throw new ProcessingException(message);
        } else if (testAction == throwAnyException) {
            throw new RuntimeException(message);
        }
    }

    public static TestAction getTestAction(ContainerRequestContext requestContext) {
        String testActionHeader = requestContext.getHeaderString(HEADER_TEST_ACTION);
        TestAction testAction = null;
        if (testActionHeader != null) {
            testAction = TestAction.valueOf(testActionHeader);
        }
        return testAction;
    }

    public static enum TestAction {
        PRE_MATCHING_REQUEST_FILTER_THROW_WEB_APPLICATION,
        PRE_MATCHING_REQUEST_FILTER_THROW_PROCESSING,
        PRE_MATCHING_REQUEST_FILTER_THROW_ANY,
        MESSAGE_BODY_READER_THROW_WEB_APPLICATION,
        MESSAGE_BODY_READER_THROW_PROCESSING,
        MESSAGE_BODY_READER_THROW_ANY,
        MESSAGE_BODY_WRITER_THROW_WEB_APPLICATION,
        MESSAGE_BODY_WRITER_THROW_PROCESSING,
        MESSAGE_BODY_WRITER_THROW_ANY;
        //TODO add other *_THROW_* actions to throw exception from other stages
    }
}
