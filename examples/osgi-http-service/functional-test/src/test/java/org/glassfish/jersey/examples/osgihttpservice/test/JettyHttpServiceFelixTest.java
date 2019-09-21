/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.osgihttpservice.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@RunWith(PaxExam.class)
public class JettyHttpServiceFelixTest extends AbstractHttpServiceTest {

    @Override
    public List<Option> osgiRuntimeOptions() {
        return Arrays.asList(CoreOptions.options(
                mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.eventadmin").versionAsInProject()
        ));
    }

    @Override
    public List<Option> httpServiceProviderOptions() {
        return jettyOptions();
    }

    @Before
    public void before() {
        defaultMandatoryBeforeMethod();
    }

    @Test
    public void testHttpServiceMethod() throws Exception {
        defaultHttpServiceTestMethod();
    }
}

