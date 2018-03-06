/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.sanitize;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class SanitizeBundleTask extends DefaultTask {
    private final Transformer transformer;

    private RegularFileProperty inputBundleFile;
    private RegularFileProperty outputBundleFile;

    @Inject
    public SanitizeBundleTask() {
        this(DocumentTools.INSTANCE);
    }

    private SanitizeBundleTask(final DocumentTools documentTools) {
        inputBundleFile = newInputFile();
        outputBundleFile = newOutputFile();

        final StreamSource stylesheet = new StreamSource(this.getClass().getResourceAsStream("/sanitize-bundle.xsl"));
        transformer = documentTools.getTransformer(stylesheet);
    }

    @InputFile
    public RegularFileProperty getInputBundleFile() {
        return inputBundleFile;
    }

    @OutputFile
    public RegularFileProperty getOutputBundleFile() {
        return outputBundleFile;
    }

    @TaskAction
    public void perform() throws TransformerException, FileNotFoundException {
        final StreamSource source = new StreamSource(inputBundleFile.getAsFile().get());
        final StreamResult streamResult = new StreamResult(new FileOutputStream(outputBundleFile.getAsFile().get()));
        transformer.transform(source, streamResult);
    }
}
