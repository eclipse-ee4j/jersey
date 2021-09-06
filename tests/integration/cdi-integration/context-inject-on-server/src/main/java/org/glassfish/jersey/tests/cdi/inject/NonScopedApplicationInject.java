/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.inject;

import javax.ws.rs.Path;
import java.util.Collections;
import java.util.Set;

public class NonScopedApplicationInject extends ApplicationInjectParent {
    @Path(InjectionChecker.ROOT + "/nonscoped")
    public static class NonScopedResource extends ApplicationInjectParent.ResourceParent {

    }

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(NonScopedResource.class);
    }
}
