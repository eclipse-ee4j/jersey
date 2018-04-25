/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.jackson1.Jackson1Feature;
import org.glassfish.jersey.osgi.test.util.Helper;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Michal Gajdos
 */
public class JsonJackson1Test extends AbstractJsonOsgiIntegrationTest {

    @Configuration
    public static Option[] configuration() {
        final List<Option> options = new ArrayList<>();

        options.addAll(Helper.getCommonOsgiOptions());
        options.addAll(Helper.expandedList(
                // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),

                mavenBundle().groupId("org.glassfish.jersey.media").artifactId("jersey-media-json-jackson1").versionAsInProject(),

                // jersey-json dependencies
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-core-asl").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-mapper-asl").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-jaxrs").versionAsInProject(),
                mavenBundle().groupId("org.codehaus.jackson").artifactId("jackson-xc").versionAsInProject()
        ));

        return Helper.asArray(options);
    }

    @Override
    protected Feature getJsonProviderFeature() {
        return new Jackson1Feature();
    }
}
