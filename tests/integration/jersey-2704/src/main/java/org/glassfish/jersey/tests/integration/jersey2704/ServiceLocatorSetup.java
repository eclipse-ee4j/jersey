/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2704;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.tests.integration.jersey2704.services.HappyService;


/**
 * This class is to listen for {@link ServletContextEvent} generated whenever context
 * is initialized and set {@link ServletProperties#SERVICE_LOCATOR} attribute to point
 * {@link ServiceLocator} pre-populated with {@link HappyService} instance.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ServiceLocatorSetup implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServiceLocator locator = ServiceLocatorUtilities.createAndPopulateServiceLocator();
        ServiceLocatorUtilities.addOneConstant(locator, new HappyService());
        event.getServletContext().setAttribute(ServletProperties.SERVICE_LOCATOR, locator);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
