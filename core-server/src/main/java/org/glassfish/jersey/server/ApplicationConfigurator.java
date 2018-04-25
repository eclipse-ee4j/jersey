/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.core.Application;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.server.spi.ComponentProvider;

/**
 * Configurator which initializes and register {@link Application} instance into {@link InjectionManager} and
 * {@link BootstrapBag}.
 *
 * @author Petr Bouda
 */
class ApplicationConfigurator implements BootstrapConfigurator {

    private Application application;
    private Class<? extends Application> applicationClass;

    /**
     * Initialize {@link Application} from provided instance.
     *
     * @param application application instance.
     */
    ApplicationConfigurator(Application application) {
        this.application = application;
    }

    /**
     * Initialize {@link Application} from provided class.
     *
     * @param applicationClass application class.
     */
    ApplicationConfigurator(Class<? extends Application> applicationClass) {
        this.applicationClass = applicationClass;
    }

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;
        Application resultApplication;

        // ApplicationConfigurer was created with an Application instance.
        if (application != null) {
            if (application instanceof ResourceConfig) {
                ResourceConfig rc = (ResourceConfig) application;
                if (rc.getApplicationClass() != null) {
                    rc.setApplication(createApplication(
                            injectionManager, rc.getApplicationClass(), serverBag.getComponentProviders()));
                }
            }
            resultApplication = application;

        // ApplicationConfigurer was created with an Application class.
        } else {
            resultApplication = createApplication(injectionManager, applicationClass, serverBag.getComponentProviders());
        }

        serverBag.setApplication(resultApplication);
        injectionManager.register(Bindings.service(resultApplication).to(Application.class));
    }

    private static Application createApplication(
            InjectionManager injectionManager,
            Class<? extends Application> applicationClass,
            Value<Collection<ComponentProvider>> componentProvidersValue) {
        // need to handle ResourceConfig and Application separately as invoking forContract() on these
        // will trigger the factories which we don't want at this point
        if (applicationClass == ResourceConfig.class) {
            return new ResourceConfig();
        } else if (applicationClass == Application.class) {
            return new Application();
        } else {
            Collection<ComponentProvider> componentProviders = componentProvidersValue.get();
            boolean appClassBound = false;
            for (ComponentProvider cp : componentProviders) {
                if (cp.bind(applicationClass, Collections.emptySet())) {
                    appClassBound = true;
                    break;
                }
            }
            if (!appClassBound) {
                if (applicationClass.isAnnotationPresent(Singleton.class)) {
                    injectionManager.register(Bindings.serviceAsContract(applicationClass).in(Singleton.class));
                    appClassBound = true;
                }
            }
            final Application app = appClassBound
                    ? injectionManager.getInstance(applicationClass)
                    : injectionManager.createAndInitialize(applicationClass);
            if (app instanceof ResourceConfig) {
                final ResourceConfig _rc = (ResourceConfig) app;
                final Class<? extends Application> innerAppClass = _rc.getApplicationClass();
                if (innerAppClass != null) {
                    Application innerApp = createApplication(injectionManager, innerAppClass, componentProvidersValue);
                    _rc.setApplication(innerApp);
                }
            }
            return app;
        }
    }
}
