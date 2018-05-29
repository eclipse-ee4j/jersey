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

package org.glassfish.jersey.tests.e2e.json;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.persistence.internal.helper.JavaVersion;
import org.glassfish.grizzly.utils.ArrayUtils;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.tests.e2e.json.entity.AnotherArrayTestBean;
import org.glassfish.jersey.tests.e2e.json.entity.AttrAndCharDataBean;
import org.glassfish.jersey.tests.e2e.json.entity.ComplexBeanWithAttributes;
import org.glassfish.jersey.tests.e2e.json.entity.ComplexBeanWithAttributes2;
import org.glassfish.jersey.tests.e2e.json.entity.ComplexBeanWithAttributes3;
import org.glassfish.jersey.tests.e2e.json.entity.ComplexBeanWithAttributes4;
import org.glassfish.jersey.tests.e2e.json.entity.EmptyElementBean;
import org.glassfish.jersey.tests.e2e.json.entity.EmptyElementContainingBean;
import org.glassfish.jersey.tests.e2e.json.entity.EncodedContentBean;
import org.glassfish.jersey.tests.e2e.json.entity.FakeArrayBean;
import org.glassfish.jersey.tests.e2e.json.entity.IntArray;
import org.glassfish.jersey.tests.e2e.json.entity.ListAndNonListBean;
import org.glassfish.jersey.tests.e2e.json.entity.ListEmptyBean;
import org.glassfish.jersey.tests.e2e.json.entity.ListWrapperBean;
import org.glassfish.jersey.tests.e2e.json.entity.MyResponse;
import org.glassfish.jersey.tests.e2e.json.entity.NamespaceBean;
import org.glassfish.jersey.tests.e2e.json.entity.NamespaceBeanWithAttribute;
import org.glassfish.jersey.tests.e2e.json.entity.NullStringBean;
import org.glassfish.jersey.tests.e2e.json.entity.Person;
import org.glassfish.jersey.tests.e2e.json.entity.PureCharDataBean;
import org.glassfish.jersey.tests.e2e.json.entity.RegisterMessage;
import org.glassfish.jersey.tests.e2e.json.entity.SimpleBean;
import org.glassfish.jersey.tests.e2e.json.entity.SimpleBeanWithAttributes;
import org.glassfish.jersey.tests.e2e.json.entity.SimpleBeanWithJustOneAttribute;
import org.glassfish.jersey.tests.e2e.json.entity.SimpleBeanWithJustOneAttributeAndValue;
import org.glassfish.jersey.tests.e2e.json.entity.TreeModel;
import org.glassfish.jersey.tests.e2e.json.entity.TwoListsWrapperBean;
import org.glassfish.jersey.tests.e2e.json.entity.User;
import org.glassfish.jersey.tests.e2e.json.entity.UserTable;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class JaxbTest extends AbstractJsonTest {

    private static final Class<?>[] CLASSES = {
            AnotherArrayTestBean.class,
            AttrAndCharDataBean.class,
            ComplexBeanWithAttributes.class,
            ComplexBeanWithAttributes2.class,
            ComplexBeanWithAttributes3.class,
            ComplexBeanWithAttributes4.class,
            EmptyElementBean.class,
            EmptyElementContainingBean.class,
            EncodedContentBean.class,
            FakeArrayBean.class,
            IntArray.class,
            ListAndNonListBean.class,
            ListEmptyBean.class,
            ListWrapperBean.class,
            MyResponse.class,
            NamespaceBean.class,
            NamespaceBeanWithAttribute.class,
            NullStringBean.class,
            Person.class,
            PureCharDataBean.class,
            RegisterMessage.class,
            SimpleBean.class,
            SimpleBeanWithAttributes.class,
            SimpleBeanWithJustOneAttribute.class,
            SimpleBeanWithJustOneAttributeAndValue.class,
            TreeModel.class,
            TwoListsWrapperBean.class,
            User.class,
            UserTable.class
    };

    public JaxbTest(final JsonTestSetup jsonTestSetup) throws Exception {
        super(jsonTestSetup);
    }

    /**
     * check if the current JVM is supported by this test.
     *
     * @return true if all tests can be run, false if some tests shall be excluded due to JRE bug
     */
    private static boolean isJavaVersionSupported() {
        final String javaVersion = PropertiesHelper.getSystemProperty("java.version").run();
        if (javaVersion != null) {
            int pos =  javaVersion.lastIndexOf("_");
            if (pos > -1) {
                final Integer minorVersion = Integer.valueOf(javaVersion.substring(pos+1));
                return minorVersion < 160 || minorVersion > 172; //only those between 161 and 172 minor
                                                                 // releases are not supported
            }
        }
        return  true;
    }

    @Parameterized.Parameters()
    public static Collection<JsonTestSetup[]> getJsonProviders() throws Exception {
        final Class<?>[]
                filteredClasses = (isJavaVersionSupported()) ? CLASSES : ArrayUtils.remove(CLASSES, EncodedContentBean.class);
        final List<JsonTestSetup[]> jsonTestSetups = new LinkedList<>();

        for (final JsonTestProvider jsonProvider : JsonTestProvider.JAXB_PROVIDERS) {
            for (final Class<?> entityClass : filteredClasses) {
                // TODO - remove the condition after jsonb polymorphic adapter is implemented
                if (!(jsonProvider instanceof JsonTestProvider.JsonbTestProvider)) {
                    jsonTestSetups.add(new JsonTestSetup[]{new JsonTestSetup(entityClass, jsonProvider)});
                }
            }
        }

        return jsonTestSetups;
    }
}
