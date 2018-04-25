/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark.util.tx;

import javax.ws.rs.WebApplicationException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * @author Paul Sandoz
 */
public final class TransactionManager {

    public static void manage(Transactional t) {
        UserTransaction utx = getUtx();
        try {
            utx.begin();
            if (t.joinTransaction) {
                t.em.joinTransaction();
            }
            t.transact();
            utx.commit();
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (SystemException se) {
                throw new WebApplicationException(se);
            }
            throw new WebApplicationException(e);
        } finally {
            t.em.close();
        }
    }

    private static UserTransaction getUtx() {
        try {
            InitialContext ic = new InitialContext();
            return (UserTransaction) ic.lookup("java:comp/UserTransaction");
        } catch (NamingException ne) {
            throw new WebApplicationException(ne);
        }
    }
}
