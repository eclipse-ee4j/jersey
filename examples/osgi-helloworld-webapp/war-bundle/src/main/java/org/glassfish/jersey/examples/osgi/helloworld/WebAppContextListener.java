/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.osgi.helloworld;

import java.util.HashMap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * This is to make sure we signal the application has been deployed/un-deployed
 * via the OSGi EventAdmin service.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class WebAppContextListener implements BundleActivator, ServletContextListener {

    static EventAdmin ea;

    BundleContext bc;
    ServiceReference eaRef;

    static synchronized EventAdmin getEa() {
        return ea;
    }

    static synchronized void setEa(EventAdmin ea) {
        WebAppContextListener.ea = ea;
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        if (getEa() != null) {
            final String contextPath = sce.getServletContext().getContextPath();
            getEa().sendEvent(new Event("jersey/test/DEPLOYED", new HashMap<String, String>() {{
                put("context-path", contextPath);
            }}));
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        if (getEa() != null) {
            getEa().sendEvent(new Event("jersey/test/UNDEPLOYED", new HashMap<String, String>() {{
                put("context-path", sce.getServletContext().getContextPath());
            }}));
        }
    }

    @Override
    public void start(BundleContext context) throws Exception {
        bc = context;
        eaRef = bc.getServiceReference(EventAdmin.class.getName());
        if (eaRef != null) {
            setEa((EventAdmin) bc.getService(eaRef));
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (eaRef != null) {
            setEa(null);
            bc.ungetService(eaRef);
        }
    }
}
