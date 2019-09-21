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

import org.springframework.stereotype.Component;

@Component
public class EnglishGoodbyeService implements GoodbyeService {

    @Override
    public String goodbye(final String who) {
        return String.format("goodbye, %s!", who);
    }
}
