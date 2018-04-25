/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey;

import javax.ws.rs.Priorities;

/**
 * Built-in Jersey-specific priority constants to be used along with {@link javax.ws.rs.Priorities} where finer-grained
 * categorization is required.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class JerseyPriorities {

    private JerseyPriorities() {
        // prevents instantiation
    }

    /**
     * Priority for components that have to be called AFTER message encoders/decoders filters/interceptors.
     * The constant has to be higher than {@link javax.ws.rs.Priorities#ENTITY_CODER} in order to force the
     * processing after the components with {@code Priorities.ENTITY_CODER} are processed.
     */
    public static final int POST_ENTITY_CODER = Priorities.ENTITY_CODER + 100;
}
