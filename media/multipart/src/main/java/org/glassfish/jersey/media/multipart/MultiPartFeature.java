/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.media.multipart.internal.FormDataParamInjectionFeature;
import org.glassfish.jersey.media.multipart.internal.MultiPartReaderClientSide;
import org.glassfish.jersey.media.multipart.internal.MultiPartReaderServerSide;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

/**
 * Feature used to register Multipart providers.
 *
 * @author Michal Gajdos
 */
public class MultiPartFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        final RuntimeType runtime = context.getConfiguration().getRuntimeType();

        if (RuntimeType.SERVER.equals(runtime)) {
            context.register(FormDataParamInjectionFeature.class);
            context.register(MultiPartReaderServerSide.class);
        } else {
            context.register(MultiPartReaderClientSide.class);
        }

        context.register(MultiPartWriter.class);

        return true;
    }
}
