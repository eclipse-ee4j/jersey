<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

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
            or pom:groupId='org.glassfish.jersey.ext.cdi'
            or pom:groupId='org.glassfish.jersey.media'
            or pom:artifactId='jersey-wadl-doclet'
            or pom:artifactId='jersey-mvc-jsp'
            or pom:artifactId='jersey-bean-validation'
            or pom:groupId='com.sun.xml.bind'
            or pom:groupId='org.codehaus.jettison'
            or pom:groupId='javax.annotation'
            or pom:groupId='javax.enterprise'
            or pom:groupId='javax.servlet'
            or pom:groupId='javax.ws.rs']/pom:scope[text()!=test]">
        <scope>provided</scope>
    </xsl:template>

    <xsl:template
            match="pom:dependencies/pom:dependency[pom:groupId='org.glassfish.jersey.core'
            or pom:groupId='org.glassfish.jersey.containers'
            or pom:groupId='org.glassfish.jersey.ext.cdi'
            or pom:groupId='org.glassfish.jersey.media'
            or pom:artifactId='jersey-wadl-doclet'
            or pom:artifactId='jersey-mvc-jsp'
            or pom:artifactId='jersey-bean-validation'
            or pom:groupId='com.sun.xml.bind'
            or pom:groupId='javax.validation'
            or pom:groupId='org.codehaus.jettison'
            or pom:groupId='javax.annotation'
            or pom:groupId='javax.enterprise'
            or pom:groupId='javax.servlet'
            or pom:groupId='javax.ws.rs']">
        <xsl:copy>
            <xsl:apply-templates />
            <xsl:if test="count(pom:scope)=0">
                <scope>provided</scope>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template
            match="pom:dependencies/pom:dependency[pom:artifactId='jersey-mvc-freemarker']">
        <xsl:copy>
            <xsl:apply-templates />
            <exclusions>
              <exclusion>
                <groupId>org.glassfish.jersey.ext</groupId>
                <artifactId>jersey-mvc</artifactId>
              </exclusion>
              <exclusion>
                <groupId>javax.ws.rs</groupId>
                <artifactId>javax.ws.rs-api</artifactId>
              </exclusion>
            </exclusions>
        </xsl:copy>
    </xsl:template>

    <!-- There is problem to run Spring example on GF - https://java.net/jira/browse/JERSEY-2032
    <xsl:template
            match="pom:dependencies/pom:dependency[pom:artifactId='jersey-spring3']">
        <xsl:copy>
            <xsl:apply-templates />
            <exclusions>
                <exclusion>
                    <groupId>javax.ws.rs</groupId>
                    <artifactId>javax.ws.rs-api</artifactId>
                </exclusion>


                <exclusion>
                    <groupId>org.glassfish.jersey.core</groupId>
                    <artifactId>jersey-server</artifactId>
                </exclusion>

                <exclusion>
                    <groupId>org.glassfish.jersey.containers</groupId>
                    <artifactId>jersey-container-servlet-core</artifactId>
                </exclusion>

                <exclusion>
                    <groupId>org.glassfish.hk2</groupId>
                    <artifactId>hk2</artifactId>
                </exclusion>

                <exclusion>
                    <groupId>javax.servlet</groupId>
                    <artifactId>javax.servlet-api</artifactId>
                </exclusion>
            </exclusions>
        </xsl:copy>
    </xsl:template>
    -->

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
        <xsl:if test="count(pom:dependency[pom:artifactId='jersey-mvc-freemarker'])=1">
          <dependency>
            <groupId>org.glassfish.jersey.ext</groupId>
            <artifactId>jersey-mvc</artifactId>
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

    <!-- remove failsafe plugin (integration testing not possible without jetty/other container) -->
    <xsl:template match="pom:plugin[pom:artifactId='maven-failsafe-plugin']">
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
