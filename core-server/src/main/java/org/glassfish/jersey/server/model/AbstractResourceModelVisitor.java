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

/**
 * Abstract implementation of {@link ResourceModelVisitor resource model visitor} containing empty implementations
 * of interface methods. This class can be derived by validator implementing only methods needed for specific
 * validations.
 *
 * @author Miroslav Fuksa
 *
 */
public abstract class AbstractResourceModelVisitor implements ResourceModelVisitor {
    @Override
    public void visitResource(Resource resource) {
    }

    @Override
    public void visitChildResource(Resource resource) {
    }

    @Override
    public void visitResourceMethod(ResourceMethod method) {
    }

    @Override
    public void visitInvocable(Invocable invocable) {
    }

    @Override
    public void visitMethodHandler(MethodHandler methodHandler) {
    }

    @Override
    public void visitResourceHandlerConstructor(HandlerConstructor constructor) {
    }

    @Override
    public void visitResourceModel(ResourceModel resourceModel) {
    }

    @Override
    public void visitRuntimeResource(RuntimeResource runtimeResource) {
    }
}
