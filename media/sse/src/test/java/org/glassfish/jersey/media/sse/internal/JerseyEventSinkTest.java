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

package org.glassfish.jersey.media.sse.internal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class JerseyEventSinkTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void onSubscribe() throws Exception {
        JerseyEventSink eventSink = new JerseyEventSink(null);

        eventSink.close();
        thrown.expect(IllegalStateException.class);
        eventSink.onSubscribe(null);
    }

    @Test
    public void onNext() throws Exception {
        JerseyEventSink eventSink = new JerseyEventSink(null);

        eventSink.close();
        thrown.expect(IllegalStateException.class);
        eventSink.onNext(null);
    }

    @Test
    public void onError() throws Exception {
        JerseyEventSink eventSink = new JerseyEventSink(null);

        eventSink.close();
        thrown.expect(IllegalStateException.class);
        eventSink.onError(null);
    }

    @Test
    public void onComplete() throws Exception {
        JerseyEventSink eventSink = new JerseyEventSink(null);

        eventSink.close();
        thrown.expect(IllegalStateException.class);
        eventSink.onComplete();
    }

    @Test
    public void test() throws Exception {
        JerseyEventSink eventSink = new JerseyEventSink(null);

        eventSink.close();
        thrown.expect(IllegalStateException.class);
        eventSink.send(null);
    }
}
