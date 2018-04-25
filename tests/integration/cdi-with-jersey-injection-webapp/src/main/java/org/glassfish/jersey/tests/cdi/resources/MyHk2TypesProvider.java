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

package org.glassfish.jersey.tests.cdi.resources;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.jersey.ext.cdi1x.spi.Hk2CustomBoundTypesProvider;

/**
 * Tell Jersey CDI extension what types should be bridged from HK2 to CDI.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class MyHk2TypesProvider implements Hk2CustomBoundTypesProvider {

    @Override
    public Set<Type> getHk2Types() {
        return new HashSet<Type>() {{
            add(MyApplication.MyInjection.class);
        }};
    }
}
