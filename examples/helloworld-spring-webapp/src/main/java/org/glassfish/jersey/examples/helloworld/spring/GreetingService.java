/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.spring;

/**
 * Simple greeting service.
 *
 * @author Marko Asplund
 */
public interface GreetingService {

    /**
     * Workout a greeting.
     *
     * @param who to greet.
     * @return greeting.
     */
    String greet(String who);
}
