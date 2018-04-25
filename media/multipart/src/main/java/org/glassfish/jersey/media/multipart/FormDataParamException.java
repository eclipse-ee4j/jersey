/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ParamException;

/**
 * A parameter-based exception for errors with {@link FormDataParam}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public final class FormDataParamException extends ParamException {

    /**
     * Create new {@link FormDataParam} exception.
     *
     * @param cause              real cause.
     * @param name               parameter name.
     * @param defaultStringValue default value.
     */
    public FormDataParamException(final Throwable cause, final String name, final String defaultStringValue) {
        super(cause, Response.Status.BAD_REQUEST, FormDataParam.class, name, defaultStringValue);
    }
}
