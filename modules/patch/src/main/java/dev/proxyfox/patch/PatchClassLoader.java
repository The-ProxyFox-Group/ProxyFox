/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.patch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Bootstrap ClassLoader instance for Patch.
 * <p>
 * Must be defined using {@code -Djava.system.class.loader=dev.proxyfox.patch.PatchClassLoader}
 *
 * @author Ampflower
 * @since ${version}
 **/
public class PatchClassLoader extends URLClassLoader {
    private static final File libraries = new File(System.getProperty("patch.libraries", "libraries"))
            .getAbsoluteFile();
    private static final String meta = "META-INF/patch";
    private static final int metaLength = 15;

    private static final URL[] urls;

    static {
        if (!libraries.exists() && !libraries.mkdirs()) {
            throw ioe("Cannot initialise " + libraries);
        }
        if (!libraries.isDirectory()) {
            throw ioe(libraries + " is not a directory");
        }

        final var classpath = new ArrayList<>(split(System.getProperty("java.class.path"), File.pathSeparatorChar));

        final var urlToSelf = PatchClassLoader.class.getProtectionDomain().getCodeSource().getLocation();

        System.out.println(urlToSelf);

        if (!urlToSelf.getPath().endsWith("/")) {
            try (final var outerStream = urlToSelf.openStream();
                 final var zipStream = new ZipInputStream(outerStream)) {
                ZipEntry entry;

                while ((entry = zipStream.getNextEntry()) != null) {
                    if (entry.getName().startsWith(meta)) {
                        final var library = new File(libraries, entry.getName().substring(metaLength));
                        classpath.add(library.toString());

                        if (library.exists()) continue;

                        try (final var libraryStream = new FileOutputStream(library)) {
                            zipStream.transferTo(libraryStream);
                        }
                    }
                }

            } catch (IOException ioe) {
                throw new IOError(ioe);
            }
        }


        final var builder = new StringBuilder();
        final var urlList = new ArrayList<URL>(classpath.size());
        try {
            for (final var file : classpath) {
                builder.append(file).append(File.pathSeparatorChar);
                urlList.add(new File(file).toURI().toURL());
            }
        } catch (MalformedURLException murle) {
            throw new ExceptionInInitializerError(murle);
        }
        System.setProperty("java.class.path", builder.substring(0, builder.length() - 1));
        urls = urlList.toArray(URL[]::new);
    }

    public PatchClassLoader(ClassLoader parent) {
        super(urls, parent.getParent());
    }

    public void appendToClassPathForInstrumentation(String path) throws MalformedURLException {
        this.addURL(new File(path).toURI().toURL());
    }

    public String toString() {
        return "patch";
    }

    private static Error ioe(String msg) {
        return new ExceptionInInitializerError(new IOException(msg));
    }

    private static List<String> split(final String string, final char by) {
        final var list = new ArrayList<String>();

        for (int l = 0, i = string.indexOf(by); i >= 0; i = string.indexOf(by, l = i + 1)) {
            list.add(string.substring(l, i));
        }

        return list;
    }
}
