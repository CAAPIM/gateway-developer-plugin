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

/**
 * The packager build a Gateway Deployment Package. A GW7 file is to be able to package bundles, aars, bootstrap
 * scripts, solution kits, etc... all into a single file. This helps make distributing and deploying solutions simpler,
 * a single file to download, a single volume to mount.
 * <p>
 * Here is an example of a GW7 file structure:
 * <pre>
 * opt/
 *   - docker/rc.d/
 *     - apply-environment.sh
 *   - SecureSpan/Gateway/
 *     - node/default/etc/bootstrap/bundle/
 *       - 1-my-bundle-1.0.00.req.bundle
 *       - custom-assertion.req.bundle
 *       - gateway-developer-example.req.bundle
 *       - helloworld.req.bundle
 *     - runtime/modules/
 *       - assertions/
 *         - Hello-World-Assertion-0.1.01.aar
 *       - lib/
 *         - custom-assertion-1.0.0.jar
 * </pre>
 * The above package will add:
 * <ul>
 * <li>a bootstrap script apply-environment.sh</li>
 * <li>4 bundle: 1-my-bundle-1.0.00.req.bundle, custom-assertion.req.bundle, gateway-developer-example.req.bundle, helloworld.req.bundle</li>
 * <li>a modular assertion: Hello-World-Assertion-0.1.01.aar</li>
 * <li>a custom assertion: custom-assertion-1.0.0.jar</li>
 * </ul>
 */
class GW7Builder {
    private static final String DIRECTORY_OPT_DOCKER_RC_D = "/opt/docker/rc.d/";
    private final FileUtils fileUtils;

    GW7Builder(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    void buildPackage(File gw7File, Set<File> templatizedBundles, Set<File> scripts) {
        byte[] applyEnvBytes = getApplyEnvironmentScriptBytes();

        try (TarArchiveOutputStream taos = new TarArchiveOutputStream(new GzipCompressorOutputStream(fileUtils.getOutputStream(gw7File)))) {
            TarArchiveEntry applyEnv = new TarArchiveEntry(DIRECTORY_OPT_DOCKER_RC_D + "apply-environment.sh");
            applyEnv.setSize(applyEnvBytes.length);
            try (ByteArrayInputStream input = new ByteArrayInputStream(applyEnvBytes)) {
                addTarEntry(taos, applyEnv, input);
            }

            writeFiles(taos, scripts, DIRECTORY_OPT_DOCKER_RC_D);
            writeFiles(taos, templatizedBundles, DIRECTORY_OPT_DOCKER_RC_D + "bundle/templatized/");
        } catch (IOException e) {
            throw new PackageBuildException("Error building GW7 Package: " + e.getMessage(), e);
        }
    }

    private void writeFiles(TarArchiveOutputStream taos, Set<File> files, String directory) {
        for (File file : files) {
            try (InputStream inputStream = fileUtils.getInputStream(file)) {
                addTarEntry(taos, new TarArchiveEntry(file, directory + file.getName()), inputStream);
            } catch (IOException e) {
                throw new PackageBuildException("Error building GW7 Package. Error adding file: " + file.getPath() + " Message: " + e.getMessage(), e);
            }
        }
    }

    private void addTarEntry(TarArchiveOutputStream taos, TarArchiveEntry tarEntry, InputStream entryStream) throws IOException {
        taos.putArchiveEntry(tarEntry);
        IOUtils.copy(entryStream, taos);
        taos.closeArchiveEntry();
    }

    private byte[] getApplyEnvironmentScriptBytes() {
        try (InputStream applyEnvStream = getClass().getResourceAsStream("/scripts/apply-environment.sh")) {
            return IOUtils.toByteArray(applyEnvStream);
        } catch (IOException e) {
            throw new PackageBuildException("Error loading apply-environment.sh script bytes: " + e.getMessage(), e);
        }
    }
}
