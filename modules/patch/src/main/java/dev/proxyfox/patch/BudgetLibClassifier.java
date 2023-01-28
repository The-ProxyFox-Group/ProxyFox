/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.patch;// Created 2023-03-01T01:42:32

import net.fabricmc.api.EnvType;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;

/**
 * @author Ampflower
 * @since 2.0.8
 **/
public class BudgetLibClassifier<T extends Enum<T> & Library> {
    private final EnumMap<T, Path> origins;
    private final Set<Path> unmatched;
    private final Class<T> clazz;

    public BudgetLibClassifier(Class<T> clazz) {
        this.clazz = clazz;
        this.origins = new EnumMap<>(clazz);
        this.unmatched = new HashSet<>();
    }

    public void process(final Collection<Path> paths, final EnvType envType) throws IOException {
        final List<T> values;
        if (envType != null) {
            final var tmp = new ArrayList<T>();
            for (var value : clazz.getEnumConstants()) {
                if (value.envType() == null || value.envType() == envType) {
                    tmp.add(value);
                }
            }

            values = List.copyOf(tmp);
        } else {
            values = List.of(clazz.getEnumConstants());
        }

        for (final var path : paths) {
            boolean found = false;
            if (Files.isDirectory(path)) {
                for (final var lib : values) {
                    for (final var lpath : lib.paths())
                        if (Files.exists(path.resolve(lpath))) {
                            found = true;
                            final var old = origins.putIfAbsent(lib, path);
                            if (old != null) {
                                Log.warn(LogCategory.GENERAL, "Found %s for %s but %s is already present!", path, lib, old);
                            }
                        }
                }
            } else if (Files.isRegularFile(path)) {
                try (final var zipfile = new ZipFile(path.toFile())) {
                    for (final var lib : values) {
                        for (final var lpath : lib.paths())
                            if (zipfile.getEntry(lpath) != null) {
                                found = true;
                                final var old = origins.putIfAbsent(lib, path);
                                if (old != null) {
                                    Log.warn(LogCategory.GENERAL, "Found %s for %s but %s is already present!", path, lib, old);
                                }
                            }
                    }
                }
            }
            if (!found) {
                unmatched.add(path);
            }
        }
    }

    public Path getOrigin(T library) {
        return origins.get(library);
    }

    public Set<Path> getUnmatched() {
        return unmatched;
    }

    public String getClassName(T library) {
        if (!origins.containsKey(library)) {
            return null;
        }

        final var path = library.path();

        if (path.endsWith(".class")) {
            return path.substring(0, path.length() - 6).replace('/', '.');
        }

        return null;
    }

    public boolean is(Path path, T library) {
        return origins.get(library).equals(path);
    }

    public boolean has(T library) {
        return origins.containsKey(library);
    }
}
