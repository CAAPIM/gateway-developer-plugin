/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.IdValidator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.string.CharacterBlacklistUtil;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.insertPrefixToEnvironmentVariable;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.IterableUtils.first;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Singleton
public class PolicyEntityBuilder implements EntityBuilder {
    private static final Logger LOGGER = Logger.getLogger(PolicyEntityBuilder.class.getName());
    static final String STRING_VALUE = "stringValue";
    private static final String TYPE = "type";
    private static final Integer ORDER = 200;
    public static final String POLICY = "policy";
    public static final String ZERO_GUID = "00000000-0000-0000-0000-000000000000";

    private final DocumentTools documentTools;
    private final IdGenerator idGenerator;
    private final PolicyXMLBuilder policyXMLBuilder;

    @Inject
    PolicyEntityBuilder(DocumentTools documentTools, IdGenerator idGenerator, PolicyXMLBuilder policyXMLBuilder) {
        this.documentTools = documentTools;
        this.idGenerator = idGenerator;
        this.policyXMLBuilder = policyXMLBuilder;
    }

    public List<Entity> buildEntities(Map<String, ?> policyMap, AnnotatedBundle annotatedBundle, Bundle bundle, BundleType bundleType, Document document) {
        // no policy has to be added to environment bundle
        if (bundleType == ENVIRONMENT) {
            return emptyList();
        }
        AnnotatedEntity annotatedEntity = annotatedBundle != null ? annotatedBundle.getAnnotatedEntity() : null;
        policyMap.values().forEach(policy -> {
            Policy policyEntity = (Policy) policy;
            if (annotatedEntity != null) {
                AnnotatedEntity annotatedPolicyEntity = policyEntity.getAnnotatedEntity();
                if (annotatedEntity.isShared()) {
                    if (annotatedPolicyEntity != null) {
                        if (annotatedPolicyEntity.getId() != null) {
                            if (IdValidator.isValidGoid(annotatedPolicyEntity.getId())) {
                                policyEntity.setId(annotatedPolicyEntity.getId());
                            } else {
                                LOGGER.log(Level.WARNING, "ignoring given invalid goid {0} for entity {1}", new String[]{annotatedPolicyEntity.getId(), policyEntity.getName()});
                            }
                        }
                        if (annotatedPolicyEntity.getGuid() != null) {
                            if (IdValidator.isValidGuid(annotatedPolicyEntity.getGuid())) {
                                policyEntity.setGuid(annotatedPolicyEntity.getGuid());
                            } else {
                                LOGGER.log(Level.WARNING, "ignoring given invalid guid {0} for entity {1}", new String[]{annotatedPolicyEntity.getId(), policyEntity.getName()});
                            }
                        }
                    }
                } else {
                    policyEntity.setId(idGenerator.generate());
                    policyEntity.setGuid(idGenerator.generateGuid());
                }
            }
        });
        policyMap.values().forEach(policy -> preparePolicy((Policy) policy, bundle, annotatedBundle));

        List<Policy> orderedPolicies = new LinkedList<>();
        policyMap.forEach((path, policy) -> maybeAddPolicy(bundle, (Policy) policy, orderedPolicies, new HashSet<Policy>()));

        return orderedPolicies.stream().map(policy -> buildPolicyEntity(policy, annotatedBundle, bundle, document)).collect(toList());
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        if (bundle instanceof AnnotatedBundle) {
            AnnotatedBundle annotatedBundle = (AnnotatedBundle) bundle;
            Map<String, Policy> map = Optional.ofNullable(bundle.getPolicies()).orElse(Collections.emptyMap());
            return buildEntities(map, annotatedBundle, annotatedBundle.getFullBundle(), bundleType, document);
        } else {
            return buildEntities(bundle.getPolicies(), null, bundle, bundleType, document);
        }
    }

    @NotNull
    @Override
    public Integer getOrder() {
        return ORDER;
    }

    @VisibleForTesting
    static void maybeAddPolicy(Bundle bundle, Policy policy, List<Policy> orderedPolicies, Set<Policy> seenPolicies) {
        if (orderedPolicies.contains(policy) || bundle.getServices().get(FilenameUtils.removeExtension(policy.getPath())) != null) {
            //This is a service policy it should have already be handled by the service entity builder OR This policy has already been added to the policy list
            return;
        }
        if (seenPolicies.contains(policy)) {
            throw new EntityBuilderException("Detected Policy Include cycle containing policies: " + seenPolicies.stream().map(Policy::getPath).collect(Collectors.joining(",")));
        }
        seenPolicies.add(policy);
        policy.getDependencies().forEach(dependency -> maybeAddPolicy(bundle, dependency, orderedPolicies, seenPolicies));
        seenPolicies.remove(policy);
        orderedPolicies.add(policy);
    }

    private void preparePolicy(Policy policy, Bundle bundle, AnnotatedBundle annotatedBundle) {
        Document policyDocument = loadPolicyDocument(policy);
        String policyName = policy.getName();
        AnnotatedEntity annotatedEntity = annotatedBundle != null ? annotatedBundle.getAnnotatedEntity() : null;
        if (annotatedEntity != null) {
            policyName = annotatedBundle.applyUniqueName(policy.getName(), BundleType.DEPLOYMENT, annotatedEntity.isShared());
        }

        PolicyBuilderContext policyBuilderContext = new PolicyBuilderContext(policyName, policyDocument, bundle, idGenerator);
        policyBuilderContext.withPolicy(policy).withAnnotatedBundle(annotatedBundle);
        policyXMLBuilder.buildPolicyXML(policyBuilderContext);
        policy.setPolicyDocument(policyDocument.getDocumentElement());
    }

    private Document loadPolicyDocument(Policy policy) {
        Document policyDocument;
        try {
            policyDocument = stringToXMLDocument(documentTools, policy.getPolicyXML());
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not load policy: " + e.getMessage(), e);
        }
        return policyDocument;
    }

    public static void resolvePossibleMissingEncapsulatedAssertionDependencies(Bundle bundle, Element encapsulatedAssertionElement) {
        Element guidElement = getSingleChildElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID, true);
        if (guidElement == null) {
            return;
        }

        Element nameElement = getSingleChildElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME, true);
        if (nameElement == null) {
            return;
        }

        String guid = guidElement.getAttribute(STRING_VALUE);
        if (!ZERO_GUID.equals(guid)) {
            return;
        }

        String name = nameElement.getAttribute(STRING_VALUE);
        List<Encass> encasses = bundle.getEncasses().values().stream().filter(e -> name.equals(e.getName())).collect(toList());
        if (encasses.isEmpty()) {
            return;
        }
        if (encasses.size() > 1) {
            throw new EntityBuilderException("Found multiple encasses in dependency bundles with name: " + name);
        }
        Encass encass = first(encasses);
        guidElement.setAttribute(STRING_VALUE, encass.getGuid());
    }

    @VisibleForTesting
    Entity buildPolicyEntity(Policy policy, AnnotatedBundle annotatedBundle, Bundle bundle, Document document) {
        String policyName = policy.getName();
        String policyNameWithPath = policy.getPath();
        policyNameWithPath = CharacterBlacklistUtil.decodePath(policyNameWithPath);
        AnnotatedEntity annotatedEntity = annotatedBundle != null ? annotatedBundle.getAnnotatedEntity() : null;
        boolean isRedeployableBundle = false;
        boolean isShared = false;
        if (annotatedEntity != null) {
            isRedeployableBundle = annotatedEntity.isRedeployable();
            isShared = annotatedEntity.isShared();
        }
        if (annotatedBundle != null) {
            policyName = annotatedBundle.applyUniqueName(policyName, BundleType.DEPLOYMENT, isShared);
            policyNameWithPath = PathUtils.extractPath(policyNameWithPath) + policyName;
        }

        PolicyTags policyTags = getPolicyTags(policy, bundle);

        Element policyDetailElement = createElementWithAttributesAndChildren(
                document,
                POLICY_DETAIL,
                ImmutableMap.of(ATTRIBUTE_ID, policy.getId(), ATTRIBUTE_GUID, policy.getGuid(), ATTRIBUTE_FOLDER_ID, policy.getParentFolder().getId()),
                createElementWithTextContent(document, NAME, policyName),
                createElementWithTextContent(document, POLICY_TYPE, policyTags == null ? PolicyType.INCLUDE.getType() : policyTags.type.getType())
        );

        if (policyTags != null) {
            Builder<String, Object> builder = ImmutableMap.<String, Object>builder().put(PROPERTY_TAG, policyTags.tag);
            if (policyTags.subtag != null) {
                builder.put(PROPERTY_SUBTAG, policyTags.subtag);
            }
            buildAndAppendPropertiesElement(
                    builder.build(),
                    document,
                    policyDetailElement
            );
        }

        Element policyElement = createElementWithAttributes(
                document,
                BundleElementNames.POLICY,
                ImmutableMap.of(ATTRIBUTE_ID, policy.getId(), ATTRIBUTE_GUID, policy.getGuid())
        );
        policyElement.appendChild(policyDetailElement);

        Element resourcesElement = document.createElement(RESOURCES);
        Element resourceSetElement = createElementWithAttribute(document, RESOURCE_SET, PROPERTY_TAG, POLICY);
        Element resourceElement = createElementWithAttribute(document, RESOURCE, TYPE, POLICY);
        resourceElement.setTextContent(documentTools.elementToString(policy.getPolicyDocument()));

        resourceSetElement.appendChild(resourceElement);
        resourcesElement.appendChild(resourceSetElement);
        policyElement.appendChild(resourcesElement);
        Entity entity = EntityBuilderHelper.getEntityWithPathMapping(EntityTypes.POLICY_TYPE,
                policy.getPath(), policyNameWithPath, policy.getId(), policyElement, policy.isHasRouting(), policy);
        if (isRedeployableBundle || !isShared) {
            entity.setMappingAction(MappingActions.NEW_OR_UPDATE);
        } else {
            entity.setMappingAction(MappingActions.NEW_OR_EXISTING);
        }
        return entity;
    }

    private PolicyTags getPolicyTags(Policy policy, Bundle bundle) {
        // Global and Internal policies have only the tag and can be treated as is
        if (Stream.of(PolicyType.GLOBAL, PolicyType.INTERNAL).collect(toList()).contains(policy.getPolicyType()) && isNotEmpty(policy.getTag())) {
            return new PolicyTags(policy.getPolicyType(), policy.getTag(), null);
        }

        final AtomicReference<PolicyTags> policyTags = new AtomicReference<>();
        for (PolicyBackedService pbs : bundle.getPolicyBackedServices().values()) {
            pbs.getOperations().stream().filter(o -> o.getPolicy().equals(policy.getPath())).forEach(o -> {
                if (!policyTags.compareAndSet(null, new PolicyTags(PolicyType.SERVICE_OPERATION, pbs.getInterfaceName(), o.getOperationName()))) {
                    throw new EntityBuilderException("Found multiple policy backed service operations for policy: " + policy.getPath());
                }
            });
        }
        return policyTags.get();
    }

    private class PolicyTags {
        private final PolicyType type;
        private final String tag;
        private final String subtag;

        private PolicyTags(PolicyType type, String tag, String subtag) {
            this.type = type;
            this.tag = tag;
            this.subtag = subtag;
        }
    }
}
