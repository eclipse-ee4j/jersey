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
import java.io.InputStream;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.message.internal.SourceProvider;

import org.junit.Before;
import org.junit.Test;

public class SourceProviderTest {

    private InjectionManager injectionManager;

    @Before
    public void setUp() {
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager(new AbstractBinder() {
            @Override
            protected void configure() {
                bindAsContract(SourceProvider.SaxSourceReader.class);
            }
        });
    }

    @Test
    public void saxSourceReaderDoesNotReadExternalDtds() throws Exception {
        SourceProvider.SaxSourceReader reader = injectionManager.getInstance(SourceProvider.SaxSourceReader.class);
        InputStream entityStream = new ByteArrayInputStream(
                "<!DOCTYPE x SYSTEM 'file:///no-such-file'> <rootObject/>".getBytes("us-ascii"));
        SAXSource ss = reader.readFrom(null, null, null, null, null, entityStream);

        TransformerFactory.newInstance().newTransformer().transform(ss, new StreamResult(new ByteArrayOutputStream()));
    }
}
