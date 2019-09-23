/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.inject.cdi.se;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Testing resource for CDI events.
 *
 * @author Petr Bouda
 */
@Path("account")
public class AccountResource {

    @Inject
    @Credit
    private Event<Long> creditEvent;

    @Inject
    @Debit
    private Event<Long> debitEvent;

    @Inject
    private Account account;

    @POST
    public void credit(@QueryParam("amount") long amount) {
        creditEvent.fire(amount);
    }

    @DELETE
    public void debit(@QueryParam("amount") long amount) {
        debitEvent.fire(amount);
    }

    @GET
    public long current() {
        return account.getCurrent();
    }
}
