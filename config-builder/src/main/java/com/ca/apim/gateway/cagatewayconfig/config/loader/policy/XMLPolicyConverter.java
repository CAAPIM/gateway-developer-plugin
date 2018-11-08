/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Singleton
public class XMLPolicyConverter implements PolicyConverter {
    public static final String EXTENSION = ".xml";

    private final DocumentFileUtils documentFileUtils;

    @Inject
    public XMLPolicyConverter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public String getPolicyTypeExtension() {
        return EXTENSION;
    }

    @Override
    public String getPolicyXML(Policy policy, String policyString) {
        return policyString;
    }

    @Override
    public boolean canConvertible(String name, Element policy) {
        // always return false. We don't want to automatically pick this converter
        return false;
    }

    @Override
    @SuppressWarnings("squid:S2095")
    // Should not close the input stream since we are returning it. It should be auto closed by the caller
    public InputStream convertFromPolicy(Element policy) {
        final PipedOutputStream out = new PipedOutputStream();
        new Thread(() -> {
            documentFileUtils.printXML(policy, out, false);
            try {
                out.close();
            } catch (IOException e) {
                throw new PolicyConverterException(e);
            }
        }).start();
        try {
            return new PipedInputStream(out);
        } catch (IOException e) {
            throw new PolicyConverterException(e);
        }
    }
}
