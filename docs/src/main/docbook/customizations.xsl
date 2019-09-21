<?xml version="1.0" encoding="utf-8"?>
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
                xmlns:d="http://docbook.org/ns/docbook"
                exclude-result-prefixes="d"
                version="1.0">

    <xsl:import href="urn:docbkx:stylesheet"/>
    <!--xsl:import href="urn:docbkx:stylesheet/highlight.xsl"/-->

    <xsl:template name="user.header.content">
        <xsl:element name="div">
            <xsl:attribute name="style">float:right</xsl:attribute>
            <xsl:element name="a">
                <xsl:attribute name="href">https://jersey.github.io</xsl:attribute>
                <xsl:element name="img">
                    <xsl:attribute name="src">https://jersey.github.io/images/jersey_logo.png</xsl:attribute>
                </xsl:element>
            </xsl:element>
        </xsl:element>
        <xsl:element name="small">Links:
            <xsl:element name="a">
                <xsl:attribute name="href">index.html</xsl:attribute>
                Table of Contents
            </xsl:element> | <xsl:element name="a">
                <xsl:attribute name="href">user-guide.html</xsl:attribute>
                Single HTML
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template name="user.footer.content">
        <xsl:element name="link">
            <xsl:attribute name="href">https://jersey.github.io/sh/shCore.css</xsl:attribute>
            <xsl:attribute name="rel">stylesheet</xsl:attribute>
            <xsl:attribute name="type">text/css</xsl:attribute>
        </xsl:element>
        <xsl:element name="link">
            <xsl:attribute name="href">https://jersey.github.io/sh/shThemeDefault.css</xsl:attribute>
            <xsl:attribute name="rel">stylesheet</xsl:attribute>
            <xsl:attribute name="type">text/css</xsl:attribute>
        </xsl:element>
        <xsl:element name="script">
            <xsl:attribute name="src">https://jersey.github.io/sh/shCore.js</xsl:attribute>
            <xsl:attribute name="type">text/javascript</xsl:attribute>
        </xsl:element>
        <xsl:element name="script">
            <xsl:attribute name="src">https://jersey.github.io/sh/shAutoloader.js</xsl:attribute>
            <xsl:attribute name="type">text/javascript</xsl:attribute>
        </xsl:element>
        <xsl:element name="script">
            <xsl:attribute name="type">text/javascript</xsl:attribute>
            function path() {
              var args = arguments,
                  result = []
                  ;

              for(var i = 0; i &lt; args.length; i++)
                  result.push(args[i].replace('@', 'https://jersey.github.io/sh/'));

              return result
            };

            SyntaxHighlighter.autoloader.apply(null, path(
              'applescript            @shBrushAppleScript.js',
              'actionscript3 as3      @shBrushAS3.js',
              'bash shell             @shBrushBash.js',
              'coldfusion cf          @shBrushColdFusion.js',
              'cpp c                  @shBrushCpp.js',
              'c# c-sharp csharp      @shBrushCSharp.js',
              'css                    @shBrushCss.js',
              'delphi pascal          @shBrushDelphi.js',
              'diff patch pas         @shBrushDiff.js',
              'erl erlang             @shBrushErlang.js',
              'groovy                 @shBrushGroovy.js',
              'java                   @shBrushJava.js',
              'jfx javafx             @shBrushJavaFX.js',
              'js jscript javascript  @shBrushJScript.js',
              'perl pl                @shBrushPerl.js',
              'php                    @shBrushPhp.js',
              'text plain             @shBrushPlain.js',
              'py python              @shBrushPython.js',
              'ruby rails ror rb      @shBrushRuby.js',
              'sass scss              @shBrushSass.js',
              'scala                  @shBrushScala.js',
              'sql                    @shBrushSql.js',
              'vb vbnet               @shBrushVb.js',
              'xml xhtml xslt html    @shBrushXml.js'
            ));

            SyntaxHighlighter.all();
        </xsl:element>
    </xsl:template>

    <xsl:template name="user.head.content">
        <xsl:element name="script">
            <xsl:attribute name="type">text/javascript</xsl:attribute>
            var _gaq = _gaq || [];
            _gaq.push(['_setAccount', 'UA-3160303-1']);
            _gaq.push(['_trackPageview']);

            (function() {
                var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
            })();
        </xsl:element>
    </xsl:template>

    <xsl:template match="d:programlisting">
<xsl:element name="pre"><xsl:attribute name="class">
    toolbar: false;
    brush: <xsl:value-of select="@language"/>;
    <xsl:if test="@linenumbering = 'unnumbered'">gutter: false;</xsl:if>
    <xsl:if test="@startinglinenumber &gt; 1">first-line: <xsl:value-of select="@startinglinenumber"/>;</xsl:if>
</xsl:attribute><xsl:apply-templates/></xsl:element>
    </xsl:template>
</xsl:stylesheet>
