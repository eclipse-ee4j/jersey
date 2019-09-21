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

package org.glassfish.jersey.tests.integration.jersey2551;

import javax.inject.Singleton;

import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;

import org.jvnet.hk2.external.generator.ServiceLocatorGeneratorImpl;
import org.jvnet.hk2.internal.DefaultClassAnalyzer;
import org.jvnet.hk2.internal.DynamicConfigurationImpl;
import org.jvnet.hk2.internal.DynamicConfigurationServiceImpl;
import org.jvnet.hk2.internal.ServiceLocatorImpl;
import org.jvnet.hk2.internal.Utilities;

/**
 * @author Michal Gajdos
 */
public class ServiceLocatorGenerator extends ServiceLocatorGeneratorImpl {

    @Override
    public ServiceLocator create(final String name, final ServiceLocator parent) {
        if (parent != null && !(parent instanceof ServiceLocatorImpl)) {
            throw new AssertionError("parent must be a " + ServiceLocatorImpl.class.getName()
                    + " instead it is a " + parent.getClass().getName());
        }

        final ServiceLocatorImpl sli = new CustomServiceLocator(name, (ServiceLocatorImpl) parent);

        final DynamicConfigurationImpl dci = new DynamicConfigurationImpl(sli);

        // The service locator itself
        dci.bind(Utilities.getLocatorDescriptor(sli));

        // The injection resolver for three thirty
        dci.addActiveDescriptor(Utilities.getThreeThirtyDescriptor(sli));

        // The dynamic configuration utility
        dci.bind(BuilderHelper.link(DynamicConfigurationServiceImpl.class, false)
                .to(DynamicConfigurationService.class)
                .in(Singleton.class.getName())
                .localOnly()
                .build());

        dci.bind(BuilderHelper.createConstantDescriptor(
                new DefaultClassAnalyzer(sli)));

        dci.commit();

        return sli;
    }

    class CustomServiceLocator extends ServiceLocatorImpl {

        public CustomServiceLocator(final String name, final ServiceLocatorImpl parent) {
            super(name, parent);
        }
    }
}
