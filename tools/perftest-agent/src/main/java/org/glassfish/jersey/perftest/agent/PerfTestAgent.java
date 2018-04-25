/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.perftest.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;

import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

/**
 * Java agent that instruments any Jersey 2 ApplicationHandler found with a new
 * Metrics timer. If parameter is given, it will be understood as another class.method
 * (delimited by a single dot) that has to be instrumented instead of the Jersey handler.
 *
 * This one has been tested with Grizzly 2, Catalina and WLS.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class PerfTestAgent {

    private static final String HANDLER_CLASS_NAME = "org.glassfish.jersey.server.ApplicationHandler";
    private static final String HANDLER_METHOD_NAME = "handle";

    public static void premain(String agentArgs, Instrumentation instrumentation) {

        final String handlerClassName = (agentArgs != null && !agentArgs.isEmpty()) ? agentArgs.substring(0, agentArgs.lastIndexOf('.')) : HANDLER_CLASS_NAME;
        final String handlerMethodName = (agentArgs != null && !agentArgs.isEmpty()) ? agentArgs.substring(agentArgs.lastIndexOf('.') + 1) : HANDLER_METHOD_NAME;

        instrumentation.addTransformer(new ClassFileTransformer() {

            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
                if (handlerClassName.replaceAll("\\.", "/").equals(className)) {
                    try {
                        ClassPool cp = ClassPool.getDefault();
                        cp.appendSystemPath();
                        CtClass cc = cp.makeClass(new java.io.ByteArrayInputStream(bytes));

                        final CtField ctxField = CtField.make("public static final agent.metrics.Timer.Context agentTimerCtx;", cc);
                        final CtField registryField = CtField.make("public static final agent.metrics.MetricRegistry agentREG = new agent.metrics.MetricRegistry();", cc);
                        final CtField reporterField = CtField.make("public static final agent.metrics.JmxReporter agentReporter = agent.metrics.JmxReporter.forRegistry(agentREG).build();", cc);
                        final CtField timerField = CtField.make("public static final agent.metrics.Timer agentTimer = "
                                + "agentREG.timer(agent.metrics.MetricRegistry.name(\"" + handlerClassName + "\", new String[] {\"" + handlerMethodName + "\"}));", cc);
                        cc.addField(registryField);
                        cc.addField(reporterField);
                        cc.addField(timerField);
                        cc.makeClassInitializer().insertAfter("agentReporter.start();");

                        CtMethod m = cc.getDeclaredMethod(handlerMethodName);
                        m.addLocalVariable("agentCtx", ctxField.getType());
                        m.insertBefore("agentCtx = agentTimer.time();");
                        m.insertAfter("agentCtx.stop();", true);
                        byte[] byteCode = cc.toBytecode();
                        cc.detach();
                        System.out.printf("Jersey Perf Agent Instrumentation Done! (instrumented method: %s)\n", m.getLongName());
                        return byteCode;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
    }
}
