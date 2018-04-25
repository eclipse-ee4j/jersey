/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.process;

import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Wrapper that holds the reference of the {@link RequestProcessingContext}. This class helps to get the current request scoped
 * object without wrapping using the proxy. Outer wrapper can be proxied but inner reference object still remains the direct
 * reference.
 *
 * @author Petr Bouda
 */
@RequestScoped
public class RequestProcessingContextReference implements Ref<RequestProcessingContext> {

    private RequestProcessingContext processingContext;

    @Override
    public void set(RequestProcessingContext processingContext) {
        this.processingContext = processingContext;
    }

    @Override
    public RequestProcessingContext get() {
        return processingContext;
    }
}
