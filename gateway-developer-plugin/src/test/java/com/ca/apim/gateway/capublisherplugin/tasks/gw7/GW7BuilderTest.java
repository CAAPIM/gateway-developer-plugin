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
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class GW7BuilderTest {

    @Test
    public void buildPackage() throws IOException {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        new Thread(() -> new GW7Builder().buildPackage(out, Stream.of(new GW7Builder.PackageFile("/my/file/path", 3L, () -> new ByteArrayInputStream(new byte[]{1, 2, 3}))).collect(Collectors.toSet()))).start();

        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(in));
        TarArchiveEntry entry = tarArchiveInputStream.getNextTarEntry();
        Assert.assertEquals("my/file/path", entry.getName());
        Assert.assertEquals(3L, entry.getSize());
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, IOUtils.toByteArray(tarArchiveInputStream));
        Assert.assertNull(tarArchiveInputStream.getNextTarEntry());
    }
}