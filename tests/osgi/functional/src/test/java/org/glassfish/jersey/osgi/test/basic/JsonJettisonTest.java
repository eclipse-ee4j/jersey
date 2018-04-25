/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.osgi.test.basic;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Feature;

import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.osgi.test.util.Helper;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Michal Gajdos
 */
public class JsonJettisonTest extends AbstractJsonOsgiIntegrationTest {

    @Configuration
    public static Option[] configuration() {
        List<Option> options = new ArrayList<Option>();

        options.addAll(Helper.getCommonOsgiOptions());
        options.addAll(Helper.expandedList(
                // jersey-json dependencies
                mavenBundle().groupId("org.glassfish.jersey.media").artifactId("jersey-media-json-jettison").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jettison").artifactId("jettison").versionAsInProject()
        ));

        return Helper.asArray(options);
    }

    @Override
    protected Feature getJsonProviderFeature() {
        return new JettisonFeature();
    }
}
