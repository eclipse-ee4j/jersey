/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.entity.filtering.json;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michal Gajdos
 */
public class MoxyEntityFilteringTest extends JerseyTest {

    @Path("/")
    @Produces("application/json")
    public static class Resource {

        @GET
        public XmlElementEntity getXmlAttributeEntity() {
            return new XmlElementEntity(new XmlAttributeEntity("foo"));
        }
    }

    @XmlRootElement
    public static class XmlElementEntity {

        @XmlElement
        private XmlAttributeEntity value;

        public XmlElementEntity() {
        }

        private XmlElementEntity(final XmlAttributeEntity value) {
            this.value = value;
        }

        public XmlAttributeEntity getValue() {
            return value;
        }

        public void setValue(final XmlAttributeEntity value) {
            this.value = value;
        }
    }

    @XmlRootElement
    public static class XmlAttributeEntity {

        @XmlAttribute
        private String attribute;

        public XmlAttributeEntity() {
        }

        public XmlAttributeEntity(final String attribute) {
            this.attribute = attribute;
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(final String attribute) {
            this.attribute = attribute;
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(Resource.class)
                // Features.
                .register(EntityFilteringFeature.class);
    }

    @Test
    public void testXmlAttributeEntity() throws Exception {
        final XmlElementEntity entity = target().request().get(XmlElementEntity.class);

        assertThat(entity, notNullValue());
        assertThat(entity.getValue(), notNullValue());
        assertThat(entity.getValue().getAttribute(), equalTo("foo"));
    }
}
