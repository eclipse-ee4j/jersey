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

/*
 * Portions contributed by Joseph Walton (Atlassian)
 */

package org.glassfish.jersey.jaxb.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.jersey.internal.inject.InjectionManager;

import org.junit.Before;
import org.junit.Test;

public class AbstractJaxbProviderTest {
    private InjectionManager injectionManager;

    @Before
    public void setUp() {
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager();
    }

    @Test
    public void abstractJaxbProviderDoesNotReadExternalDtds() throws Exception {
        SAXParserFactory spf = injectionManager.getInstance(SAXParserFactory.class);

        String url = "file:///no-such-file";
        String s = "<!DOCTYPE x SYSTEM '" + url + "'><x/>";
        SAXSource saxSource = AbstractJaxbProvider.getSAXSource(spf, new ByteArrayInputStream(s.getBytes("us-ascii")));

        TransformerFactory.newInstance().newTransformer().transform(saxSource, new StreamResult(new ByteArrayOutputStream()));
    }
}
