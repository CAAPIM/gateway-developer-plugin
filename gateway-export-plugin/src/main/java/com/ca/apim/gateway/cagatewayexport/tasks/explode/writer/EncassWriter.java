/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.EncassArgument;
import com.ca.apim.gateway.cagatewayconfig.beans.EncassResult;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.writeFile;
import static java.util.stream.Collectors.toCollection;

@Singleton
public class EncassWriter implements EntityWriter {
    private static final String ENCASS_FILE = "encass";
    private final DocumentFileUtils documentFileUtils;
    private final JsonTools jsonTools;

    @Inject
    EncassWriter(DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        Map<String, Encass> encassBeans = bundle.getEntities(Encass.class)
                .values()
                .stream()
                .collect(Collectors.toMap(Encass::getName, this::getEncassBean));

        writeFile(rootFolder, documentFileUtils, jsonTools, encassBeans, ENCASS_FILE, Encass.class);
    }

    @VisibleForTesting
    Encass getEncassBean(Encass encassEntity) {
        Encass encassBean = new Encass();
        encassBean.setPolicy(encassEntity.getPolicy());
        encassBean.setArguments(
                encassEntity.getArguments()
                        .stream()
                        .map(
                                encassParam -> new EncassArgument(
                                        encassParam.getName(),
                                        encassParam.getType(),
                                        encassParam.getRequireExplicit()
                                )
                        ).collect(toCollection(() -> new TreeSet<>(Comparator.comparing(EncassArgument::getName)))
                )
        );

        encassBean.setResults(
                encassEntity.getResults()
                        .stream()
                        .map(
                                encassParam -> new EncassResult(
                                        encassParam.getName(),
                                        encassParam.getType()
                                )
                        ).collect(toCollection(() -> new TreeSet<>(Comparator.comparing(EncassResult::getName)))
                )
        );
        return encassBean;
    }
}
