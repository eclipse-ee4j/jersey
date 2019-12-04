package org.glassfish.jersey.wadl.doclet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

class Loader extends URLClassLoader {

    public Loader(final String[] paths, final ClassLoader parent) {
        super(getURLs(paths), parent);
    }

    Loader(final String[] paths) {
        super(getURLs(paths));
    }

    private static URL[] getURLs(final String[] paths) {
        final List<URL> urls = new ArrayList<>();
        for (final String path : paths) {
            try {
                urls.add(new File(path).toURI().toURL());
            } catch (final MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

}
