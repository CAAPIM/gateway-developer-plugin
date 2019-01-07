/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;

/**
 * Utility methods for project dependencies handling.
 */
public class ProjectDependencyUtils {

    private static final String BUNDLE = ".bundle";
    private static final String AAR = ".aar";
    private static final String JAR = ".jar";

    private ProjectDependencyUtils() { }

    /**
     * Filter only bundle files.
     *
     * @param files the full collection of files
     * @return bundle files
     */
    @SuppressWarnings("squid:S1319") // we need linkedlist here explicitly
    public static LinkedList<File> filterBundleFiles(Collection<File> files) {
        return filter(files, BUNDLE, LinkedList::new);
    }

    /**
     * Filter only modular assertion (AAR) files.
     *
     * @param files the full collection of files
     * @return bundle files
     */
    public static Set<File> filterModularAssertionFiles(Collection<File> files) {
        return filter(files, AAR, LinkedHashSet::new);
    }

    /**
     * Filter only jar files.
     *
     * @param files the full collection of files
     * @return bundle files
     */
    public static Set<File> filterJarFiles(Collection<File> files) {
        return filter(files, JAR, LinkedHashSet::new);
    }

    @NotNull
    private static <C extends Collection<File>> C filter(Collection<File> files, String filter, Supplier<C> collectionFactory) {
        return files.stream().filter(f -> f.getName().endsWith(filter)).collect(toCollection(collectionFactory));
    }
}
