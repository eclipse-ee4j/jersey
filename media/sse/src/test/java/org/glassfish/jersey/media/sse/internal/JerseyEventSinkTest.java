/*
 * Copyright (c) 2017, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.sse.internal;

import org.glassfish.jersey.media.sse.OutboundEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Bucek
 */
public class JerseyEventSinkTest {

    @Test
    public void test() throws Exception {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            JerseyEventSink eventSink = new JerseyEventSink(null);

            eventSink.close();
            eventSink.send(null);
        });
    }
}
