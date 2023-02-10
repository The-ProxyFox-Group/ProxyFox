/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.patch;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Ampflower
 * @since ${version}
 **/
public class PatchAgent {
    private static final Path libraries = Path.of(System.getProperty("patch.libraries", "libraries")).toAbsolutePath();
    private static final String meta = "META-INF/patch/";
    private static final int metaLength = 15;

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Throwable {
        final var classpath = classpath();

        System.out.println("Preparing with the following: " + classpath);

        final var builder = new StringBuilder(System.getProperty("java.class.path"));

        for (final var path : classpath) {
            System.out.println("Injecting " + path + " into system");
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(path));
            builder.append(File.pathSeparatorChar).append(path);
        }

        System.setProperty("java.class.path", builder.toString());
    }

    /**
     * Creates a new classpath to append.
     */
    public static List<String> classpath() throws IOException, URISyntaxException {
        if (Files.notExists(libraries)) {
            Files.createDirectories(libraries);
        }
        if (!Files.isDirectory(libraries)) {
            throw new IOException(libraries + " is not a directory");
        }

        final var classpath = new ArrayList<String>(); // split(, File.pathSeparatorChar);

        final var urlToSelf = PatchAgent.class.getProtectionDomain().getCodeSource().getLocation();

        if (!Files.isDirectory(Path.of(urlToSelf.toURI()))) {
            try (final var outer = urlToSelf.openStream();
                 final var zip = new ZipInputStream(outer)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (!entry.getName().startsWith(meta)) continue;

                    final var path = libraries.resolve(entry.getName().substring(metaLength));
                    classpath.add(path.toString());
                    if (Files.exists(path)) continue;

                    try (final var file = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
                        zip.transferTo(file);
                    }
                }
            }
        }

        return classpath;
    }
}
