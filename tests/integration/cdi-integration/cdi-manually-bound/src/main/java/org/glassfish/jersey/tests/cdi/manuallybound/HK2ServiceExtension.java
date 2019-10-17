/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.manuallybound;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

/*
 * Replaces bean-discovery-mode="annotated" + @ApplicationScoped on HK2InjectedFilter
 */
public class HK2ServiceExtension implements Extension {
    public void observeAfterTypeDiscovery(@Observes AfterTypeDiscovery event, BeanManager beanManager) {
        event.addAnnotatedType(HK2InjectedFilter.class, "test-hk2service");
    }

    public void decorateAnnotatedType(@Observes ProcessAnnotatedType<HK2InjectedFilter> pat, BeanManager beanManager) {
        AnnotatedTypeConfigurator<HK2InjectedFilter> annotatedTypeConfigurator = pat.configureAnnotatedType();

        annotatedTypeConfigurator.filterFields(service -> service.getBaseType() == HK2Service.class).findFirst().get()
                .remove(a -> a.equals(InjectLiteral.INSTANCE));
    }
}
