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

package org.glassfish.jersey.tests.integration.jersey3670;

import javax.ws.rs.core.Application;
import java.util.LinkedHashSet;
import java.util.Set;

public class MyApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        LinkedHashSet<Class<?>> classes = new LinkedHashSet<>();
        classes.add(MyResource.class);
        classes.add(MyConverterProvider.class);
        return classes;
    }

}