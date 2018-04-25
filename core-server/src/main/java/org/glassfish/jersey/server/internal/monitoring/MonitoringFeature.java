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

package org.glassfish.jersey.server.internal.monitoring;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.GenericType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.internal.monitoring.jmx.MBeanExposer;
import org.glassfish.jersey.server.monitoring.ApplicationInfo;
import org.glassfish.jersey.server.monitoring.MonitoringStatistics;
import org.glassfish.jersey.server.monitoring.MonitoringStatisticsListener;

/**
 * Feature that enables calculating of {@link MonitoringStatistics monitoring statistics} and
 * optionally also enables exposure of monitoring MBeans.
 * <p>
 * Calculation of {@code MonitoringStatistics} is necessary in order to expose monitoring MBeans, so by default
 * this feature always enables calculation of {@code MonitoringStatistics}. Additionally, the feature can be
 * configured by setting {@code true} to {@link #setmBeansEnabled(boolean)} in order to enable exposure
 * of monitoring MBeans. The same can be achieved by configuration of a property
 * {@link org.glassfish.jersey.server.ServerProperties#MONITORING_STATISTICS_MBEANS_ENABLED} which
 * overrides the setting defined by the {@link #setmBeansEnabled(boolean)} method.
 * <p/>
 * <p>
 * The MonitoringStatistics can be controlled also by definition of a property
 * {@link org.glassfish.jersey.server.ServerProperties#MONITORING_STATISTICS_ENABLED} which overrides
 * the registration of this feature.
 * </p>
 * When auto-discovery is enabled then monitoring statistics and exposure of MBeans can be controlled only
 * by properties above without a need to explicitly register this feature.
 *
 * @see org.glassfish.jersey.server.ServerProperties#MONITORING_STATISTICS_ENABLED for more details.
 * @author Miroslav Fuksa
 */
public final class MonitoringFeature implements Feature {

    private static final Logger LOGGER = Logger.getLogger(MonitoringFeature.class.getName());

    private boolean monitoringEnabled = true;
    private boolean statisticsEnabled = true; // monitoring statistics are enabled only if monitoring is enabled
    private boolean mBeansEnabled; // monitoring mbeans are enabled only if monitoring statistics is enabled

    @Override
    public boolean configure(FeatureContext context) {
        final Boolean monitoringEnabledProperty = ServerProperties.getValue(context.getConfiguration().getProperties(),
                ServerProperties.MONITORING_ENABLED, null, Boolean.class);
        final Boolean statisticsEnabledProperty = ServerProperties.getValue(context.getConfiguration().getProperties(),
                ServerProperties.MONITORING_STATISTICS_ENABLED, null, Boolean.class);
        final Boolean mbeansEnabledProperty = ServerProperties.getValue(context.getConfiguration().getProperties(),
                ServerProperties.MONITORING_STATISTICS_MBEANS_ENABLED, null, Boolean.class);

        if (monitoringEnabledProperty != null) {
            monitoringEnabled = monitoringEnabledProperty;
            statisticsEnabled = monitoringEnabled; // monitoring statistics are enabled by default if monitoring is enabled
        }

        if (statisticsEnabledProperty != null) {
            monitoringEnabled = monitoringEnabled || statisticsEnabledProperty;
            statisticsEnabled = statisticsEnabledProperty;
        }

        if (mbeansEnabledProperty != null) {
            monitoringEnabled = monitoringEnabled || mbeansEnabledProperty;
            statisticsEnabled = statisticsEnabled || mbeansEnabledProperty;
            mBeansEnabled = mbeansEnabledProperty;
        }

        if (statisticsEnabledProperty != null && !statisticsEnabledProperty) {
            if (mbeansEnabledProperty != null && mBeansEnabled) {
                LOGGER.log(Level.WARNING,
                        LocalizationMessages.WARNING_MONITORING_FEATURE_ENABLED(ServerProperties.MONITORING_STATISTICS_ENABLED));
            } else {
                LOGGER.log(Level.WARNING,
                        LocalizationMessages.WARNING_MONITORING_FEATURE_DISABLED(ServerProperties.MONITORING_STATISTICS_ENABLED));
            }
        }

        if (monitoringEnabled) {
            context.register(ApplicationInfoListener.class);
            context.register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bindFactory(ReferencingFactory.<ApplicationInfo>referenceFactory())
                            .to(new GenericType<Ref<ApplicationInfo>>() { })
                            .in(Singleton.class);

                    bindFactory(ApplicationInfoInjectionFactory.class)
                            .to(ApplicationInfo.class);
                }
            });
        }

        if (statisticsEnabled) {
            context.register(MonitoringEventListener.class);
            context.register(new AbstractBinder() {
                @Override
                protected void configure() {
                    bindFactory(ReferencingFactory.<MonitoringStatistics>referenceFactory())
                            .to(new GenericType<Ref<MonitoringStatistics>>() { })
                            .in(Singleton.class);

                    bindFactory(StatisticsInjectionFactory.class).to(MonitoringStatistics.class);

                    bind(StatisticsListener.class).to(MonitoringStatisticsListener.class).in(Singleton.class);
                }
            });
        }

        if (mBeansEnabled) {
            // instance registration is needed here as MBeanExposer needs to be a singleton so that
            // one instance handles listening to events of MonitoringStatisticsListener and ContainerLifecycleListener
            context.register(new MBeanExposer());
        }

        return monitoringEnabled;
    }

    /**
     * Set whether the feature should also enable exposure of monitoring statistics MBeans.
     * The set value can be overwritten by the definition of the property
     * {@link org.glassfish.jersey.server.ServerProperties#MONITORING_STATISTICS_MBEANS_ENABLED}.
     *
     * @param mBeansEnabled {@code true} is monitoring MBeans should be exposed.
     */
    public void setmBeansEnabled(boolean mBeansEnabled) {
        this.mBeansEnabled = mBeansEnabled;
    }

    private static class ApplicationInfoInjectionFactory extends ReferencingFactory<ApplicationInfo> {

        /**
         * Create new referencing injection factory.
         *
         * @param referenceFactory reference provider backing the factory.
         */
        @Inject
        public ApplicationInfoInjectionFactory(Provider<Ref<ApplicationInfo>> referenceFactory) {
            super(referenceFactory);
        }

    }

    private static class StatisticsInjectionFactory extends ReferencingFactory<MonitoringStatistics> {

        /**
         * Create new referencing injection factory.
         *
         * @param referenceFactory reference provider backing the factory.
         */
        @Inject
        public StatisticsInjectionFactory(Provider<Ref<MonitoringStatistics>> referenceFactory) {
            super(referenceFactory);
        }

        @Override
        public MonitoringStatistics get() {
            return super.get();
        }
    }

    private static class StatisticsListener implements MonitoringStatisticsListener {

        @Inject
        Provider<Ref<MonitoringStatistics>> statisticsFactory;

        @Override
        public void onStatistics(MonitoringStatistics statistics) {
            statisticsFactory.get().set(statistics);
        }
    }

}
