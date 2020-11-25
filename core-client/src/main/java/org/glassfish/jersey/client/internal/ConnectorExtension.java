/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.internal;

import org.glassfish.jersey.client.ClientRequest;

/**
 * Connector extension interface to extend existing connector's functionality.
 *
 * @param <T> type of connection to be extended/processed
 * @param <E> type of exception which can be thrown while processing/handling exeption
 *
 * @since 2.33
 */
interface ConnectorExtension<T, E extends Exception> {

    /**
     * Main function which allows extension of connector's functionality
     *
     * @param request request instance to work with (shall contain all required settings/params to be used in extension)
     * @param extensionParam connector's instance which is being extended
     */
    void invoke(ClientRequest request, T extensionParam);

    /**
     * After connection is done some additional work may be done
     *
     * @param extensionParam connector's instance which is being extended
     */
    void postConnectionProcessing(T extensionParam);

    /**
     * Exception handling method
     *
     * @param request request instance to work with (shall contain all required settings/params to be used in extension)
     * @param ex exception instance which comes from connector
     * @param extensionParam connector's instance which is being extended
     * @return true if exception was handled by this method, false otherwise
     * @throws E can thor exception if required by handling
     */
    boolean handleException(ClientRequest request, T extensionParam, E ex) throws E;

}