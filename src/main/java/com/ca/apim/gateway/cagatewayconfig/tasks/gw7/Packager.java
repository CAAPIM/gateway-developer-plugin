/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.gw7;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class Packager {
    private final FileUtils fileUtils;

    public Packager(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    public void buildPackage(File gw7File, Set<File> templatizedBundles, Set<File> scripts) {
        try(TarArchiveOutputStream taos = new TarArchiveOutputStream(new GzipCompressorOutputStream(fileUtils.getOutputStream(gw7File)))) {
            InputStream applyEnvStream = getClass().getResourceAsStream("/scripts/apply-environment.sh");
            byte[] applyEnvBytes = IOUtils.toByteArray(applyEnvStream);
            applyEnvStream.close();
            TarArchiveEntry applyEnv = new TarArchiveEntry("/opt/docker/rc.d/apply-environment.sh");
            applyEnv.setSize(applyEnvBytes.length);
            taos.putArchiveEntry(applyEnv);
            IOUtils.copy(new ByteArrayInputStream(applyEnvBytes), taos);
            taos.closeArchiveEntry();

            writeFiles(taos, scripts, "/opt/docker/rc.d/");
            writeFiles(taos, templatizedBundles, "/opt/docker/rc.d/bundle/templatized/");
        } catch (IOException e) {
            throw new PackageBuildException("Error building GW7 Package: " + e.getMessage(), e);
        }
    }

    private void writeFiles(TarArchiveOutputStream taos, Set<File> files, String directory) {
        for (File file : files) {
            try {
                TarArchiveEntry ze = new TarArchiveEntry(file, directory + file.getName());
                taos.putArchiveEntry(ze);
                InputStream inputStream = fileUtils.getInputStream(file);
                IOUtils.copy(inputStream, taos);
                inputStream.close();
                taos.closeArchiveEntry();
            } catch (IOException e) {
                throw new PackageBuildException("Error building GW7 Package. Error adding file: " + file.getPath() + " Message: " + e.getMessage(), e);
            }
        }
    }
}
