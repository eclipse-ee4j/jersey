/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.cdi2se;

import javax.enterprise.context.ApplicationScoped;

/**
 * Application-scoped service returning "hello" sentence.
 *
 * @author Petr Bouda
 */
@ApplicationScoped
public class HelloBean {

    public String hello(String name) {
        return "Hello " + name;
    }
}
