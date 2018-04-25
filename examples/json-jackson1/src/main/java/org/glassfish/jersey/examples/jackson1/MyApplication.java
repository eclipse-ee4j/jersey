/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson1;

import org.glassfish.jersey.jackson1.Jackson1Feature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * {@link javax.ws.rs.core.Application} descendant.
 *
 * Used to set resource and providers classes.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class MyApplication extends ResourceConfig {

    public MyApplication() {
        super(
                EmptyArrayResource.class,
                NonJaxbBeanResource.class,
                CombinedAnnotationResource.class,
                // register Jackson ObjectMapper resolver
                MyObjectMapperProvider.class,
                ExceptionMappingTestResource.class,
                Jackson1Feature.class
        );
    }
}
