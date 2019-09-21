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

import org.springframework.stereotype.Service;

/**
 * Simple greeting service
 *
 * @author Geoffroy Warin (http://geowarin.github.io)
 */
@Service
public class GreetingService {
    public String greet(String who) {
        return String.format("hello, %s!", who);
    }
}
