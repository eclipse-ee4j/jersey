/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.monitoring;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import javax.annotation.Priority;
import javax.inject.Provider;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.monitoring.ApplicationInfo;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * The test uses server properties {@link ServerProperties#MONITORING_STATISTICS_MBEANS_ENABLED},
 * {@link ServerProperties#MONITORING_STATISTICS_MBEANS_ENABLED},
 * {@link ServerProperties#MONITORING_STATISTICS_MBEANS_ENABLED}
 * and it also implements {@link ForcedAutoDiscoverable} and tests if it is possible to inject
 * {@link ApplicationInfo} in different circumstances.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@RunWith(Parameterized.class)
public class ApplicationInfoTest extends JerseyTest {

    private static final String FORCE_ENABLE = "FORCE_ENABLE";
    private static final String ENABLE_MONITORING = "ENABLE_MONITORING";
    private static final String ENABLE_MONITORING_STATISTICS = "ENABLE_MONITORING_STATISTICS";
    private static final String ENABLE_MONITORING_STATISTICS_MBEANS = "ENABLE_MONITORING_STATISTICS_MBEANS";

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                //force, 3x AutoDiscoverable, 3x ResourceConfig,   response
                // no property set => 500
                {false, false, false, false, null, null, null, 500},
                // property set by ForcedAutoDiscoverable => 200
                {false, true, false, false, null, null, null, 200},
                {false, false, true, false, null, null, null, 200},
                {false, false, false, true, null, null, null, 200},
                // property disable by ResourceConfig => 500
                {false, true, false, false, false, false, false, 500},
                {false, false, true, false, false, false, false, 500},
                {false, false, false, true, false, false, false, 500},
                // property disable by ResourceConfig but forced by ForcedAutoDiscoverable => 200
                {true, true, false, false, false, false, false, 200},
                {true, false, true, false, false, false, false, 200},
                {true, false, false, true, false, false, false, 200}
        });
    }

    private int responseStatus;

    public ApplicationInfoTest(boolean forceEnable, boolean enableMonitoring,
                               boolean enableMonitoringStatistics, boolean enableMonitoringStatisticsMBeans,
                               Boolean monitoringEnabled, Boolean monitoringStatisticsEnabled,
                               Boolean monitoringStatisticsMBeansEnabled, int responseStatus) {
        super(createApplication(forceEnable, enableMonitoring,
                enableMonitoringStatistics, enableMonitoringStatisticsMBeans,
                monitoringEnabled, monitoringStatisticsEnabled,
                monitoringStatisticsMBeansEnabled));
        this.responseStatus = responseStatus;
    }

    private static Application createApplication(boolean forceEnable, boolean enableMonitoring,
                                                 boolean enableMonitoringStatistics, boolean enableMonitoringStatisticsMBeans,
                                                 Boolean monitoringEnabled, Boolean monitoringStatisticsEnabled,
                                                 Boolean monitoringStatisticsMBeansEnabled) {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class);
        resourceConfig.property(ServerProperties.APPLICATION_NAME, "testApp");
        resourceConfig.property(FORCE_ENABLE, forceEnable);
        if (enableMonitoring) {
            resourceConfig.property(ENABLE_MONITORING, true);
        }
        if (enableMonitoringStatistics) {
            resourceConfig.property(ENABLE_MONITORING_STATISTICS, true);
        }
        if (enableMonitoringStatisticsMBeans) {
            resourceConfig.property(ENABLE_MONITORING_STATISTICS_MBEANS, true);
        }
        if (monitoringEnabled != null) {
            resourceConfig.property(ServerProperties.MONITORING_ENABLED, monitoringEnabled);
        }
        if (monitoringStatisticsEnabled != null) {
            resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, monitoringStatisticsEnabled);
        }
        if (monitoringStatisticsMBeansEnabled != null) {
            resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, monitoringStatisticsMBeansEnabled);
        }

        return resourceConfig;
    }

    @Test
    public void test() {
        final Response response = target().path("resource").request().get();
        Assert.assertEquals(responseStatus, response.getStatus());
        if (responseStatus == 200) {
            Assert.assertEquals("testApp", response.readEntity(String.class));
        }
    }

    @Path("resource")
    public static class Resource {

        @Context
        Provider<ApplicationInfo> applicationInfoProvider;

        @GET
        public String getAppName() {
            final ApplicationInfo applicationInfo = applicationInfoProvider.get();
            return applicationInfo.getResourceConfig().getApplicationName();
        }
    }

    @ConstrainedTo(RuntimeType.SERVER)
    @Priority(AutoDiscoverable.DEFAULT_PRIORITY - 1)
    public static class ForcedAutoDiscoverableImpl implements ForcedAutoDiscoverable {

        @Override
        public void configure(final FeatureContext context) {
            final boolean forceEnable = PropertiesHelper.isProperty(context.getConfiguration().getProperty(FORCE_ENABLE));
            if (PropertiesHelper.isProperty(context.getConfiguration().getProperty(ENABLE_MONITORING))) {
                enable(context, forceEnable, ServerProperties.MONITORING_ENABLED);
            }
            if (PropertiesHelper.isProperty(context.getConfiguration().getProperty(ENABLE_MONITORING_STATISTICS))) {
                enable(context, forceEnable, ServerProperties.MONITORING_STATISTICS_ENABLED);
            }
            if (PropertiesHelper.isProperty(context.getConfiguration().getProperty(ENABLE_MONITORING_STATISTICS_MBEANS))) {
                enable(context, forceEnable, ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED);
            }
        }

        private void enable(FeatureContext context, boolean forceEnable, String propertyName) {
            if (forceEnable) {
                context.property(propertyName, true);
            } else {
                if (context.getConfiguration().getProperty(propertyName) == null) {
                    context.property(propertyName, true);
                }
            }
        }

    }

}
