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

package org.glassfish.jersey.tests.integration.tracing;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@Provider
@PreMatching
@Priority(42)
public class PreMatchingContainerRequestFilter42 implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        //System.out.println("*** PreMatchingContainerRequestFilter42.filter");
        Utils.throwException(requestContext, this,
                Utils.TestAction.PRE_MATCHING_REQUEST_FILTER_THROW_WEB_APPLICATION,
                Utils.TestAction.PRE_MATCHING_REQUEST_FILTER_THROW_PROCESSING,
                Utils.TestAction.PRE_MATCHING_REQUEST_FILTER_THROW_ANY);
    }
}
