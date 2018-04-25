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

package org.glassfish.jersey.server.wadl.generators.resourcedoc;

import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;

import javax.ws.rs.POST;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.wadl.WadlGenerator;
import org.glassfish.jersey.server.wadl.internal.ApplicationDescription;
import org.glassfish.jersey.server.wadl.internal.WadlBuilder;
import org.glassfish.jersey.server.wadl.internal.WadlGeneratorImpl;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.WadlGeneratorResourceDocSupport;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.AnnotationDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ClassDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.MethodDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.NamedValueType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ParamDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ResourceDocType;

import org.junit.Test;

import com.sun.research.ws.wadl.Application;

public class WadlGeneratorResourceDocSupportTest {
    @Test
    public void wadlIsGeneratedWithUnknownCustomParameterAnnotation() throws JAXBException {
        /* Set up a ClassDocType that has something for a custom-annotated parameter */
        ClassDocType cdt = new ClassDocType();
        cdt.setClassName(TestResource.class.getName());

        MethodDocType mdt = new MethodDocType();
        mdt.setMethodName("method");
        cdt.getMethodDocs().add(mdt);

        ParamDocType pdt = new ParamDocType("x", "comment about x");
        mdt.getParamDocs().add(pdt);

        AnnotationDocType adt = new AnnotationDocType();
        adt.setAnnotationTypeName(CustomParam.class.getName());
        adt.getAttributeDocs().add(new NamedValueType("value", "x"));

        pdt.getAnnotationDocs().add(adt);

        ResourceDocType rdt = new ResourceDocType();
        rdt.getDocs().add(cdt);


        /* Generate WADL for that class */
        WadlGenerator wg = new WadlGeneratorResourceDocSupport(new WadlGeneratorImpl(), rdt);

        WadlBuilder wb = new WadlBuilder(wg, false, null);
        Resource resource = Resource.from(TestResource.class);
        ApplicationDescription app = wb.generate(Collections.singletonList(resource));


        /* Confirm that it can be marshalled without error */
        StringWriter sw = new StringWriter();

        JAXBContext context = JAXBContext.newInstance(Application.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(app.getApplication(), sw);
    }

    public static class TestResource {
        @POST
        public String method(@CustomParam("x") Object param) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * An annotation IntrospectionModeller doesn't know about.
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomParam {
        String value();
    }
}
