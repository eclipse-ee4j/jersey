/*
 * Copyright (c) 2010, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.reload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.examples.reload.compiler.AppClassLoader;
import org.glassfish.jersey.examples.reload.compiler.Compiler;
import org.glassfish.jersey.examples.reload.compiler.JavaFile;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Reload example application.
 * <p/>
 * A {@link ContainerLifecycleListener container listener} gets registered
 * with the application. Upon application startup notification, the listener schedules
 * a new {@link TimerTask timer task} to check a text file called {@code resources}
 * every 2 seconds. When the text file change is detected, the application gets reloaded with
 * a new {@link ResourceConfig resource configuration} including all
 * resource classes listed in that file.
 *
 * @author Jakub Podlesak
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private static final URI BASE_URI = URI.create("http://localhost:8080/flights/");
    public static final String ROOT_PATH = "arrivals";
    public static final String CONFIG_FILENAME = "resources";
    public static final String SRC_MAIN_JAVA = "src/main/java";

    static Container container;

    static class FileCheckTask extends TimerTask {

        @Override
        public void run() {

            WatchService watcher;

            try {
                watcher = FileSystems.getDefault().newWatchService();

                Path srcDir = Paths.get("src/main/java/org/glassfish/jersey/examples/reload");
                registerWatcher(watcher, srcDir);

                Path configFilePath = Paths.get(".");
                registerWatcher(watcher, configFilePath);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not initialize watcher service!");
            }

            for (;;) {

                try {
                    final WatchKey watchKey = watcher.take();

                    try {
                        for (WatchEvent<?> event : watchKey.pollEvents()) {
                            final WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                                Path modifiedFile = pathEvent.context();
                                System.out.printf("FILE MODIFIED: %s\n", modifiedFile);
                            }
                        }
                    } finally {
                        watchKey.reset(); // so that consecutive events could be processed
                    }

                    final File configFile = new File(CONFIG_FILENAME);
                    reloadApp(configFile);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void registerWatcher(WatchService watcher, Path directory) throws IOException {
            directory.register(watcher,
                    new WatchEvent.Kind[]{
                            StandardWatchEventKinds.ENTRY_MODIFY
                    },
                    /* SensitivityWatchEventModifier.HIGH */ getWatchEventModifiers());
        }

        private void reloadApp(final File configFile) {
            LOGGER.info("Reloading resource classes:");
            final ResourceConfig rc = createResourceConfig(configFile);
            App.container.reload(rc);
        }

        /**
         * @return sensitivity watch event modifier
         */
        private static WatchEvent.Modifier[] getWatchEventModifiers() {
            String className = "com.sun.nio.file.SensitivityWatchEventModifier";
            try {
                Class<?> c = Class.forName(className);
                Field f = c.getField("HIGH");
                WatchEvent.Modifier modifier = WatchEvent.Modifier.class.cast(f.get(c));
                return new WatchEvent.Modifier[] {modifier};
            } catch (Exception e) {
                return new WatchEvent.Modifier[] {};
            }
        }

    }

    private static ResourceConfig createResourceConfig(File configFile) {
        final ResourceConfig rc = new ResourceConfig();

        try {
            final AppClassLoader appClassLoader = new AppClassLoader(Thread.currentThread().getContextClassLoader());
            final List<JavaFile> javaFiles = getJavaFiles(configFile);

            Compiler.compile(appClassLoader, javaFiles);

            for (JavaFile javaFile : javaFiles) {
                try {
                    rc.registerClasses(appClassLoader.loadClass(javaFile.getClassName()));
                } catch (final ClassNotFoundException ex) {
                    LOGGER.info(String.format(" ! class %s not found.\n", javaFile.getClassName()));
                }
            }
        } catch (final Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rc;
    }

    private static List<JavaFile> getJavaFiles(File configFile) throws Exception {

        final List<JavaFile> javaFiles = new LinkedList<>();

        try (BufferedReader r = Files.newBufferedReader(configFile.toPath())) {
            while (r.ready()) {
                final String className = r.readLine();
                if (!className.startsWith("#")) {
                    javaFiles.add(new JavaFile(className, SRC_MAIN_JAVA));
                    LOGGER.info(String.format(" + included class %s.\n", className));
                } else {
                    LOGGER.info(String.format(" - ignored class %s\n", className.substring(1)));
                }
            }
        }
        return javaFiles;
    }

    public static void main(final String[] args) throws Exception {
        try {
            LOGGER.info("Resource Config Reload Jersey Example App");

            for (String s : args) {
                if (s.startsWith("-cp=")) {
                    Compiler.classpath = s.substring(4);
                }
            }

            final ResourceConfig resourceConfig = createResourceConfig(new File(CONFIG_FILENAME));
            registerReloader(resourceConfig);

            final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, true);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            }));
            server.start();

            System.out.println(
                    String.format("Application started.\nTry out %s%s\nStop the application using CTRL+C", BASE_URI, ROOT_PATH));

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Class<?> loadClass(String className) throws Exception {
        final JavaFile javaFile = new JavaFile(className, SRC_MAIN_JAVA);
        return Compiler.compile(className, javaFile);
    }

    private static void registerReloader(ResourceConfig resourceConfig) {
        resourceConfig.registerInstances(new ContainerLifecycleListener() {
            @Override
            public void onStartup(final Container container) {
                App.container = container;
                final Timer t = new Timer(true);
                t.schedule(new FileCheckTask(), 0);
            }

            @Override
            public void onReload(final Container container) {
                System.out.println("Application has been reloaded!");
            }

            @Override
            public void onShutdown(final Container container) {
                // ignore
            }
        });
    }
}
