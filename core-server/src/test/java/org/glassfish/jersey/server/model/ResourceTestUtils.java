/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Utils for {@link Resource resource} testing.
 *
 * @author Miroslav Fuksa
 *
 */
public class ResourceTestUtils {

    public static void containsExactMethods(Resource resource, boolean shouldContainLocator, String... httpMethods) {
        assertEquals(shouldContainLocator, resource.getResourceLocator() != null);
        for (String httpMethod : httpMethods) {
            containsMethod(resource, httpMethod);
        }
        assertEquals(httpMethods.length, resource.getResourceMethods().size());
    }

    public static void containsMethod(Resource resource, String httpMethod) {
        for (ResourceMethod method : resource.getResourceMethods()) {
            if (method.getHttpMethod().equals(httpMethod)) {
                return;
            }
        }
        fail("Resource " + resource + " does not contain resource method " + httpMethod + "!");
    }

    public static Resource getResource(List<Resource> resources, String path) {
        for (Resource resource : resources) {
            if (resource.getPath().equals(path)) {
                return resource;
            }
        }
        fail("Resource with path '" + path + "' is not in the list of resources " + resources + "!");
        return null;
    }


    public static RuntimeResource getRuntimeResource(List<RuntimeResource> resources, String regex) {
        for (RuntimeResource resource : resources) {
            if (resource.getRegex().equals(regex)) {
                return resource;
            }
        }
        fail("RuntimeResource with regex '" + regex + "' is not in the list of runtime resources " + resources + "!");
        return null;
    }

    public static void containsExactMethods(RuntimeResource resource, boolean shouldContainLocator, String... httpMethods) {
        assertEquals(shouldContainLocator, resource.getResourceLocators().size() == 1);
        for (String httpMethod : httpMethods) {
            containsMethod(resource, httpMethod);
        }
        assertEquals(httpMethods.length, resource.getResourceMethods().size());
    }

    public static void containsMethod(RuntimeResource resource, String httpMethod) {
        for (ResourceMethod method : resource.getResourceMethods()) {
            if (method.getHttpMethod().equals(httpMethod)) {
                return;
            }
        }
        fail("RuntimeResource " + resource + " does not contain resource method " + httpMethod + "!");
    }


}
