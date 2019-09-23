/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.inject.AnnotationLiteral;

/**
 * {@link org.glassfish.jersey.client.ClientAsyncExecutor} annotation literal.
 * <p>
 * This class provides a {@link #INSTANCE constant instance} of the {@code @ClientAsyncExecutor} annotation to be used
 * in method calls that require use of annotation instances.
 * </p>
 *
 * @author Marek Potociar
 * @since 2.18
 */
@SuppressWarnings("ClassExplicitlyAnnotation")
public final class ClientAsyncExecutorLiteral extends AnnotationLiteral<ClientAsyncExecutor> implements ClientAsyncExecutor {

    /**
     * An {@link org.glassfish.jersey.client.ClientAsyncExecutor} annotation instance.
     */
    public static final ClientAsyncExecutor INSTANCE = new ClientAsyncExecutorLiteral();

    private ClientAsyncExecutorLiteral() {
        // prevents instantiation from the outside.
    }
}
