/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Following the visitor pattern, this interface allows implementing processors
 * traversing all abstract model components present in a given model.
 *
 * @see ResourceModelComponent
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface ResourceModelVisitor {

    /**
     * Start visiting a single resource model.
     *
     * @param resource resource model.
     */
    void visitResource(Resource resource);

    /**
     *  Start visiting a single child resource model.
     * @param resource child resource model.
     */
    void visitChildResource(Resource resource);

    /**
     * Visit a single resource method model.
     *
     * @param method resource method model.
     */
    void visitResourceMethod(ResourceMethod method);

    /**
     * Visit a single resource method invocable model.
     *
     * @param invocable resource method invocable model.
     */
    void visitInvocable(Invocable invocable);

    /**
     * Visit a single resource method handler model.
     *
     * @param methodHandler resource method handler model.
     */
    void visitMethodHandler(MethodHandler methodHandler);

    /**
     * Process a resource method handler constructor.
     *
     * Typically a constructor of a JAX-RS annotated resource class.
     *
     * @param constructor resource method handler constructor.
     */
    void visitResourceHandlerConstructor(HandlerConstructor constructor);

    /**
     * Process a resource model.
     *
     * @param resourceModel resource model.
     */
    void visitResourceModel(ResourceModel resourceModel);

    /**
     * Process a runtime resource model.
     * @param runtimeResource runtime resource model.
     */
    void visitRuntimeResource(RuntimeResource runtimeResource);
}
