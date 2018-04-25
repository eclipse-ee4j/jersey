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

import java.util.Date;
import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Monitoring configuration of an application.
 * <p/>
 * Application info instance can be injected, e.g:
 * <pre>
 *   &#064;Path("resource")
 *   public static class ApplicationInfoTest {
 *       &#064;Inject
 *       Provider&lt;ApplicationInfo&gt; applicationInfoProvider;
 *
 *       &#064;GET
 *       public String getAppName() throws InterruptedException {
 *           final ApplicationInfo applicationInfo = appInfoProvider.get();
 *           final String name = applicationInfo.getResourceConfig().getApplicationName();
 *
 *           return name;
 *       }
 *   }
 * </pre>
 * Note usage of {@link javax.inject.Provider} to retrieve application info. Info changes over time and this will
 * inject the latest info. In the case of singleton resources usage of {@code Provider} is the only way how
 * to inject application info that are up to date.
 * <p/>
 * Application info retrieved from Jersey runtime might be mutable and thanks to it might provide inconsistent data
 * as not all attributes are updated in the same time. To retrieve the immutable and consistent
 * data the method {@link #snapshot()} should be used.
 *
 * @author Miroslav Fuksa
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @see ApplicationEvent
 * @see ApplicationEventListener
 * @see MonitoringStatistics See MonitoringStatistics class for general details about statistics.
 * @since 2.12
 */
public interface ApplicationInfo {
    /**
     * Get the resource config.
     *
     * @return Resource config.
     */
    public ResourceConfig getResourceConfig();

    /**
     * Get the start time of the application.
     *
     * @return Time when an application initialization has been finished.
     */
    public Date getStartTime();

    /**
     * Get resource classes registered by the user in the current application. The set contains only
     * user resource classes and not resource classes added by Jersey
     * or by {@link org.glassfish.jersey.server.model.ModelProcessor}.
     * <p/>
     * User resources are resources that
     * were explicitly registered by the configuration, discovered by the class path scanning or that
     * constructs explicitly registered {@link org.glassfish.jersey.server.model.Resource programmatic resource}.
     *
     * @return Resource user registered classes.
     */
    public Set<Class<?>> getRegisteredClasses();

    /**
     * Get resource instances registered by the user in the current application. The set contains only
     * user resources and not resources added by Jersey
     * or by {@link org.glassfish.jersey.server.model.ModelProcessor}.
     * <p/>
     * User resources are resources that
     * were explicitly registered by the configuration, discovered by the class path scanning or that
     * constructs explicitly registered {@link org.glassfish.jersey.server.model.Resource programmatic resource}.
     *
     * @return Resource instances registered by user.
     */
    public Set<Object> getRegisteredInstances();

    /**
     * Get registered providers available in the runtime. The registered providers
     * are providers like {@link org.glassfish.jersey.server.model.MethodList.Filter filters},
     * {@link javax.ws.rs.ext.ReaderInterceptor reader} and {@link javax.ws.rs.ext.WriterInterceptor writer}
     * interceptors which are explicitly registered by configuration, or annotated by
     * {@link javax.ws.rs.ext.Provider @Provider} or registered in META-INF/services. The
     * set does not include providers that are by default built in Jersey.
     *
     * @return Set of provider classes.
     */
    public Set<Class<?>> getProviders();

    /**
     * Get the immutable consistent snapshot of the application info. Working with snapshots might
     * have negative performance impact as snapshot must be created but ensures consistency of data over time.
     * However, the usage of snapshot is encouraged to avoid working with inconsistent data. Not all attributes
     * must be updated in the same time on mutable version of info.
     *
     * @return Snapshot of application info.
     */
    public ApplicationInfo snapshot();
}

