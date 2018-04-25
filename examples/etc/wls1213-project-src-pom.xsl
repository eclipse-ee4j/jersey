<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Distribution License v. 1.0, which is available at
    http://www.eclipse.org/org/documents/edl-v10.php.

    SPDX-License-Identifier: BSD-3-Clause

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:pom="http://maven.apache.org/POM/4.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
        version="1.0">

    <xsl:output method="xml" indent="yes" />

    <xsl:template match="/">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template
            match="pom:dependencies/pom:dependency[pom:groupId='org.glassfish.jersey.core'
            or pom:groupId='org.glassfish.jersey.containers'
            or pom:artifactId='jersey-media-json-jackson'
            or pom:artifactId='jersey-media-json-jettison'
            or pom:artifactId='jersey-media-moxy'
            or pom:artifactId='jersey-media-multipart'
            or pom:artifactId='jersey-media-sse'
            or pom:groupId='com.sun.xml.bind'
            or pom:groupId='javax.servlet']/pom:scope[text()!=test]">
        <scope>provided</scope>
    </xsl:template>

    <xsl:template
            match="pom:dependencies/pom:dependency[pom:groupId='org.glassfish.jersey.core'
            or pom:groupId='org.glassfish.jersey.containers'
            or pom:artifactId='jersey-media-json-jackson'
            or pom:artifactId='jersey-media-json-jettison'
            or pom:artifactId='jersey-media-moxy'
            or pom:artifactId='jersey-media-multipart'
            or pom:artifactId='jersey-media-sse'
            or pom:groupId='com.sun.xml.bind'
            or pom:groupId='javax.servlet']">
        <xsl:copy>
            <xsl:apply-templates />
            <xsl:if test="count(pom:scope)=0">
                <scope>provided</scope>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template
            match="pom:dependencies/pom:dependency[pom:artifactId='jersey-bean-validation'
            or pom:artifactId='jersey-mvc-jsp'
            or pom:artifactId='jersey-mvc-freemarker'
            or pom:artifactId='jersey-media-json-processing']/pom:scope[text()!=test]">
        <exclusions>
            <exclusion>
                <groupId>org.glassfish.jersey.core</groupId>
                <artifactId>jersey-common</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.glassfish.jersey.core</groupId>
                <artifactId>jersey-server</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.glassfish.hk2.external</groupId>
                <artifactId>javax.inject</artifactId>
            </exclusion>
            <exclusion>
                <groupId>javax.ws.rs</groupId>
                <artifactId>javax.ws.rs-api</artifactId>
            </exclusion>
        </exclusions>
    </xsl:template>

    <xsl:template
            match="pom:dependencies/pom:dependency[pom:artifactId='jersey-bean-validation'
            or pom:artifactId='jersey-mvc-jsp'
            or pom:artifactId='jersey-mvc-freemarker'
            or pom:artifactId='jersey-media-json-processing']">
        <xsl:copy>
            <xsl:apply-templates />
            <xsl:if test="count(pom:scope)=0">
                <exclusions>
                    <exclusion>
                        <groupId>org.glassfish.jersey.core</groupId>
                        <artifactId>jersey-common</artifactId>
                    </exclusion>
                    <exclusion>
                        <groupId>org.glassfish.jersey.core</groupId>
                        <artifactId>jersey-server</artifactId>
                    </exclusion>
                    <exclusion>
                        <groupId>org.glassfish.hk2.external</groupId>
                        <artifactId>javax.inject</artifactId>
                    </exclusion>
                    <exclusion>
                        <groupId>javax.ws.rs</groupId>
                        <artifactId>javax.ws.rs-api</artifactId>
                    </exclusion>
                </exclusions>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="pom:dependencies">
        <xsl:copy>
            <xsl:apply-templates />
            <xsl:if test="count(pom:dependency[pom:artifactId='jersey-container-servlet-core'])=0">
                <dependency>
                    <groupId>org.glassfish.jersey.containers</groupId>
                    <artifactId>jersey-container-servlet-core</artifactId>
                    <scope>provided</scope>
                </dependency>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="pom:project">
        <xsl:copy>
            <xsl:apply-templates />
            <xsl:if test="count(pom:dependencies)=0">
                <dependencies>
                    <dependency>
                        <groupId>org.glassfish.jersey.containers</groupId>
                        <artifactId>jersey-container-servlet-core</artifactId>
                        <scope>provided</scope>
                    </dependency>
                </dependencies>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- remove <packagingExcludes>WEB-INF/glassfish-web.xml</packagingExcludes>
         as this file is required in Glassfish bundle since <class-loader>
         is defined in it -->
    <xsl:template match="pom:plugin[pom:artifactId='maven-war-plugin']/pom:configuration[pom:packagingExcludes]">
    </xsl:template>

    <!--build war even if web.xml is missing as it's not required,
        <packagingIncludes> defaults to 'all' so it includes
        <packagingIncludes>WEB-INF/glassfish-web.xml</packagingIncludes>
        to pick up <class-loader> -->
    <xsl:template match="pom:plugin[pom:artifactId='maven-war-plugin']">
        <xsl:copy>
            <xsl:apply-templates />
            <xsl:if test="count(pom:configuration)=1">
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- remove examples-source-zip profile -->
    <xsl:template match="pom:profile/pom:plugins/pom:plugin[pom:id='examples-source-zip']">
    </xsl:template>

    <!--&lt;!&ndash; remove xslt-maven-plugin &ndash;&gt;-->
    <!--<xsl:template match="pom:plugin[pom:artifactId='xml-maven-plugin']">-->
    <!--</xsl:template>-->

    <!--&lt;!&ndash; remove maven-assembly-plugin &ndash;&gt;-->
    <!--<xsl:template match="pom:plugin[pom:artifactId='maven-assembly-plugin']">-->
    <!--</xsl:template>-->

    <!-- remove maven-jetty-plugin -->
    <xsl:template match="pom:plugin[pom:artifactId='maven-jetty-plugin']">
    </xsl:template>

    <!-- remove jetty-maven-plugin -->
    <xsl:template match="pom:plugin[pom:artifactId='jetty-maven-plugin']">
    </xsl:template>

    <xsl:template match="comment()">
        <xsl:comment>
            <xsl:value-of select="." />
        </xsl:comment>
    </xsl:template>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
