/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.glassfish.jersey.internal.util.SaxHelper;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Secure SAX parser factory wrapper.
 *
 * @author Martin Matula
 * @author Michal Gajdos
 */
public class SecureSaxParserFactory extends SAXParserFactory {

    private static final Logger LOGGER = Logger.getLogger(SecureSaxParserFactory.class.getName());
    private static final EntityResolver EMPTY_ENTITY_RESOLVER = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
    };

    private static final String EXTERNAL_GENERAL_ENTITIES_FEATURE = "http://xml.org/sax/features/external-general-entities";
    private static final String EXTERNAL_PARAMETER_ENTITIES_FEATURE = "http://xml.org/sax/features/external-parameter-entities";
    private static final String DISALLOW_DOCTYPE_DECLARATION_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    private final SAXParserFactory spf;
    private Map<String, Object> saxParserProperties;
    private final boolean isSecure;

    /**
     * Create new secure SAX parser factory wrapper.
     *
     * @param spf SAX parser factory.
     */
    public SecureSaxParserFactory(SAXParserFactory spf) {
        this(spf, true);
    }

    /**
     * Create new SAX parser factory wrapper. Can be secure.
     * @param spf SAX parser factory.
     * @param isSecure defined whether the parser is secure.
     */
    SecureSaxParserFactory(SAXParserFactory spf, boolean isSecure) {
        this.spf = spf;
        this.isSecure = isSecure;

        if (SaxHelper.isXdkParserFactory(spf)) {
            LOGGER.log(Level.WARNING, LocalizationMessages.SAX_XDK_NO_SECURITY_FEATURES());
        } else {
            try {
                if (isSecure) {
                    spf.setFeature(EXTERNAL_GENERAL_ENTITIES_FEATURE, Boolean.FALSE);
                }
            } catch (Exception ex) {
                LOGGER.log(
                        Level.CONFIG,
                        LocalizationMessages.SAX_CANNOT_DISABLE_GENERAL_ENTITY_PROCESSING_FEATURE(spf.getClass()),
                        ex);
            }

            try {
                if (isSecure) {
                    spf.setFeature(EXTERNAL_PARAMETER_ENTITIES_FEATURE, Boolean.FALSE);
                }
            } catch (Exception ex) {
                LOGGER.log(
                        Level.CONFIG,
                        LocalizationMessages.SAX_CANNOT_DISABLE_PARAMETER_ENTITY_PROCESSING_FEATURE(spf.getClass()),
                        ex);
            }

            try {
                if (isSecure) {
                    spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
                }
            } catch (Exception ex) {
                LOGGER.log(
                        Level.CONFIG,
                        LocalizationMessages.SAX_CANNOT_ENABLE_SECURE_PROCESSING_FEATURE(spf.getClass()),
                        ex);
            }

            try {
                if (isSecure) {
                    spf.setFeature(DISALLOW_DOCTYPE_DECLARATION_FEATURE, Boolean.TRUE);
                }
            } catch (Exception ex) {
                LOGGER.log(
                        Level.CONFIG,
                        LocalizationMessages.SAX_CANNOT_ENABLE_DISALLOW_DOCTYPE_DECLARATION_FEATURE(spf.getClass()),
                        ex);
            }
        }
    }

    @Override
    public void setNamespaceAware(boolean b) {
        spf.setNamespaceAware(b);
    }

    @Override
    public void setValidating(boolean b) {
        spf.setValidating(b);
    }

    @Override
    public boolean isNamespaceAware() {
        return spf.isNamespaceAware();
    }

    @Override
    public boolean isValidating() {
        return spf.isValidating();
    }

    @Override
    public Schema getSchema() {
        return spf.getSchema();
    }

    @Override
    public void setSchema(Schema schema) {
        spf.setSchema(schema);
    }

    @Override
    public void setXIncludeAware(boolean b) {
        spf.setXIncludeAware(b);
    }

    @Override
    public boolean isXIncludeAware() {
        return spf.isXIncludeAware();
    }

    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        final SAXParser wrappingParser = new WrappingSAXParser(spf.newSAXParser(), isSecure);
        if (saxParserProperties != null) {
            for (Map.Entry<String, Object> entry : saxParserProperties.entrySet()) {
                JaxbFeatureUtil.setProperty(SAXParser.class, entry, wrappingParser::setProperty);
            }
        }
        return wrappingParser;
    }

    @Override
    public void setFeature(String s, boolean b)
            throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        spf.setFeature(s, b);
    }

    @Override
    public boolean getFeature(String s) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        return spf.getFeature(s);
    }

    void setSaxParserProperties(Map<String, Object> saxParserProperties) {
        this.saxParserProperties = saxParserProperties;
    }

    @SuppressWarnings("deprecation")
    private static final class WrappingSAXParser extends SAXParser {

        private final SAXParser sp;
        private final boolean isSecure;

        private WrappingSAXParser(SAXParser sp, boolean isSecure) {
            this.sp = sp;
            this.isSecure = isSecure;
        }

        @Override
        public void reset() {
            sp.reset();
        }

        @Override
        @SuppressWarnings("deprecation")
        public void parse(InputStream inputStream, org.xml.sax.HandlerBase handlerBase) throws SAXException, IOException {
            sp.parse(inputStream, handlerBase);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void parse(InputStream inputStream, org.xml.sax.HandlerBase handlerBase, String s)
                throws SAXException, IOException {
            sp.parse(inputStream, handlerBase, s);
        }

        @Override
        public void parse(InputStream inputStream, DefaultHandler defaultHandler) throws SAXException, IOException {
            sp.parse(inputStream, defaultHandler);
        }

        @Override
        public void parse(InputStream inputStream, DefaultHandler defaultHandler, String s) throws SAXException, IOException {
            sp.parse(inputStream, defaultHandler, s);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void parse(String s, org.xml.sax.HandlerBase handlerBase) throws SAXException, IOException {
            sp.parse(s, handlerBase);
        }

        @Override
        public void parse(String s, DefaultHandler defaultHandler) throws SAXException, IOException {
            sp.parse(s, defaultHandler);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void parse(File file, org.xml.sax.HandlerBase handlerBase) throws SAXException, IOException {
            sp.parse(file, handlerBase);
        }

        @Override
        public void parse(File file, DefaultHandler defaultHandler) throws SAXException, IOException {
            sp.parse(file, defaultHandler);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void parse(InputSource inputSource, org.xml.sax.HandlerBase handlerBase) throws SAXException, IOException {
            sp.parse(inputSource, handlerBase);
        }

        @Override
        public void parse(InputSource inputSource, DefaultHandler defaultHandler) throws SAXException, IOException {
            sp.parse(inputSource, defaultHandler);
        }

        @Override
        @SuppressWarnings("deprecation")
        public org.xml.sax.Parser getParser() throws SAXException {
            return sp.getParser();
        }

        @Override
        public XMLReader getXMLReader() throws SAXException {
            XMLReader r = sp.getXMLReader();
            r.setEntityResolver(EMPTY_ENTITY_RESOLVER);

            try {
                if (isSecure) {
                    r.setFeature(EXTERNAL_GENERAL_ENTITIES_FEATURE, Boolean.FALSE);
                }
            } catch (Exception ex) {
                LOGGER.log(
                        Level.CONFIG,
                        LocalizationMessages.SAX_CANNOT_DISABLE_GENERAL_ENTITY_PROCESSING_FEATURE_ON_READER(r.getClass()),
                        ex);
            }

            return r;
        }

        @Override
        public boolean isNamespaceAware() {
            return sp.isNamespaceAware();
        }

        @Override
        public boolean isValidating() {
            return sp.isValidating();
        }

        @Override
        public void setProperty(String s, Object o) throws SAXNotRecognizedException, SAXNotSupportedException {
            sp.setProperty(s, o);
        }

        @Override
        public Object getProperty(String s) throws SAXNotRecognizedException, SAXNotSupportedException {
            return sp.getProperty(s);
        }

        @Override
        public Schema getSchema() {
            return sp.getSchema();
        }

        @Override
        public boolean isXIncludeAware() {
            return sp.isXIncludeAware();
        }
    }
}
