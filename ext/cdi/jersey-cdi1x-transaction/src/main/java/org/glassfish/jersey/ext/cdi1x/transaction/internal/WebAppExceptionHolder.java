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

package org.glassfish.jersey.ext.cdi1x.transaction.internal;

import java.io.Serializable;

import javax.ws.rs.WebApplicationException;

import javax.enterprise.context.RequestScoped;
import javax.transaction.Transactional;

import org.glassfish.jersey.ext.cdi1x.internal.JerseyVetoed;

/**
 * CDI bean to store any {@link WebApplicationException}
 * thrown in a {@link Transactional} CDI bean for later use
 * in {@link TransactionalExceptionMapper}.
 *
 * @author Jakub.Podlesak (jakub.podlesak at oracle.com)
 */
@RequestScoped
@JerseyVetoed
@TransactionalExceptionInterceptorProvider.WaeQualifier
public class WebAppExceptionHolder implements Serializable {

    private static final long serialVersionUID = 31415926535879L;

    private WebApplicationException exception;

    /* package */ void setException(WebApplicationException exception) {
        this.exception = exception;
    }

    /* package */ WebApplicationException getException() {
        return exception;
    }
}
