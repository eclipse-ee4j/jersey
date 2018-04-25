/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerFactory;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.PerThread;

/**
 * Binder for JAX-B message body workers.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JaxbMessagingBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bindSingletonWorker(DocumentProvider.class);
        bindSingletonWorker(XmlJaxbElementProvider.App.class);
        bindSingletonWorker(XmlJaxbElementProvider.Text.class);
        bindSingletonWorker(XmlJaxbElementProvider.General.class);

        bindSingletonWorker(XmlCollectionJaxbProvider.App.class);
        bindSingletonWorker(XmlCollectionJaxbProvider.Text.class);
        bindSingletonWorker(XmlCollectionJaxbProvider.General.class);

        bindSingletonWorker(XmlRootElementJaxbProvider.App.class);
        bindSingletonWorker(XmlRootElementJaxbProvider.Text.class);
        bindSingletonWorker(XmlRootElementJaxbProvider.General.class);

        bind(XmlRootObjectJaxbProvider.App.class).to(MessageBodyReader.class).in(Singleton.class);
        bind(XmlRootObjectJaxbProvider.Text.class).to(MessageBodyReader.class).in(Singleton.class);
        bind(XmlRootObjectJaxbProvider.General.class).to(MessageBodyReader.class).in(Singleton.class);

        // XML factory injection points:
        bindFactory(DocumentBuilderFactoryInjectionProvider.class).to(DocumentBuilderFactory.class).in(PerThread.class);
        bindFactory(SaxParserFactoryInjectionProvider.class).to(SAXParserFactory.class).in(PerThread.class);
        bindFactory(XmlInputFactoryInjectionProvider.class).to(XMLInputFactory.class).in(PerThread.class);
        bindFactory(TransformerFactoryInjectionProvider.class).to(TransformerFactory.class).in(PerThread.class);
    }

    private <T extends MessageBodyReader & MessageBodyWriter> void bindSingletonWorker(Class<T> worker) {
        bind(worker).to(MessageBodyReader.class).to(MessageBodyWriter.class).in(Singleton.class);
    }
}
