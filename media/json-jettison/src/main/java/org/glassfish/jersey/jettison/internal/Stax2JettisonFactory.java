/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jettison.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.message.internal.ReaderWriter;

import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamReader;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

/**
 * Factory for creating JSON-enabled StAX readers and writers.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
public class Stax2JettisonFactory {

    private Stax2JettisonFactory() {
    }

    public static XMLStreamWriter createWriter(final Writer writer,
                                               final JettisonConfig config) throws IOException {
        switch (config.getNotation()) {
            case BADGERFISH:
                return new BadgerFishXMLStreamWriter(writer);
            case MAPPED_JETTISON:
                Configuration jmConfig;
                if (null == config.getXml2JsonNs()) {
                    jmConfig = new Configuration();
                } else {
                    jmConfig = new Configuration(config.getXml2JsonNs());
                }

                final MappedXMLStreamWriter result = new MappedXMLStreamWriter(new MappedNamespaceConvention(jmConfig), writer);

                for (String array : config.getArrayElements()) {
                    result.serializeAsArray(array);
                }

                return result;
            default:
                return null;
        }
    }

    public static XMLStreamReader createReader(final Reader reader,
                                               final JettisonConfig config) throws XMLStreamException {
        Reader nonEmptyReader = ensureNonEmptyReader(reader);

        switch (config.getNotation()) {
            case MAPPED_JETTISON:
                try {
                    Configuration jmConfig;
                    if (null == config.getXml2JsonNs()) {
                        jmConfig = new Configuration();
                    } else {
                        jmConfig = new Configuration(config.getXml2JsonNs());
                    }
                    return new MappedXMLStreamReader(
                            new JSONObject(new JSONTokener(ReaderWriter.readFromAsString(nonEmptyReader))),
                            new MappedNamespaceConvention(jmConfig));
                } catch (Exception ex) {
                    throw new XMLStreamException(ex);
                }
            case BADGERFISH:
                try {
                    return new BadgerFishXMLStreamReader(
                            new JSONObject(new JSONTokener(ReaderWriter.readFromAsString(nonEmptyReader))));
                } catch (Exception ex) {
                    throw new XMLStreamException(ex);
                }
        }
        // This should not occur
        throw new IllegalArgumentException("Unknown JSON config");
    }

    private static Reader ensureNonEmptyReader(Reader reader) throws XMLStreamException {
        try {
            Reader mr = reader.markSupported() ? reader : new BufferedReader(reader);
            mr.mark(1);
            if (mr.read() == -1) {
                throw new XMLStreamException("JSON expression can not be empty!");
            }
            mr.reset();
            return mr;
        } catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
    }

}
