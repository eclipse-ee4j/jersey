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

package org.glassfish.jersey.internal.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A helper class to aid the closing of {@link java.io.InputStream}.
 *
 * @author Paul Sandoz
 */
public class Closing {

    public static Closing with(final InputStream in) {
        return new Closing(in);
    }

    private final InputStream in;

    public Closing(final InputStream in) {
        this.in = in;
    }

    public void invoke(final Closure<InputStream> c) throws IOException {
        if (in == null) {
            return;
        }
        try {
            c.invoke(in);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }
}
