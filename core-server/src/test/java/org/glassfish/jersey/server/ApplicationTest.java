/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.junit.Test;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class ApplicationTest {

    @Path("test")
    public static class DummyResource {

    }

    @Test
    public void testGetClassesContainsNull() {
        Application a = new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return new HashSet<Class<?>>() {{
                    add(null);
                    add(DummyResource.class);
                }};
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.emptySet();
            }
        };

        new ApplicationHandler(a);
    }

    @Test
    public void testGetSingletonsContainsNull() {
        Application a = new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return new HashSet<Object>() {{
                    add(null);
                    add(new DummyResource());
                }};
            }
        };

        new ApplicationHandler(a);
    }

    @Test
    public void testGetSingletonsNull() {
        Application a = new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return Collections.emptySet();
            }

            @Override
            public Set<Object> getSingletons() {
                return null;
            }
        };

        new ApplicationHandler(a);
    }

    @Test
    public void testGetClassesNull() {
        Application a = new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                return null;
            }

            @Override
            public Set<Object> getSingletons() {
                return Collections.emptySet();
            }
        };

        new ApplicationHandler(a);
    }
}
