/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

        private File file;

        FileTemplatizedBundle(File file) {
            this.file = file;
        }

        @Override
        public String getContents() {
            try {
                return new String(Files.readAllBytes(this.file.toPath()));
            } catch (IOException e) {
                throw new BundleDetemplatizeException("Could not read bundle file: " + file.getName(), e);
            }
        }

        @Override
        public void writeContents(String content) {
            try {
                Files.write(file.toPath(), content.getBytes());
            } catch (IOException e) {
                throw new BundleDetemplatizeException("Could not write detemplatized bundle to: " + file.getName(), e);
            }
        }

        @Override
        public String getName() {
            return this.file.getName();
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
