/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.internal.managed;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.inject.weld.managed.CdiInjectionManagerFactory;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InjectionManagerTest extends TestParent {

    @Test
    public void injectionManagerTest() {
        injectionManager.completeRegistration();
        final InjectionManagerInjectedBean bean = injectionManager.getInstance(InjectionManagerInjectedBean.class);
        Assertions.assertNotNull(bean);
        InjectionManager got = bean.getInjectionManager();
        Assertions.assertEquals(injectionManager, got);

        final InjectionManager clientInjectionManager = new CdiInjectionManagerFactory().create(null, RuntimeType.CLIENT);
        clientInjectionManager.completeRegistration();
        final InjectionManagerInjectedBean clientBean = clientInjectionManager.getInstance(InjectionManagerInjectedBean.class);
        Assertions.assertNotNull(clientBean);
        InjectionManager gotClient = clientBean.getInjectionManager();
        Assertions.assertEquals(clientInjectionManager, gotClient);
    }

    @Dependent
    public static class InjectionManagerInjectedBean {
        @Inject
        InjectionManager injectionManager;

        InjectionManager getInjectionManager() {
            return injectionManager;
        }
    }

}
