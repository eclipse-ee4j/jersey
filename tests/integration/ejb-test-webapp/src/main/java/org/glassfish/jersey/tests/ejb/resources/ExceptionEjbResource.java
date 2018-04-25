/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.ejb.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.ejb.EJBException;
import javax.ejb.Singleton;

/**
 * EJB backed JAX-RS resource to test if a custom exception info makes it to the client.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Singleton
@Path("exception")
public class ExceptionEjbResource {

    public static class MyCheckedException extends Exception {

        public MyCheckedException(String message) {
            super(message);
        }
    }

    public static class MyRuntimeException extends RuntimeException {

        public MyRuntimeException(String message) {
            super(message);
        }
    }

    public static final String EjbExceptionMESSAGE = "ejb exception thrown directly";
    public static final String CheckedExceptionMESSAGE = "checked exception thrown directly";

    @GET
    @Path("ejb")
    public String throwEjbException() {
        throw new EJBException(EjbExceptionMESSAGE);
    }

    @GET
    @Path("checked")
    public String throwCheckedException() throws MyCheckedException {
        throw new MyCheckedException(CheckedExceptionMESSAGE);
    }

    @GET
    @Path("custom1/{p}")
    public String throwCustomExceptionOne() throws CustomBaseException {
        throw new CustomExceptionOne();
    }

    @GET
    @Path("custom2/{p}")
    public String throwCustomExceptionTwo() throws CustomBaseException {
        throw new CustomExceptionTwo();
    }
}
