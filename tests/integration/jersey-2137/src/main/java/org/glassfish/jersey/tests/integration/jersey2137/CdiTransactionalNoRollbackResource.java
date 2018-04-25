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

package org.glassfish.jersey.tests.integration.jersey2137;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * Request scoped transactional CDI bean registered as JAX-RS resource class.
 * Part of JERSEY-2137 reproducer. {@link javax.ws.rs.WebApplicationException}
 * thrown in the resource method below should drive the response as specified
 * in the JAX-RS spec regardless
 * on the {@link javax.transaction.Transactional#dontRollbackOn()} value.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RequestScoped
@Transactional(dontRollbackOn = WebApplicationException.class)
@Path("cdi-transactional-no-rollback")
public class CdiTransactionalNoRollbackResource {

    @PersistenceContext(unitName = "Jersey2137PU")
    private EntityManager entityManager;

    @Path("{a}")
    @PUT
    public void putBalance(@PathParam("a") long a, String balance) {
        final Account account = entityManager.find(Account.class, a);
        if (account == null) {
            Account newAccount = new Account();
            newAccount.setId(a);
            newAccount.setBalance(Long.decode(balance));
            entityManager.persist(newAccount);
            throw new WebApplicationException(Response.ok("New accout created.").build());
        } else {
            account.setBalance(Long.decode(balance));
            entityManager.merge(account);
            throw new WebApplicationException(Response.ok("Balance updated.").build());
        }
    }
}
