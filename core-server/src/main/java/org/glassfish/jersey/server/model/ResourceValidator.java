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

package org.glassfish.jersey.server.model;

import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Validator ensuring that resource are correct (for example that root resources contains path, etc.).
 *
 * @author Miroslav Fuksa
 */
class ResourceValidator extends AbstractResourceModelVisitor {

    @Override
    public void visitResource(final Resource resource) {
        checkResource(resource);
    }

    private void checkResource(final Resource resource) {
        if (!resource.getResourceMethods().isEmpty() && resource.getResourceLocator() != null) {
            Errors.warning(resource, LocalizationMessages.RESOURCE_CONTAINS_RES_METHODS_AND_LOCATOR(resource,
                    resource.getPath()));
        }

        if (resource.getPath() != null
                && resource.getResourceMethods().isEmpty()
                && resource.getChildResources().isEmpty()
                && resource.getResourceLocator() == null) {
            Errors.warning(resource, LocalizationMessages.RESOURCE_EMPTY(resource, resource.getPath()));
        }

    }

    @Override
    public void visitChildResource(Resource resource) {
        checkResource(resource);
    }
}
