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

package org.glassfish.jersey.server.monitoring;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.monitoring.ApplicationInfoListener;
import org.glassfish.jersey.server.internal.monitoring.MonitoringEventListener;
import org.glassfish.jersey.server.internal.monitoring.MonitoringFeature;
import org.glassfish.jersey.server.internal.monitoring.jmx.MBeanExposer;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test of registration of {@link MonitoringFeature}.
 * @author Miroslav Fuksa
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class MonitoringFeatureTest {

    @Test
    public void testStatisticsEnabled() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(MonitoringFeature.class);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testMonitoringDisabled() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(MonitoringFeature.class);
        resourceConfig.property(ServerProperties.MONITORING_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertFalse(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertFalse(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testStatisticsDisabled() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(MonitoringFeature.class);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertFalse(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testMonitoringEnabledByAutodiscovery() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_ENABLED, true);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testMonitoringEnabledStatisticsDisabledByAutodiscovery() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_ENABLED, true);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertFalse(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testStatisticsEnabledByAutodiscovery() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, true);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testStatisticsDisabledByAutodiscovery() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertFalse(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertFalse(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }


    @Test
    public void testStatisticsEnabledMbeansEnabledByInstance() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        final MonitoringFeature monitoringFeature = new MonitoringFeature();
        monitoringFeature.setmBeansEnabled(true);
        resourceConfig.register(monitoringFeature);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertTrue(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testStatisticsEnabledMbeansEnabledByInstance2() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        final MonitoringFeature monitoringFeature = new MonitoringFeature();
        monitoringFeature.setmBeansEnabled(true);
        resourceConfig.register(monitoringFeature);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testAllDisabled() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        final MonitoringFeature monitoringFeature = new MonitoringFeature();
        monitoringFeature.setmBeansEnabled(true);
        resourceConfig.register(monitoringFeature);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, false);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertFalse(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testAllDisabled2() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        final MonitoringFeature monitoringFeature = new MonitoringFeature();
        resourceConfig.register(monitoringFeature);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, false);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertFalse(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testAllDisabled3() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, false);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertFalse(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertFalse(config.isRegistered(MonitoringEventListener.class));
        Assert.assertFalse(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testAllEnabled() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, true);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, true);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertTrue(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testAllEnabled2() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, true);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, true);
        final MonitoringFeature monitoringFeature = new MonitoringFeature();
        monitoringFeature.setmBeansEnabled(false);
        resourceConfig.register(monitoringFeature);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertTrue(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testOnlyMBeansEnabled() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, true);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertTrue(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testOnlyMBeansEnabled2() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, true);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertTrue(config.isRegistered(MBeanExposer.class));
    }

    @Test
    public void testOnlyMBeansEnabled3() {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, true);
        resourceConfig.property(ServerProperties.MONITORING_STATISTICS_ENABLED, false);
        resourceConfig.register(new MonitoringFeature());
        final ApplicationHandler applicationHandler = new ApplicationHandler(resourceConfig);

        final ResourceConfig config = applicationHandler.getConfiguration();
        Assert.assertTrue(config.isRegistered(ApplicationInfoListener.class));
        Assert.assertTrue(config.isRegistered(MonitoringEventListener.class));
        Assert.assertTrue(config.isRegistered(MBeanExposer.class));
    }
}
