<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:web="http://java.sun.com/xml/ns/javaee"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
        version="1.0">

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <!-- fixes the hardcoded port in managed-client-webapp example's web.xml -->
    <xsl:template match="//web:web-app/web:servlet/web:init-param[web:param-name='org.glassfish.jersey.examples.managedclient.ClientA.baseUri']/web:param-value">
        <xsl:copy>
            <xsl:text>http://localhost:7001/managed-client-webapp/internal</xsl:text>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
