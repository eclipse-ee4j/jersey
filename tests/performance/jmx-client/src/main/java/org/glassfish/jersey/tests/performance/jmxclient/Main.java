/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.jmxclient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * JMX Client entry point.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class Main {

    public static void main(String[] args) throws Exception {

//      e.g. "service:jmx:rmi:///jndi/rmi://sysifos.cz.oracle.com:11112/jmxrmi"
        final String jmxUrl = args[0];
//      e.g. "org.glassfish.jersey.test.performance.interceptor.dynamic:type=DynamicallyBoundInterceptorResource,name=gets"
        final String mBeanName = args[1];
//      e.g. "OneMinuteRate"
        final String mBeanAttrName = args[2];
//      e.g. 50
        final int sampleCount = Integer.parseInt(args[3]);
//      e.g. "phishing.properties"
        final String propertiesFile = args[4];

        System.out.printf("JMX URL = %s\nMBean = %s\nattribute = %s\nsamples = %d\nfilename = %s\n"
                + "Going to connect...\n",
                jmxUrl, mBeanName, mBeanAttrName, sampleCount, propertiesFile);

        final JMXServiceURL url = new JMXServiceURL(jmxUrl);
        final JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        final MBeanServerConnection mBeanServer = jmxc.getMBeanServerConnection();
        final ObjectName mBeanObjectName = new ObjectName(mBeanName);

        System.out.println("Connected...");

        double totalSum = 0;
        int samplesTaken = 0;

        for (int i = 0; i < sampleCount; i++) {
            Thread.sleep(5000);

            Double sample = (Double) mBeanServer.getAttribute(mBeanObjectName, mBeanAttrName);

            System.out.printf("OMR[%d]=%f\n", i, sample);

            totalSum += sample;
            samplesTaken++;
        }

        jmxc.close();

        final double result = totalSum / samplesTaken;
        writeResult(result, propertiesFile);

        System.out.printf("\nAverage=%f\n", result);
    }

    private static void writeResult(double resultValue, String propertiesFile) throws IOException {
        Properties resultProps = new Properties();
        resultProps.put("YVALUE", Double.toString(resultValue));
        resultProps.store(new FileOutputStream(propertiesFile), null);
    }
}
