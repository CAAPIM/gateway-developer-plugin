/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

/**
 * Represents a Templatized Deployment Bundle and provide read/write to its contents.
 */
interface TemplatizedBundle {

    String getName();

    String getContents();

    void writeContents(String content);

    /**
     * Templatized bundle stored in the File System.
     */
    class FileTemplatizedBundle implements TemplatizedBundle {

        private final File newFile;
        private final File originalFile;

        FileTemplatizedBundle(File originalFile, File newFile) {
            this.newFile = newFile;
            this.originalFile = originalFile;
        }

        @Override
        public String getContents() {
            try {
                return new String(Files.readAllBytes(this.originalFile.toPath()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new BundleDetemplatizeException("Could not read bundle file: " + originalFile.getName(), e);
            }
        }

        @Override
        public void writeContents(String content) {
            try {
                Files.write(newFile.toPath(), Collections.singleton(content), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new BundleDetemplatizeException("Could not write detemplatized bundle to: " + newFile.getName(), e);
            }
        }

        @Override
        public String getName() {
            return this.originalFile.getName();
        }
    }

    /**
     * In-memory templatized bundle stored as a String.
     */
    class StringTemplatizedBundle implements TemplatizedBundle {

        private final String name;
        private String bundleContents;

        StringTemplatizedBundle(String name, String bundleContents) {
            this.name = name;
            this.bundleContents = bundleContents;
        }

        @Override
        public String getContents() {
            return bundleContents;
        }

        @Override
        public void writeContents(String content) {
            this.bundleContents = content;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
