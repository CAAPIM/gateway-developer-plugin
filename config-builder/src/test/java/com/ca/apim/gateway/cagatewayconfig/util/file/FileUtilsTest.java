/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.file;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TemporaryFolderExtension.class)
class FileUtilsTest {

    private TemporaryFolder rootProjectDir;
    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;
    }

    @Test
    void getInputStream() throws IOException {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "file.file");
        Files.write(path, "Test".getBytes(Charset.defaultCharset()));
        InputStream stream = FileUtils.INSTANCE.getInputStream(path.toFile());
        assertNotNull(stream);
        // this is required while running in windows env it' s not possible to delete folders with files inside
        stream.close();
        Files.delete(path);
    }

    @Test
    void getInputStreamMissingFile() {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "file.file");
        assertThrows(EntityBuilderException.class, () -> FileUtils.INSTANCE.getInputStream(path.toFile()));
    }

    @Test
    void getOutputStream() throws IOException {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(),  "file.file");
        OutputStream stream = FileUtils.INSTANCE.getOutputStream(path.toFile());
        assertNotNull(stream);
        // this is required while running in windows env it' s not possible to delete folders with files inside
        stream.close();
        Files.delete(path);
    }

    @Test
    void getOutputStreamMissingPath() {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(),  "test", "file.file");
        assertThrows(EntityBuilderException.class, () -> FileUtils.INSTANCE.getOutputStream(path.toFile()));
    }

    @Test
    void getFileAsString() throws IOException {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "file.file");
        Files.write(path, "Test".getBytes(Charset.defaultCharset()));
        String content = FileUtils.INSTANCE.getFileAsString(path.toFile());
        assertNotNull(content);
        assertEquals("Test", content);
    }

    @Test
    void getFileNonexistent() throws IOException {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "file.file");
        assertThrows(EntityBuilderException.class, () -> FileUtils.INSTANCE.getFileAsString(path.toFile()));
    }

}