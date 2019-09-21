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
import java.util.Map;
import java.util.Set;

/**
 * Application MX Bean.
 *
 * @author Miroslav Fuksa
 */
public interface ApplicationMXBean {
    /**
     * Get the application name.
     *
     * @return Application name.
     */
    public String getApplicationName();

    /**
     * Get the {@link javax.ws.rs.core.Application application class} used for configuration of Jersey application.
     *
     * @return Application class name.
     */
    public String getApplicationClass();

    /**
     * Get the map of configuration properties converted to strings.
     *
     * @return Map property keys to property string values.
     */
    public Map<String, String> getProperties();

    /**
     * Get the start time of the application (when application was initialized).
     *
     * @return Application start time.
     */
    public Date getStartTime();

    /**
     * Get a set of string names of resource classes registered by the user.
     *
     * @return Set of classes full names (with package names).
     * @see org.glassfish.jersey.server.monitoring.ApplicationEvent#getRegisteredClasses() for specification
     *      of returned classes.
     */
    public Set<String> getRegisteredClasses();

    /**
     * Get a set of string names of classes of user registered instances.
     *
     * @return Set of user registered instances converted to their class full names (with package names).
     * @see org.glassfish.jersey.server.monitoring.ApplicationEvent#getRegisteredInstances()
     *      for specification of returned instances.
     */
    public Set<String> getRegisteredInstances();

    /**
     * Get classes of registered providers.
     *
     * @return Set of provider class full names (with packages names).
     * @see org.glassfish.jersey.server.monitoring.ApplicationEvent#getProviders() for specification
     *      of returned classes.
     */
    public Set<String> getProviderClasses();
}
