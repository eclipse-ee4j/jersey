<?xml version='1.0'?>
<!--

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

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xslthl="http://xslthl.sf.net"
                exclude-result-prefixes="xslthl"
                version='1.0'>

  <xsl:template match='xslthl:keyword'>
    <span class="ReservedWord"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:comment'>
    <span class="Comment"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:oneline-comment'>
    <span class="Comment"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:multiline-comment'>
    <span class="DocComment"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:tag'>
    <span class="ReservedWord"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:attribute'>
    <span class="Identifier"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:value'>
    <span class="String"><xsl:value-of select='.'/></span>
  </xsl:template>

  <xsl:template match='xslthl:string'>
    <span class="String"><xsl:value-of select='.'/></span>
  </xsl:template>

</xsl:stylesheet>
