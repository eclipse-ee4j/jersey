/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.monitoring;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link SlidingWindowTimeReservoir}.
 *
 * @author Stepan Vavra
 */
public class SlidingWindowTimeReservoirAggregatingTrimmerTest extends SlidingWindowTimeReservoirTest {

    protected SlidingWindowTimeReservoir slidingWindowTimeReservoir(final long now) {
        return new SlidingWindowTimeReservoir(10, TimeUnit.NANOSECONDS, now,
                TimeUnit.NANOSECONDS, new AggregatingTrimmer(now, TimeUnit.NANOSECONDS, 1, TimeUnit.SECONDS));
    }

}
