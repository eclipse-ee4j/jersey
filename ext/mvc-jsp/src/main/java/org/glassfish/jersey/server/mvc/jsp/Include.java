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

package org.glassfish.jersey.server.mvc.jsp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.glassfish.jersey.server.mvc.internal.TemplateHelper;
import org.glassfish.jersey.server.mvc.jsp.internal.LocalizationMessages;

/**
 * Includes a side JSP file for the {@code resolvingClass} class.
 * <p/>
 * This tag looks for a side JSP file of the given name
 * from the inheritance hierarchy of the "resolvingClass" class,
 * and includes the contents of it, just like &lt;jsp:include>.
 * <p/>
 * For example, if the {@code resolvingClass} class is the {@code Foo} class,
 * which looks like the following:
 * <pre>
 * class Foo extends Bar { ... }
 * class Bar extends Zot { ... }
 * </pre>
 * <p/>
 * And if you write:
 * <pre>
 * &lt;st:include page="abc.jsp"/&gt;
 * </pre>
 * then, it looks for the following files in this order,
 * and includes the first one found.
 * <ol>
 * <li>a side-file of the {@code Foo} class named {@code abc.jsp} ({@code /WEB-INF/Foo/abc.jsp})
 * <li>a side-file of the {@code Bar} class named {@code abc.jsp} ({@code /WEB-INF/Bar/abc.jsp})
 * <li>a side-file of the {@code Zot} class named {@code abc.jsp} ({@code /WEB-INF/Zot/abc.jsp})
 * </ol>
 *
 * @author Kohsuke Kawaguchi
 * @author Paul Sandoz
 */
public class Include extends SimpleTagSupport {

    private String page;

    /**
     * Specifies the name of the JSP to be included.
     *
     * @param page page to be included.
     */
    public void setPage(String page) {
        this.page = page;
    }

    private Object getPageObject(String name) {
        return getJspContext().getAttribute(name, PageContext.PAGE_SCOPE);
    }

    public void doTag() throws JspException, IOException {
        final JspContext jspContext = getJspContext();
        final Class<?> resolvingClass = (Class<?>) jspContext
                .getAttribute(RequestDispatcherWrapper.RESOLVING_CLASS_ATTRIBUTE_NAME, PageContext.REQUEST_SCOPE);
        final String basePath = (String) jspContext.getAttribute(RequestDispatcherWrapper.BASE_PATH_ATTRIBUTE_NAME,
                PageContext.REQUEST_SCOPE);

        final ServletConfig servletConfig = (ServletConfig) getPageObject(PageContext.CONFIG);
        final ServletContext servletContext = servletConfig.getServletContext();

        for (Class<?> clazz = resolvingClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
            final String template = basePath + TemplateHelper.getAbsolutePath(clazz, page, '/');

            if (servletContext.getResource(template) != null) {
                // Tomcat returns a RequestDispatcher even if the JSP file doesn't exist so check if the resource exists first.
                final RequestDispatcher dispatcher = servletContext.getRequestDispatcher(template);

                if (dispatcher != null) {
                    try {
                        final HttpServletRequest request = (HttpServletRequest) getPageObject(PageContext.REQUEST);
                        final HttpServletResponse response = (HttpServletResponse) getPageObject(PageContext.RESPONSE);

                        dispatcher.include(request,
                                new Wrapper(response, new PrintWriter(jspContext.getOut())));
                    } catch (ServletException e) {
                        throw new JspException(e);
                    }
                    return;
                }
            }
        }

        throw new JspException(LocalizationMessages.UNABLE_TO_FIND_PAGE_FOR_RESOLVING_CLASS(page, resolvingClass));
    }

    class Wrapper extends HttpServletResponseWrapper {

        private final PrintWriter writer;

        Wrapper(HttpServletResponse httpServletResponse, PrintWriter w) {
            super(httpServletResponse);
            this.writer = w;
        }

        public PrintWriter getWriter() throws IOException {
            return writer;
        }
    }
}
