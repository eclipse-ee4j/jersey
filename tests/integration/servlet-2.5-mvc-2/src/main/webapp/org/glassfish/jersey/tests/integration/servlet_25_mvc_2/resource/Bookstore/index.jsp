<%--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

--%>

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css" media="screen">
          @import url( <c:url value="/css/style.css"/> );
        </style>
        <title>REST Bookstore Sample</title>
    </head>
    <body>

    <h1>${it.name}</h1>
    
    <h2>Item List</h2>

    <ul>
        <c:forEach var="i" items="${it.items}">
            <li><a href="items/${i.key}/">${i.value.title}</a>
        </c:forEach>
    </ul>
    
    <h2>Others</h2>
    <p>
      <a href="count">count inventory</a>
    <p>
      <a href="time">get the system time</a>
    <p>
      <a href="jsp/help.jsp">regular resources</a>
    </p>    
    </body>
</html>
