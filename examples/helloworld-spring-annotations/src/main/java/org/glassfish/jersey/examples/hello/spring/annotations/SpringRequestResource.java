/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.hello.spring.annotations;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Integration of jersey and spring.
 * This rest controller is a singleton spring bean with autowired dependencies
 * from spring
 *
 * @author Geoffroy Warin (http://geowarin.github.io)
 */
@Singleton
@Path("spring-resource")
@Service
public class SpringRequestResource {

    AtomicInteger counter = new AtomicInteger();

    @Autowired
    private GreetingService greetingService;

    @Autowired
    private List<GoodbyeService> goodbyeServicesList;
    @Autowired
    private Set<GoodbyeService> goodbyeServicesSet;

    @Autowired
    private List<GoodbyeService> goodbyeServicesIterable;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getHello() {
        return greetingService.greet("world " + counter.incrementAndGet());
    }

    private void checkIntegrity() {
        final Iterator<GoodbyeService> it = goodbyeServicesIterable.iterator();
        int i = 0;
        while (it.hasNext()) {

            final GoodbyeService s1 = it.next();
            final GoodbyeService s2 = goodbyeServicesList.get(i);
            if (s1 != s2) {
                throw new ProcessingException("Instance of service s1 (" + s1.getClass()
                        + ") is not equal to service s2(" + s2.getClass() + ")");
            }
            i++;
        }

        if (goodbyeServicesList.size() != goodbyeServicesSet.size()) {
            throw new ProcessingException("Size of set and size of the list differs. list=" + goodbyeServicesList.size()
                    + "; set=" + goodbyeServicesSet.size());
        }
    }

    private GoodbyeService getService(Class<?> serviceClass) {
        for (GoodbyeService service : goodbyeServicesList) {
            if (serviceClass.isAssignableFrom(service.getClass())) {
                return service;
            }
        }
        return null;
    }

    @Path("goodbye")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getGoodbye() {
        checkIntegrity();

        final GoodbyeService goodbyeService = getService(EnglishGoodbyeService.class);
        return goodbyeService.goodbye("cruel world");
    }

    @Path("norwegian-goodbye")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getNorwegianGoodbye() {
        checkIntegrity();
        return getService(NorwegianGoodbyeService.class).goodbye("på badet");
    }
}
