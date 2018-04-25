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

import java.io.IOException;

import javax.ejb.EJB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Standalone Servlet instance that has nothing to do with Jersey.
 * It helps to compare Jersey and non-Jersey specific exception handling
 * processing.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@WebServlet(name = "StandaloneServlet", urlPatterns = {"/servlet"})
public class StandaloneServlet extends HttpServlet {

    static final String ThrowCheckedExceptionACTION = "throwCheckedException";
    static final String ThrowEjbExceptionACTION = "throwEjbException";

    @EJB ExceptionEjbResource ejbResource;

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request.
     * @param response servlet response.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        final String action = request.getParameter("action");

        if (ThrowCheckedExceptionACTION.equals(action)) {
            try {
                ejbResource.throwCheckedException();
            } catch (ExceptionEjbResource.MyCheckedException ex) {
                throw new ServletException(ex);
            }
        }

        if (ThrowEjbExceptionACTION.equals(action)) {
            ejbResource.throwEjbException();
        }

        sayHello(response);
    }

    private void sayHello(HttpServletResponse response) throws IOException {
        response.setHeader("Content-type", "text/plain");
        response.getOutputStream().print(
                String.format("Use action parameter to specify exception."
                + " \nSupported options: %s, %s.", ThrowCheckedExceptionACTION, ThrowEjbExceptionACTION));
    }
}
