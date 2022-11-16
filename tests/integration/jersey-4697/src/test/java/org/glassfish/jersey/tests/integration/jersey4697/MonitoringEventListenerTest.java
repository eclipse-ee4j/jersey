/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey4697;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.monitoring.MonitoringEventListener;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.ExceptionMapperMXBean;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MonitoringEventListenerTest extends JerseyTest {

    private static final long TIMEOUT = 1000;
    private static final String APPLICATION_NAME = "MonitoringEventListenerTest";
    private static final String MBEAN_EXCEPTION =
            "org.glassfish.jersey:type=" + APPLICATION_NAME + ",subType=Global,exceptions=ExceptionMapper";

    @Path("/example")
    public static class ExampleResource {
        @Inject
        private InjectionManager injectionManager;
        @GET
        @Path("/error")
        public Response error() {
            throw new RuntimeException("Any exception to be counted in ExceptionMapper");
        }
        @GET
        @Path("/poison")
        public Response poison() {
            MonitoringEventListener monitoringEventListener = listener();
            RequestEvent requestEvent = mock(RequestEvent.class);
            when(requestEvent.getType()).thenReturn(RequestEvent.Type.START);
            RequestEventListener eventListener = monitoringEventListener.onRequest(requestEvent);
            RequestEvent poisonEvent = mock(RequestEvent.class);
            when(poisonEvent.getType()).thenReturn(RequestEvent.Type.EXCEPTION_MAPPING_FINISHED);
            when(poisonEvent.getExceptionMapper())
                .thenThrow(new IllegalStateException("This causes the scheduler to stop working"));
            eventListener.onEvent(poisonEvent);
            return Response.ok().build();
        }
        @GET
        @Path("/queueSize")
        public Response queueSize() throws Exception {
            MonitoringEventListener monitoringEventListener = listener();
            Method method = MonitoringEventListener.class.getDeclaredMethod("getExceptionMapperEvents");
            method.setAccessible(true);
            Collection<?> queue = (Collection<?>) method.invoke(monitoringEventListener);
            return Response.ok(queue.size()).build();
        }
        private MonitoringEventListener listener() {
            Iterable<ApplicationEventListener> listeners =
                    Providers.getAllProviders(injectionManager, ApplicationEventListener.class);
            for (ApplicationEventListener listener : listeners) {
                if (listener instanceof MonitoringEventListener) {
                    return (MonitoringEventListener) listener;
                }
            }
            throw new IllegalStateException("MonitoringEventListener was not found");
        }
    }

    @Provider
    public static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
        @Override
        public Response toResponse(RuntimeException e) {
            return Response.status(500).entity("RuntimeExceptionMapper: " + e.getMessage()).build();
        }
    }

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig(ExampleResource.class);
        // Need to map the exception to be counted by ExceptionMapper
        resourceConfig.register(RuntimeExceptionMapper.class);
        resourceConfig.property(ServerProperties.MONITORING_ENABLED, true);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, true);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, true);
        // Scheduler will process 1000 events per second
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_REFRESH_INTERVAL, 1);
        resourceConfig.setApplicationName(APPLICATION_NAME);
        return resourceConfig;
    }

    @Test
    public void exceptionInScheduler() throws Exception {
        final Long ERRORS_BEFORE_FAIL = 10L;
        // Send some requests to process some statistics.
        request(ERRORS_BEFORE_FAIL);
        // Give some time to process events
        Thread.sleep(TIMEOUT);
        // Verify the exceptionMapperEvents is empty, because no event of this type was sent yet
        queueIsEmpty();
        // Sending one event that will make an internal error in the scheduler.
        // No new events will be pushed in the queues after this.
        makeFailure();
        // Sending again requests. These events will not be processed, so they will not be counted for statistics.
        request(20);
        // The expectation is that the scheduler is not going to process previous events because there was a failure before.
        // We give some time before checking that no new errors are registered.
        Thread.sleep(TIMEOUT);
        Long monitoredErrors = mappedErrorsFromJMX(MBEAN_EXCEPTION);
        assertEquals(ERRORS_BEFORE_FAIL, monitoredErrors);
    }

    private void makeFailure() {
        Response response = target("/example/poison").request().get();
        assertEquals(200, response.getStatus());
    }

    private void queueIsEmpty() {
        Response response = target("/example/queueSize").request().get();
        assertEquals(200, response.getStatus());
        assertEquals(Integer.valueOf(0), response.readEntity(Integer.class));
    }

    private Long mappedErrorsFromJMX(String name) throws Exception {
        Long monitoredErrors = null;
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName(name);
        ExceptionMapperMXBean bean = JMX.newMBeanProxy(mbs, objectName, ExceptionMapperMXBean.class);
        Map<?, ?> counter = bean.getExceptionMapperCount();
        CompositeDataSupport value = (CompositeDataSupport) counter.entrySet().iterator().next().getValue();
        for (Object obj : value.values()) {
            if (obj instanceof Long) {
                // Messy way to get the errors, but generic types doesn't match and there is no nice way
                monitoredErrors = (Long) obj;
                break;
            }
        }
        return monitoredErrors;
    }

    private void request(long requests) {
        for (long i = 0; i < requests; i++) {
            Response response = target("/example/error").request().get();
            assertEquals(500, response.getStatus());
        }
    }
}
