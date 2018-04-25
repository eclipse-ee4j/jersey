/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.spring;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Simple {@link GreetingService} implementation to just say hello.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class GreetingServiceImpl implements GreetingService {

    @Autowired
    private HttpServletRequest servletRequest;

    @Override
    public String greet(String who) {
        final String serverName = servletRequest.getServerName();
        return String.format("hello, %s! Greetings from server %s!", who, serverName);
    }

}
