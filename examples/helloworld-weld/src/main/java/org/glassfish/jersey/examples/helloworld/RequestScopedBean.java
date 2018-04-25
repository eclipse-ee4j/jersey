/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import javax.enterprise.context.RequestScoped;

/**
 * Request scoped CDI bean. Serves as a storage
 * for request scoped data to demonstrate that CDI based
 * JAX-RS interceptor, {@link CustomInterceptor}, could
 * use CDI means to obtain JAX-RS request data.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RequestScoped
public class RequestScopedBean {

    private String requestId;

    /**
     * Request ID setter.
     *
     * @param requestId
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Get me current request ID.
     *
     * @return request id.
     */
    public String getRequestId() {
        return requestId;
    }
}
