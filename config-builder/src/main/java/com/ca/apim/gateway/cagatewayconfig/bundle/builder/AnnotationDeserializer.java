/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class AnnotationDeserializer extends JsonDeserializer<Set<Annotation>> {
    private static final Logger LOGGER = Logger.getLogger(AnnotationDeserializer.class.getName());

    /**
     * Deserializes the input into set of annotations.
     * @param parser JSON/YAML Parser
     * @param context Context
     * @return Set of Annotations parsed from the input
     * @throws IOException if parser failed
     */
    @Override
    public Set<Annotation> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        if (node.isArray()) {
            final Set<Annotation> annotations = new HashSet<>();
            node.elements().forEachRemaining(ele -> {
                if (ele.isObject() && ele.hasNonNull("type")) {
                    final Annotation annotation = new Annotation(ele.get("type").asText());
                    if (AnnotationConstants.SUPPORTED_ANNOTATION_TYPES.contains(annotation.getType()) && !annotations.contains(annotation)) {
                        if (ele.hasNonNull("name")) {
                            annotation.setName(ele.get("name").asText());
                        }
                        if (ele.hasNonNull("id")) {
                            annotation.setId(ele.get("id").asText());
                        }
                        if (ele.hasNonNull("guid")) {
                            annotation.setGuid(ele.get("guid").asText());
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
                    } else {
                        LOGGER.log(Level.WARNING, "Annotations contain unsupported or duplicate annotation: {0}", annotation.getType());
                    }

                } else if (ele.isTextual()) {
                    final Annotation annotation = new Annotation(ele.asText());
                    if (AnnotationConstants.SUPPORTED_ANNOTATION_TYPES.contains(annotation.getType()) && !annotations.contains(annotation)) {
                        annotations.add(annotation);
                    } else {
                        LOGGER.log(Level.WARNING, "Annotations contain unsupported or duplicate annotation: {0}", annotation.getType());
                    }
                }
            });
            return annotations;
        }
        return Collections.emptySet();
    }
}
