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

import org.glassfish.jersey.internal.inject.AnnotationLiteral;

/**
 * {@link ClientBackgroundScheduler} annotation literal.
 * <p>
 * This class provides a {@link #INSTANCE constant instance} of the {@code @ClientBackgroundScheduler} annotation to be used
 * in method calls that require use of annotation instances.
 * </p>
 *
 * @author Adam Lindenthal
 * @since 2.26
 */
@SuppressWarnings("ClassExplicitlyAnnotation")
public final class ClientBackgroundSchedulerLiteral extends AnnotationLiteral<ClientBackgroundScheduler>
        implements ClientBackgroundScheduler {

    /**
     * An {@link ClientBackgroundScheduler} annotation instance.
     */
    public static final ClientBackgroundScheduler INSTANCE = new ClientBackgroundSchedulerLiteral();

    private ClientBackgroundSchedulerLiteral() {
        // prevents instantiation from the outside.
    }
}
