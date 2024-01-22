/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.binder.common;

import org.glassfish.jersey.inject.weld.ClientTestParent;
import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.MessageBodyFactory;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.spi.HeaderDelegateProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import java.util.Collections;
import java.util.List;

public class CoreCommonBindingsTest extends ClientTestParent {

    @Test
    public void testProviders() {
        injectionManager.completeRegistration();
        injectionManager.register(new MessagingBinders.HeaderDelegateProviders(RuntimeType.CLIENT));
        assertMultiple(HeaderDelegateProvider.class, 10, "DateProvider");
    }

    @Test
    public void testMessageBodyProviders() {
        injectionManager.completeRegistration();
        injectionManager.register(new MessagingBinders.MessageBodyProviders(Collections.emptyMap(), RuntimeType.CLIENT));
        assertMultiple(MessageBodyReader.class, 10, "StringMessageProvider");
        assertMultiple(MessageBodyWriter.class, 10, "StringMessageProvider");
    }
}
