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

package org.glassfish.jersey.message.internal;

import java.util.Collection;

import org.glassfish.jersey.internal.PropertiesDelegate;

/**
 * Delegating properties delegate backed by another {@code PropertiesDelegate} with implemented "cache" or direct reference to
 * tracing support related classes (e.g. {@code TracingLogger}) to improve performance of lookup tracing context instance.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @since 2.3
 */
public final class TracingAwarePropertiesDelegate implements PropertiesDelegate {

    private final PropertiesDelegate propertiesDelegate;

    private TracingLogger tracingLogger;

    /**
     * Create new tracing aware properties delegate wrapper.
     *
     * @param propertiesDelegate wrapped delegate.
     */
    public TracingAwarePropertiesDelegate(PropertiesDelegate propertiesDelegate) {
        this.propertiesDelegate = propertiesDelegate;
    }

    @Override
    public void removeProperty(String name) {
        if (TracingLogger.PROPERTY_NAME.equals(name)) {
            tracingLogger = null;
        }
        propertiesDelegate.removeProperty(name);
    }

    @Override
    public void setProperty(String name, Object object) {
        if (TracingLogger.PROPERTY_NAME.equals(name)) {
            tracingLogger = (TracingLogger) object;
        }
        propertiesDelegate.setProperty(name, object);
    }

    @Override
    public Object getProperty(String name) {
        if (tracingLogger != null && TracingLogger.PROPERTY_NAME.equals(name)) {
            return tracingLogger;
        }
        return propertiesDelegate.getProperty(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return propertiesDelegate.getPropertyNames();
    }
}
