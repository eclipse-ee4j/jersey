/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.test.artifacts;

import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ArchetypesTest {
    public static final String[] archetypePoms = {
            "../../archetypes/jersey-example-java8-webapp/src/main/resources/archetype-resources/pom.xml",
            "../../archetypes/jersey-heroku-webapp/src/main/resources/archetype-resources/pom.xml",
            "../../archetypes/jersey-quickstart-grizzly2/src/main/resources/archetype-resources/pom.xml",
            "../../archetypes/jersey-quickstart-webapp/src/main/resources/archetype-resources/pom.xml",
    };

    @Test
    public void testPropertiesVersion() throws XmlPullParserException, IOException {
        Properties properties = MavenUtil.getModelFromFile("../../pom.xml").getProperties();
//        System.out.println(properties);
        TestResult testResult = new TestResult();
        for (String pom : archetypePoms) {
            File pomFile = new File(pom);
            Assert.assertTrue("The pom file " + pom + " does not exist", pomFile.exists());
            Assert.assertTrue("The pom file " + pom + " cannot be read", pomFile.canRead());

            boolean failed = false;
            Model pomModel = MavenUtil.getModelFromFile(pom);
            Properties pomProperties = pomModel.getProperties();
            for (Map.Entry<Object, Object> pomEntry : pomProperties.entrySet()) {
                if (pomEntry.getKey().equals("jersey.config.test.container.port")) {
                    // Skip the following
                    continue;
                }
                // Update the names with the ones in Jersey
                // Check the properties are there
                final String key = pomEntry.getKey().toString();

                if (properties.getProperty(key) == null) {
                    testResult.ok().append("Property ")
                            .append(pomEntry.getKey().toString())
                            .append(" from ").append(pom).println(" not in Jersey");
                    failed = true;
                }
                // check the values
                else if (
                        //archetype property value can be a variable from the main pom.xml - check and exclude if so
                        !(properties.containsKey(key) && pomEntry.getValue().toString().contains(key))
                        && !properties.getProperty(key).equals(pomEntry.getValue())
                ) {
                    testResult.exception().append("The property ")
                            .append(pomEntry.getKey().toString())
                            .append(" in archetype pom ")
                            .append(pom)
                            .append(" not equals Jersey ")
                            .println(properties.getProperty(pomEntry.getKey().toString()));
                    failed = true;
                }
            }
            if (!failed) {
                testResult.ok().append("The properties in archetype pom ").append(pom).println(" equals Jersey");
            }
        }

        if (!testResult.result()) {
            Assert.fail();
        }
    }

}
