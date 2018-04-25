/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.security.PrivilegedAction;

import javax.ws.rs.core.SecurityContext;

/**
 * Security context that allows establishing a subject before a resource method
 * or a sub-resource locator is called. Container or filters should set an
 * implementation of this interface to the request context using
 * {@link ContainerRequest#setSecurityContext(javax.ws.rs.core.SecurityContext)}.
 *
 * When Jersey detects this kind of context is in the request scope,
 * it will use {@link #doAsSubject(java.security.PrivilegedAction)} method to
 * dispatch the request to a resource method (or to call a sub-resource locator).
 *
 * @author Martin Matula
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface SubjectSecurityContext extends SecurityContext {

    /**
     * Jersey wraps calls to resource methods and sub-resource locators in
     * {@link PrivilegedAction} instance and passes it to this method when
     * dispatching a request. Implementations should do the needful to establish
     * a {@link javax.security.auth.Subject} and invoke the {@link PrivilegedAction}
     * passed as the parameter using
     * {@link javax.security.auth.Subject#doAs(javax.security.auth.Subject, java.security.PrivilegedAction)}.
     * <p>
     * The privileged action passed into the method may, when invoked, fail with either
     * {@link javax.ws.rs.WebApplicationException} or {@link javax.ws.rs.ProcessingException}.
     * Both these exceptions must be propagated to the caller without a modification.
     * </p>
     *
     * @param action {@link PrivilegedAction} that represents a resource or sub-resource locator
     *               method invocation to be executed by this method after establishing a subject.
     * @return result of the action.
     * @throws NullPointerException if the {@code PrivilegedAction} is {@code null}.
     * @throws SecurityException    if the caller does not have permission to invoke the
     *                              {@code Subject#doAs(Subject, PrivilegedAction)} method.
     * @throws javax.ws.rs.WebApplicationException
     *                              propagated exception from the privileged action. May be thrown in case the invocation
     *                              of resource or sub-resource locator method in the privileged action results in
     *                              this exception.
     * @throws javax.ws.rs.ProcessingException
     *                              propagated exception from the privileged action. May be thrown in case the invocation
     *                              of resource or sub-resource locator method in the privileged action has failed
     *                              or resulted in a non-checked exception.
     */
    public Object doAsSubject(PrivilegedAction action);
}
