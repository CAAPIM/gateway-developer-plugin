/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

/**
 * AnnotationDeserializer deserializes the "annotations" tag in the entity elements. Annotation on entities are
 * supported in 2 ways:
 * 1) Array of objects containing "type" and the other optional fields like "name", "description" and "tags"
 * field. For example,
 * annotations:
 * - type: "@bundle"
 *   name: "encass-example"
 *   description: "description for encass-example"
 *   tags:
 *   - "anytag"
 *   - "sometag"
 *
 * 2) Array of Strings containing only "type". For example,
 * annotations:
 * - "@bundle"
 * - "@reusable"
 *
 * This deserialization implementation takes care of both these types and deserializes the input into
 * {@link java.util.Set} of {@link com.ca.apim.gateway.cagatewayconfig.beans.Annotation}
 */
package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;

public class AnnotationDeserializer extends JsonDeserializer<Set<Annotation>> {

    /**
     * Deserializes the input into set of annotations.
     * @param p Parser
     * @param ctxt Context
     * @return Set of Annotations parsed from the input
     * @throws IOException if parser failed
     */
    @Override
    public Set<Annotation> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec oc = p.getCodec();
        JsonNode node = oc.readTree(p);

        if (node.isArray()) {
            final Set<Annotation> annotations = new HashSet<>();
            node.elements().forEachRemaining(ele -> {
                if (ele.isObject() && ele.hasNonNull("type")) {
                    final Annotation annotation = new Annotation(ele.get("type").asText());
                    if (ele.hasNonNull("name")) {
                        annotation.setName(ele.get("name").asText());
                    }
                    if (ele.hasNonNull("description")) {
                        annotation.setDescription(ele.get("description").asText());
                    }
                    if (ele.hasNonNull("tags")) {
                        final List<String> tags = new ArrayList<>();
                        ele.get("tags").elements().forEachRemaining(e -> tags.add(e.textValue()));
                        annotation.setTags(tags);
                    }
                    annotations.add(annotation);
                } else if (ele.isTextual()) {
                    annotations.add(new Annotation(ele.asText()));
                }
            });
            return annotations;
        }
        return Collections.emptySet();
    }
}
