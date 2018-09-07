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
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.osgi.test.util.Helper;

import org.eclipse.persistence.jaxb.BeanValidationMode;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Michal Gajdos
 */
public class JsonMoxyTest extends AbstractJsonOsgiIntegrationTest {

    @Configuration
    public static Option[] configuration() {
        final List<Option> options = new ArrayList<>();

        options.addAll(Helper.getCommonOsgiOptions());
        options.addAll(Helper.expandedList(
                // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),

                bootDelegationPackage("javax.xml.bind"),
                bootDelegationPackage("javax.xml.bind.*"),
                // validation
                bootDelegationPackage("javax.xml.parsers"),
                bootDelegationPackage("javax.xml.parsers.*"),

                // moxy dependencies
                mavenBundle().groupId("org.glassfish.jersey.media").artifactId("jersey-media-moxy").versionAsInProject(),
                mavenBundle().groupId("org.glassfish.jersey.ext").artifactId("jersey-entity-filtering").versionAsInProject(),
                mavenBundle().groupId("org.eclipse.persistence").artifactId("org.eclipse.persistence.moxy").versionAsInProject(),
                mavenBundle().groupId("org.eclipse.persistence").artifactId("org.eclipse.persistence.core").versionAsInProject(),
                mavenBundle().groupId("org.eclipse.persistence").artifactId("org.eclipse.persistence.asm").versionAsInProject(),
                mavenBundle().groupId("org.glassfish").artifactId("javax.json").versionAsInProject(),

                // validation
                mavenBundle().groupId("org.hibernate.validator").artifactId("hibernate-validator").versionAsInProject(),
                mavenBundle().groupId("org.jboss.logging").artifactId("jboss-logging").versionAsInProject(),
                mavenBundle().groupId("com.fasterxml").artifactId("classmate").versionAsInProject(),
                mavenBundle().groupId("org.glassfish").artifactId("javax.el").versionAsInProject()
        ));

        return Helper.asArray(options);
    }

    @Override
    protected Feature getJsonProviderFeature() {
        // Turn off BV otherwise the test is not stable.
        return new Feature() {

            @Override
            public boolean configure(final FeatureContext context) {
                context.register(new MoxyJsonConfig()
                        .property(MarshallerProperties.BEAN_VALIDATION_MODE, BeanValidationMode.NONE)
                        .resolver());

                return true;
            }
        };
    }
}
