/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jaxb.internal;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.ws.rs.RuntimeType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Secure {@code DocumentBuilderFactory} and {@code XMLInputFactory} must not load external DTDs
 * or external parameter entities named by the request entity.
 */
public class XmlFactoryExternalDtdTest {

    private Path dtd;
    private CommonConfig config;

    @BeforeEach
    public void setUp() throws Exception {
        dtd = Files.createTempFile("jersey-xxe", ".dtd");
        Files.write(dtd, "<!ENTITY x 'x'>".getBytes(StandardCharsets.US_ASCII));
        config = new CommonConfig(RuntimeType.SERVER, ComponentBag.EXCLUDE_EMPTY);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(dtd);
    }

    private byte[] externalDtd() {
        return ("<!DOCTYPE r SYSTEM '" + dtd.toUri() + "'><r/>").getBytes(StandardCharsets.US_ASCII);
    }

    private byte[] externalParameterEntity() {
        return ("<!DOCTYPE r [<!ENTITY % pe SYSTEM '" + dtd.toUri() + "'> %pe;]><r/>").getBytes(StandardCharsets.US_ASCII);
    }

    @Test
    public void documentBuilderDoesNotLoadExternalDtd() {
        DocumentBuilderFactoryInjectionProvider provider = new DocumentBuilderFactoryInjectionProvider(null, config);
        assertThrows(SAXException.class,
                () -> provider.get().newDocumentBuilder().parse(new ByteArrayInputStream(externalDtd())));
        assertThrows(SAXException.class,
                () -> provider.get().newDocumentBuilder().parse(new ByteArrayInputStream(externalParameterEntity())));
    }

    @Test
    public void documentBuilderParsesPlainDocument() throws Exception {
        DocumentBuilderFactoryInjectionProvider provider = new DocumentBuilderFactoryInjectionProvider(null, config);
        byte[] doc = "<!DOCTYPE r [<!ENTITY i 'i'>]><r>text</r>".getBytes(StandardCharsets.US_ASCII);
        assertEquals("text", provider.get().newDocumentBuilder().parse(new ByteArrayInputStream(doc))
                .getDocumentElement().getTextContent());
    }

    @Test
    public void xmlInputFactoryDoesNotLoadExternalDtd() {
        XmlInputFactoryInjectionProvider provider = new XmlInputFactoryInjectionProvider(null, config);
        assertThrows(XMLStreamException.class, () -> {
            XMLStreamReader reader = provider.get().createXMLStreamReader(new ByteArrayInputStream(externalDtd()));
            while (reader.hasNext()) {
                reader.next();
            }
        });
    }
}
