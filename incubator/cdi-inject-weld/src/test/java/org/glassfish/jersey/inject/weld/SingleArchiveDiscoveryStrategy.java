/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategy;
import org.jboss.weld.environment.deployment.discovery.ReflectionDiscoveryStrategy;
import org.jboss.weld.resources.spi.ResourceLoader;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Prevent loading the archives multiple times, once from the classpath, once from the module path.
 * Done by Thread.currentThread().getContextClassLoader().getResource("META-INF/beans.xml")
 */
public class SingleArchiveDiscoveryStrategy extends ReflectionDiscoveryStrategy implements DiscoveryStrategy {

    public SingleArchiveDiscoveryStrategy(){
        super(null, null, null, null);
    }

    public SingleArchiveDiscoveryStrategy(ResourceLoader resourceLoader,
                                          Bootstrap bootstrap,
                                          Set<Class<? extends Annotation>> initialBeanDefiningAnnotations,
                                          BeanDiscoveryMode emptyBeansXmlDiscoveryMode) {
        super(resourceLoader, bootstrap, initialBeanDefiningAnnotations, emptyBeansXmlDiscoveryMode);
    }

    @Override
    public Set<WeldBeanDeploymentArchive> performDiscovery() {
        if (scanner == null) {
            scanner = new SingleArchiveScanner(resourceLoader, bootstrap, BeanDiscoveryMode.ANNOTATED);
        }
        return super.performDiscovery();
    }
}
