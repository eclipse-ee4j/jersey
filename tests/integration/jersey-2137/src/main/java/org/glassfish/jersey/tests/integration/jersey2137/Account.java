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

package org.glassfish.jersey.tests.integration.jersey2137;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entity bean that maintains information on account balance.
 * This is to help determine if rollback happens or not, when
 * entity bean is accessed from a transactional CDI beans.
 * Entity beans have implicit JTA support, so this will
 * save us some lines of code.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Entity
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    /**
     * Get the account id.
     *
     * @return account id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the account id.
     *
     * @param id account id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    private long balance;

    /**
     * Get the value of balance
     *
     * @return the value of balance
     */
    public long getBalance() {
        return balance;
    }

    /**
     * Set the value of balance
     *
     * @param balance new value of balance
     */
    public void setBalance(long balance) {
        this.balance = balance;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Account)) {
            return false;
        }
        Account other = (Account) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.glassfish.jersey.tests.integration.jersey2137.Account[ id=" + id + " ]";
    }
}
