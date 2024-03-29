/*
 * Copyright (c) 2019, 2022 Oracle and/or its affiliates. All rights reserved.
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

import jdk.javadoc.doclet.DocletEnvironment;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ClassDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.MethodDocType;
import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ParamDocType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

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
     * the given {@link TypeElement}.
     *
     * @param classDoc     the class javadoc
     * @param classDocType the {@link ClassDocType} to extend. This will later be processed by the
     *                     {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     */
    @Deprecated
    void processClassDoc(TypeElement classDoc, ClassDocType classDocType);

    /**
     * Process the provided methodDoc and add your custom information to the methodDocType.<br>
     *
     * @param methodDoc     the {@link ExecutableElement} representing the docs of your method.
     * @param methodDocType the related {@link MethodDocType} that will later be processed by the
     *                      {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     */
    @Deprecated
    void processMethodDoc(ExecutableElement methodDoc, MethodDocType methodDocType);

    /**
     * Use this method to extend the provided {@link ParamDocType} with the information from the
     * given {@link ParamTag} and {@link Parameter}.
     *
     * @param parameter    the parameter (that is documented or not)
     * @param paramDocType the {@link ParamDocType} to extend. This will later be processed by the
     *                     {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     */
    @Deprecated
    void processParamTag(VariableElement parameter, ParamDocType paramDocType);

    /**
     * Use this method to extend the provided {@link ClassDocType} with the information from
     * the given {@link TypeElement}.
     *
     * @param classDoc     the class javadoc
     * @param classDocType the {@link ClassDocType} to extend. This will later be processed by the
     *                     {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     * @param docEnv       the doclet environment used to extract info from classDoc
     */
    default void processClassDocWithDocEnv(TypeElement classDoc, ClassDocType classDocType, DocletEnvironment docEnv) {
        processClassDoc(classDoc, classDocType);
    }

    /**
     * Process the provided methodDoc and add your custom information to the methodDocType.<br>
     *
     * @param methodDoc     the {@link ExecutableElement} representing the docs of your method.
     * @param methodDocType the related {@link MethodDocType} that will later be processed by the
     *                      {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     * @param docEnv        the doclet environment used to extract info from methodDoc
     */
    default void processMethodDocWithDocEnv(ExecutableElement methodDoc, MethodDocType methodDocType, DocletEnvironment docEnv) {
        processMethodDoc(methodDoc, methodDocType);
    }

    /**
     * Use this method to extend the provided {@link ParamDocType} with the information from the
     * given {@link VariableElement}.
     *
     * @param parameter    the parameter (that is documented or not)
     * @param paramDocType the {@link ParamDocType} to extend. This will later be processed by the
     *                     {@link org.glassfish.jersey.server.wadl.WadlGenerator}s.
     * @param docEnv       the Doclet Environment used to extract info from parameter
     */
    default void processParamTagWithDocEnv(VariableElement parameter, ParamDocType paramDocType, DocletEnvironment docEnv) {
        processParamTag(parameter, paramDocType);
    }

}
