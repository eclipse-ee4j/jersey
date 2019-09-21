/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.extendedwadl.resources;

import org.glassfish.jersey.examples.extendedwadl.SampleWadlGeneratorConfig;
import org.glassfish.jersey.examples.extendedwadl.util.Examples;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Jonathan Benoit
 */
public class MyApplication extends ResourceConfig {
    public MyApplication() {
        super(ItemResource.class, ItemsResource.class, Examples.class, SampleWadlGeneratorConfig.class);
    }
}
