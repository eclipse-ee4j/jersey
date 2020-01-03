/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client;

import javax.ws.rs.client.CompletionStageRxInvoker;

/**
 * Implementation of Reactive Invoker for {@code CompletionStage}.
 *
 * This class allows for using {@link javax.ws.rs.client.InvocationCallback} in
 * {@link javax.ws.rs.client.Invocation.Builder#rx(Class) Invocation.Builder.rx(JerseyCompletionStageRxInvoker.class)}
 * requests.
 *
 * @author Michal Gajdos
 * @since 2.26
 */
public class JerseyCompletionStageRxInvoker extends JerseyInvocation.AsyncInvoker implements CompletionStageRxInvoker {
    JerseyCompletionStageRxInvoker(JerseyInvocation.Builder builder) {
        super(builder);
    }
}