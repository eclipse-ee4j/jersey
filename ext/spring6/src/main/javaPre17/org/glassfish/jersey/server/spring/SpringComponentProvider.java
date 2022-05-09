/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.server.spi.ComponentProvider;

import java.util.Set;

/**
 * Custom ComponentProvider class.
 * Responsible for 1) bootstrapping Jersey 3 Spring integration and
 * 2) making Jersey skip JAX-RS Spring component life-cycle management and leave it to us.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public class SpringComponentProvider implements ComponentProvider {

    @Override
    public void initialize(InjectionManager injectionManager) {
        if (JdkVersion.getJdkVersion().getMajor() < 17) {
            throw new IllegalStateException(LocalizationMessages.NOT_SUPPORTED());
        }
    }

    @Override
    public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {
        return false;
    }

    @Override
    public void done() {

    }
}
