/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.extendedwadl;

import java.util.List;

import org.glassfish.jersey.server.wadl.config.WadlGeneratorConfig;
import org.glassfish.jersey.server.wadl.config.WadlGeneratorDescription;
import org.glassfish.jersey.server.wadl.internal.generators.WadlGeneratorApplicationDoc;
import org.glassfish.jersey.server.wadl.internal.generators.WadlGeneratorGrammarsSupport;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.WadlGeneratorResourceDocSupport;

/**
 * This subclass of {@link WadlGeneratorConfig} defines/configures {@link org.glassfish.jersey.server.wadl.WadlGenerator}s
 * to be used for generating WADL.
 *
 * @author Martin Grotzke (martin.grotzke@freiheit.com)
 */
public class SampleWadlGeneratorConfig extends WadlGeneratorConfig {

    @Override
    public List<WadlGeneratorDescription> configure() {
        return generator(WadlGeneratorApplicationDoc.class)
                .prop("applicationDocsStream", "application-doc.xml")
                .generator(WadlGeneratorGrammarsSupport.class)
                .prop("grammarsStream", "application-grammars.xml")
                .prop("overrideGrammars", true)
                .generator(WadlGeneratorResourceDocSupport.class)
                .prop("resourceDocStream", "resourcedoc.xml")
                .descriptions();
    }

}
