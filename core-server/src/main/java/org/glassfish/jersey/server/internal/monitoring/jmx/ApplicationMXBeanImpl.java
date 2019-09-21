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

package org.glassfish.jersey.server.internal.monitoring.jmx;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.monitoring.ApplicationInfo;
import org.glassfish.jersey.server.monitoring.ApplicationMXBean;

/**
 * MXBean implementing {@link org.glassfish.jersey.server.monitoring.ApplicationMXBean} MXBean interface.
 *
 * @author Miroslav Fuksa
 */
public class ApplicationMXBeanImpl implements ApplicationMXBean {

    private final String applicationName;
    private final String applicationClass;
    private final Map<String, String> configurationProperties;
    private final Date startTime;
    private final Set<String> providers;
    private final Set<String> registeredClasses;
    private final Set<String> registeredInstances;

    /**
     * Create a new application MXBean and register it to the mbean server using {@code mBeanExposer}.
     *
     * @param applicationInfo Application info which should be exposed.
     * @param mBeanExposer MBean exposer.
     * @param parentName {@link javax.management.ObjectName Object name} prefix of parent mbeans.
     */
    public ApplicationMXBeanImpl(final ApplicationInfo applicationInfo, final MBeanExposer mBeanExposer,
                                 final String parentName) {
        this.providers = new HashSet<>();
        this.registeredClasses = new HashSet<>();
        this.registeredInstances = new HashSet<>();

        for (final Class<?> provider : applicationInfo.getProviders()) {
            this.providers.add(provider.getName());
        }

        for (final Class<?> registeredClass : applicationInfo.getRegisteredClasses()) {
            this.registeredClasses.add(registeredClass.toString());
        }

        for (final Object registeredInstance : applicationInfo.getRegisteredInstances()) {
            this.registeredInstances.add(registeredInstance.getClass().getName());
        }

        final ResourceConfig resourceConfig = applicationInfo.getResourceConfig();
        this.applicationName = resourceConfig.getApplicationName();
        this.applicationClass = resourceConfig.getApplication().getClass().getName();
        this.configurationProperties = new HashMap<>();
        for (final Map.Entry<String, Object> entry : resourceConfig.getProperties().entrySet()) {
            final Object value = entry.getValue();
            String stringValue;
            try {
                stringValue = (value == null) ? "[null]" : value.toString();
            } catch (final Exception e) { // See JERSEY-2053: Sometimes toString() throws exception...
                stringValue = LocalizationMessages.PROPERTY_VALUE_TOSTRING_THROWS_EXCEPTION(
                        e.getClass().getName(), e.getMessage());
            }
            configurationProperties.put(entry.getKey(), stringValue);
        }
        this.startTime = new Date(applicationInfo.getStartTime().getTime());

        mBeanExposer.registerMBean(this, parentName + ",global=Configuration");
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getApplicationClass() {
        return applicationClass;
    }

    @Override
    public Map<String, String> getProperties() {
        return configurationProperties;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Set<String> getRegisteredClasses() {
        return registeredClasses;
    }

    @Override
    public Set<String> getRegisteredInstances() {
        return registeredInstances;
    }

    @Override
    public Set<String> getProviderClasses() {
        return providers;
    }
}
