/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.lang.reflect.Method;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

/**
 * Contains information about method annotation {@link ClientHeaderParam}.
 *
 * @author David Kral
 */
class ClientHeaderParamModel {

    private final String headerName;
    private final String[] headerValue;
    private final Method computeMethod;
    private final boolean required;

    ClientHeaderParamModel(Class<?> iClass, ClientHeaderParam clientHeaderParam) {
        headerName = clientHeaderParam.name();
        headerValue = clientHeaderParam.value();
        computeMethod = InterfaceUtil.parseComputeMethod(iClass, headerValue);
        required = clientHeaderParam.required();
    }

    /**
     * Returns header name.
     *
     * @return header name
     */
    String getHeaderName() {
        return headerName;
    }

    /**
     * Returns header value.
     *
     * @return header value
     */
    String[] getHeaderValue() {
        return headerValue;
    }

    /**
     * Returns method which is used to compute header value.
     *
     * @return compute method
     */
    Method getComputeMethod() {
        return computeMethod;
    }

    /**
     * Returns true if header is required and false if not. It header is not required and exception
     * is thrown during compute method invocation, this header will be ignored and not included to request.
     *
     * @return if header is required
     */
    boolean isRequired() {
        return required;
    }
}
