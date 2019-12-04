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

package org.glassfish.jersey.wadl.doclet;

import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ClassDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.MethodDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ParamDocType;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;

/**
 * A doc processor is handed over javadoc elements so that it can turn this into
 * resource doc elements, even self defined.
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public interface DocProcessor {

    /**
     * Specify jaxb classes of instances that you add to the {@code resourcedoc} model.
     * These classes are added to the list of classes when creating the jaxb context
     * with {@code JAXBContext.newInstance( clazzes );}.
     *
     * @return a list of classes or {@code null}
     */
    Class<?>[] getRequiredJaxbContextClasses();

    /**
     * specify which of your elements you want to be handled as CDATA.
     * The use of the '^' between the {@code namespaceURI} and the {@code localname}
     * seems to be an implementation detail of the xerces code.
     * When processing xml that doesn't use namespaces, simply omit the
     * namespace prefix as shown in the third CDataElement below.
     *
     * @return an Array of element descriptors or {@code null}
     */
    String[] getCDataElements();

    /**
     * Use this method to extend the provided {@link ClassDocType} with the information from
     * the given {@link ClassDoc}.
     *
     * @param classDoc     the class javadoc
     * @param classDocType the {@link ClassDocType} to extend. This will later be processed by the
     *                     {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     */
    void processClassDoc(ClassDoc classDoc, ClassDocType classDocType);

    /**
     * Process the provided methodDoc and add your custom information to the methodDocType.<br>
     * Use e.g. {@link MethodDocType#getAny()} to store custom elements.
     *
     * @param methodDoc     the {@link MethodDoc} representing the docs of your method.
     * @param methodDocType the related {@link MethodDocType} that will later be processed by the
     *                      {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     */
    void processMethodDoc(MethodDoc methodDoc, MethodDocType methodDocType);

    /**
     * Use this method to extend the provided {@link ParamDocType} with the information from the
     * given {@link ParamTag} and {@link Parameter}.
     *
     * @param paramTag     the parameter javadoc
     * @param parameter    the parameter (that is documented or not)
     * @param paramDocType the {@link ParamDocType} to extend. This will later be processed by the
     *                     {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     */
    void processParamTag(ParamTag paramTag, Parameter parameter, ParamDocType paramDocType);

}
