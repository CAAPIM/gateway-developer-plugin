/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.capublisherplugin.tasks.gw7;

import com.ca.apim.gateway.cagatewayconfig.tasks.gw7.GW7Builder;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

class GW7BuilderTest {

    @Test
    void buildPackage() throws IOException {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        GW7Builder builder = new GW7Builder();
        builder.buildPackage(
                out,
                Stream.of(
                        new GW7Builder.PackageFile("/my/file/path", 3L, () -> new ByteArrayInputStream(new byte[]{1, 2, 3}))
                ).collect(Collectors.toSet())
        );

        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(in));
        TarArchiveEntry entry = tarArchiveInputStream.getNextTarEntry();
        Assertions.assertEquals("my/file/path", entry.getName());
        Assertions.assertEquals(3L, entry.getSize());
        Assertions.assertArrayEquals(new byte[]{1, 2, 3}, IOUtils.toByteArray(tarArchiveInputStream));
        Assertions.assertNull(tarArchiveInputStream.getNextTarEntry());
    }
}