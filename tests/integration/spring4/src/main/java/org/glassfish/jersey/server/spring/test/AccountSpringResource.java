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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Spring managed JAX-RS resource for testing jersey-spring.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
@Path("/spring/account")
@Component
public class AccountSpringResource {

    @Inject
    @Named("AccountService-singleton")
    private AccountService accountServiceInject;

    @Autowired
    @Qualifier("AccountService-singleton")
    private AccountService accountServiceAutowired;

    @Inject
    @Named("AccountService-request-1")
    private AccountService accountServiceRequest1;

    @Autowired
    @Qualifier("AccountService-request-1")
    private AccountService accountServiceRequest2;

    @Autowired
    @Qualifier("AccountService-prototype-1")
    private AccountService accountServicePrototype1;

    @Autowired
    @Qualifier("AccountService-prototype-1")
    private AccountService accountServicePrototype2;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Inject
    private HK2ServiceSingleton hk2Singleton;

    @Inject
    private HK2ServiceRequestScoped hk2RequestScoped;

    @Inject
    private HK2ServicePerLookup hk2PerLookup;

    private String message = "n/a";

    // resource methods for testing resource class scope
    @GET
    @Path("message")
    public String getMessage() {
        return message;
    }

    @PUT
    @Path("message")
    @Consumes(MediaType.TEXT_PLAIN)
    public String setMessage(String message) {
        this.message = message;
        return message;
    }

    // JERSEY-2506 FIX VERIFICATION
    @GET
    @Path("server")
    public String verifyServletRequestInjection() {
        return "PASSED: " + httpServletRequest.getServerName();
    }

    @GET
    @Path("singleton/server")
    public String verifyServletRequestInjectionIntoSingleton() {
        return accountServiceInject.verifyServletRequestInjection();
    }

    @GET
    @Path("singleton/autowired/server")
    public String verifyServletRequestInjectionIntoAutowiredSingleton() {
        return accountServiceAutowired.verifyServletRequestInjection();
    }

    @GET
    @Path("request/server")
    public String verifyServletRequestInjectionIntoRequestScopedBean() {
        return accountServiceRequest1.verifyServletRequestInjection();
    }

    @GET
    @Path("prototype/server")
    public String verifyServletRequestInjectionIntoPrototypeScopedBean() {
        return accountServicePrototype1.verifyServletRequestInjection();
    }

    // resource methods for testing singleton scoped beans
    @GET
    @Path("singleton/inject/{accountId}")
    public BigDecimal getAccountBalanceSingletonInject(@PathParam("accountId") String accountId) {
        return accountServiceInject.getAccountBalance(accountId);
    }

    @GET
    @Path("singleton/autowired/{accountId}")
    public BigDecimal getAccountBalanceSingletonAutowired(@PathParam("accountId") String accountId) {
        return accountServiceAutowired.getAccountBalance(accountId);
    }

    @PUT
    @Path("singleton/{accountId}")
    @Consumes(MediaType.TEXT_PLAIN)
    public void setAccountBalanceSingleton(@PathParam("accountId") String accountId, String balance) {
        accountServiceInject.setAccountBalance(accountId, new BigDecimal(balance));
    }

    // resource methods for testing request scoped beans
    @PUT
    @Path("request/{accountId}")
    @Consumes(MediaType.TEXT_PLAIN)
    public BigDecimal setAccountBalanceRequest(@PathParam("accountId") String accountId, String balance) {
        accountServiceRequest1.setAccountBalance(accountId, new BigDecimal(balance));
        return accountServiceRequest2.getAccountBalance(accountId);
    }

    // resource methods for testing prototype scoped beans
    @PUT
    @Path("prototype/{accountId}")
    @Consumes(MediaType.TEXT_PLAIN)
    public BigDecimal setAccountBalancePrototype(@PathParam("accountId") String accountId, String balance) {
        accountServicePrototype1.setAccountBalance(accountId, new BigDecimal(balance));
        return accountServicePrototype2.getAccountBalance(accountId);
    }

}
