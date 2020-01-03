/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.jmockit.server;

import mockit.Mocked;
import mockit.Verifications;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.glassfish.jersey.tests.jmockit.server.innerstatic.InnerStaticClass;
import org.glassfish.jersey.tests.jmockit.server.toplevel.PublicRootResourceClass;
import org.glassfish.jersey.tests.jmockit.server.toplevelinnerstatic.PublicRootResourceInnerStaticClass;
import org.junit.Test;

/**
 * @author Pavel Bucek
 */
public class ResourceConfigTest {

    /**
     * Reproducer for OWLS-19790: Invalidate resource finders in resource config only when needed.
     */
    @Test
    public void testInvalidateResourceFinders(@Mocked final PackageNamesScanner scanner) throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .packages(false, PublicRootResourceClass.class.getPackage().getName());

        // Scan packages.
        resourceConfig.getClasses();

        // No reset.
        new Verifications() {{
            scanner.reset();
            times = 0;
        }};

        resourceConfig.register(InnerStaticClass.PublicClass.class);

        // Reset - we called getClasses() on ResourceConfig.
        new Verifications() {{
            scanner.reset();
            times = 1;
        }};

        // No reset.
        resourceConfig.register(PublicRootResourceClass.class);
        resourceConfig.register(PublicRootResourceInnerStaticClass.PublicClass.class);

        // No reset - simple registering does not invoke cache invalidation and reset of resource finders.
        new Verifications() {{
            scanner.reset();
            times = 1;
        }};

        // Scan packages.
        resourceConfig.getClasses();

        resourceConfig.registerFinder(new PackageNamesScanner(new String[] {"javax.ws.rs"}, false));

        // Reset - we called getClasses() on ResourceConfig.
        new Verifications() {{
            scanner.reset();
            times = 2;
        }};
    }
}
