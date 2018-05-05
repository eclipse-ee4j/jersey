/*
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.microprofile.rest.client.cdi;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

public class RestClientCDIExtension implements Extension {

    private final Set<Class<?>> validRestClientClasses = new LinkedHashSet<>();

    private final Set<Class<?>> invalidRestClientClasses = new LinkedHashSet<>();

    public void registerClient(
                    @Observes
                    @WithAnnotations(RegisterRestClient.class)
                    ProcessAnnotatedType<?> processAnnotatedType) {
        Class<?> restClient = processAnnotatedType.getAnnotatedType().getJavaClass();
        if (restClient.isInterface()) {
            validRestClientClasses.add(restClient);
            processAnnotatedType.veto();
        } else {
            invalidRestClientClasses.add(restClient);
        }
    }

    public void createProxy(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        validRestClientClasses.stream()
                .map(restClient -> new RestClientProducer(restClient, beanManager))
                .forEach(afterBeanDiscovery::addBean);
    }

    public void reportErrors(@Observes AfterDeploymentValidation afterDeploymentValidation) {
        invalidRestClientClasses.stream()
                .map(restClient -> new IllegalArgumentException(
                String.format("Rest Client [%s] must be interface", restClient)
        ))
                .forEach(afterDeploymentValidation::addDeploymentProblem);
    }
}
