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

package org.glassfish.jersey.server.spring.test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * AccountService implementation.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public class AccountServiceImpl implements AccountService {

    private Map<String, BigDecimal> accounts = new HashMap<>();
    private BigDecimal defaultAccountBalance;

    // JERSEY-2506 FIX VERIFICATION
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public void setAccountBalance(String accountId, BigDecimal balance) {
        accounts.put(accountId, balance);
    }

    @Override
    public BigDecimal getAccountBalance(String accountId) {
        BigDecimal balance = accounts.get(accountId);
        if (balance == null) {
            return defaultAccountBalance;
        }
        return balance;
    }

    public void setDefaultAccountBalance(String defaultAccountBalance) {
        this.defaultAccountBalance = new BigDecimal(defaultAccountBalance);
    }

    public String verifyServletRequestInjection() {
        return "PASSED: " + httpServletRequest.getServerName();
    }

}
