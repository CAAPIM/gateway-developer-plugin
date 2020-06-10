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
    static final String BOOLEAN_VALUE = "booleanValue";
    static final String POLICY_PATH = "policyPath";
    static final String ENCASS_NAME = "encassName";
    static final String ENV_PARAM_NAME = "ENV_PARAM_NAME";
    private static final String TYPE = "type";
    private static final Integer ORDER = 200;
    public static final String POLICY = "policy";
    public static final String ZERO_GUID = "00000000-0000-0000-0000-000000000000";

    private final DocumentTools documentTools;
    private final IdGenerator idGenerator;

    @Inject
    PolicyEntityBuilder(DocumentTools documentTools, IdGenerator idGenerator) {
        this.documentTools = documentTools;
        this.idGenerator = idGenerator;
    }

    private boolean isAnnotatedEntity(Policy policy, AnnotatedEntity annotatedEntity) {
        return annotatedEntity != null && annotatedEntity.getEntityName().equals(policy.getName()) &&
                EntityTypes.POLICY_TYPE.equals(annotatedEntity.getEntityType());
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
                if (annotatedEntity.isReusable() || isAnnotatedEntity(policyEntity, annotatedEntity)) {
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
        Element policyElement = policyDocument.getDocumentElement();
        final String policyName;
        AnnotatedEntity annotatedEntity = annotatedBundle != null ? annotatedBundle.getAnnotatedEntity() : null;
        if (annotatedEntity != null) {
            if (annotatedEntity.isReusable() || isAnnotatedEntity(policy, annotatedEntity)) {
                policyName = policy.getName();
            } else {
                policyName = annotatedBundle.getUniquePrefix() + policy.getName();
            }
        } else {
            policyName = policy.getName();
        }

        prepareAssertion(policyElement, PolicyXMLElements.INCLUDE, assertionElement -> prepareIncludeAssertion(policy, bundle, assertionElement, annotatedBundle));
        prepareAssertion(policyElement, ENCAPSULATED, assertionElement -> prepareEncapsulatedAssertion(policy, bundle, policyDocument, assertionElement, annotatedBundle));
        prepareAssertion(policyElement, SET_VARIABLE, assertionElement -> prepareSetVariableAssertion(policyName, policyDocument, assertionElement));
        prepareAssertion(policyElement, HARDCODED_RESPONSE, assertionElement -> prepareHardcodedResponseAssertion(policyDocument, assertionElement));

        policy.setPolicyDocument(policyElement);
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

    @VisibleForTesting
    static void prepareHardcodedResponseAssertion(Document policyDocument, Element assertionElement) {
        prepareBase64Element(policyDocument, assertionElement, RESPONSE_BODY, BASE_64_RESPONSE_BODY);
    }

    @VisibleForTesting
    static void prepareSetVariableAssertion(String policyName, Document policyDocument, Element assertionElement) {
        Element nameElement;
        try {
            nameElement = getSingleElement(assertionElement, VARIABLE_TO_SET);
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not find VariableToSet element in a SetVariable Assertion.");
        }

        String variableName = nameElement.getAttribute(STRING_VALUE);
        if (variableName.startsWith(PREFIX_ENV)) {
            assertionElement.insertBefore(
                    createElementWithAttribute(policyDocument, BASE_64_EXPRESSION, ENV_PARAM_NAME, insertPrefixToEnvironmentVariable(variableName, policyName)),
                    assertionElement.getFirstChild()
            );
        } else {
            prepareBase64Element(policyDocument, assertionElement, EXPRESSION, BASE_64_EXPRESSION);
        }
    }

    private static void prepareBase64Element(Document policyDocument, Element assertionElement, String elementName, String base64ElementName) {
        Element element;
        try {
            element = getSingleElement(assertionElement, elementName);
        } catch (DocumentParseException e) {
            LOGGER.log(Level.FINE, "Did not find '" + elementName + "' tag for SetVariableAssertion. Not generating Base64ed version");
            return;
        }

        String expression = getCDataOrText(element);
        String encoded = Base64.getEncoder().encodeToString(expression.getBytes());
        assertionElement.insertBefore(createElementWithAttribute(policyDocument, base64ElementName, STRING_VALUE, encoded), element);
        assertionElement.removeChild(element);
    }

    private static String getCDataOrText(Element element) {
        StringBuilder content = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (Node child : nodeList(children)) {
            short nodeType = child.getNodeType();
            if (nodeType == Node.TEXT_NODE) {
                content.append(child.getTextContent());
            } else if (nodeType == Node.CDATA_SECTION_NODE) {
                content.append(((CDATASection) child).getData());
                break;
            } else {
                throw new EntityBuilderException("Unexpected set variable assertion expression node type: " + child.getNodeName());
            }
        }
        return StringEscapeUtils.unescapeXml(content.toString());
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
    void prepareEncapsulatedAssertion(Policy policy, Bundle bundle, Document policyDocument, Element encapsulatedAssertionElement, AnnotatedBundle annotatedBundle) {
        if (encapsulatedAssertionElement.hasAttribute(ENCASS_NAME)) {
            final String encassName = encapsulatedAssertionElement.getAttribute(ENCASS_NAME);
            Encass encass = getEncass(bundle, encassName, annotatedBundle);
            final String guid = findEncassReferencedGuid(policy, encass, encapsulatedAssertionElement, encassName);
            updateEncapsulatedAssertion(policyDocument, encapsulatedAssertionElement, encass, encassName, guid, annotatedBundle);
        } else if (!isNoOpIfConfigMissing(encapsulatedAssertionElement)) {
            Element guidElement = getSingleChildElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID, true);
            Element nameElement = getSingleChildElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME, true);
            throw new EntityBuilderException("No encassName specified for encass in policy: '" + policy.getPath() + "' GUID: '" + (guidElement != null ? guidElement.getAttribute(STRING_VALUE) : null) + "' Name: '" + (nameElement != null ? nameElement.getAttribute(STRING_VALUE) : null) + "'");
        } else {
            LOGGER.log(Level.FINE, "No encassName specified for encass in policy: \"{0}\". Since NoOp is true, this will be treated as a No Op.", policy.getPath());
        }
    }

    private Encass getEncass(Bundle bundle, String name, AnnotatedBundle annotatedBundle) {
        LOGGER.log(Level.FINE, "Looking for referenced encass: {0}", name);
        final AtomicReference<Encass> referenceEncass = new AtomicReference<>(bundle.getEncasses().get(name));

        if (referenceEncass.get() == null) {
            //check the dependency in the given dependent bundle
            bundle.getDependencies().forEach(b -> {
                Encass encassDependency = b.getEncasses().get(name);
                if (encassDependency != null) {
                    if (!referenceEncass.compareAndSet(null, encassDependency)) {
                        throw new EntityBuilderException("Found multiple encasses in dependency bundles with name: " + name);
                    }
                    //add dependent bundle if bundle type is not null
                    DependentBundle dependentBundle = b.getDependentBundle();
                    if (dependentBundle != null && dependentBundle.getType() != null){
                        if(annotatedBundle != null) {
                            annotatedBundle.addDependentBundle(dependentBundle);
                        } else {
                            bundle.addDependentBundle(dependentBundle);
                        }
                    }
                }
            });

            //if encass is not found in any of the bundles, check this entity in missing entities
            if (referenceEncass.get() == null) {
                final MissingGatewayEntity missingEntity = bundle.getMissingEntities().get(name);
                if (missingEntity != null && missingEntity.isExcluded()) {
                    LOGGER.log(Level.WARNING, "Resolving the referenced encass {0} as known excluded entity with guid: {1}",
                            new Object[] {name, missingEntity.getGuid()});
                    referenceEncass.set(getExcludedEncass(missingEntity.getGuid()));
                }
            }
        }
        return referenceEncass.get();
    }

    private Encass getExcludedEncass(final String guid){
        Encass missingEncass = new Encass();
        missingEncass.setGuid(guid);
        Set<Annotation> annotations = new HashSet<>();
        annotations.add(AnnotableEntity.EXCLUDE_ANNOTATION);
        missingEncass.setAnnotations(annotations);
        return missingEncass;
    }

    private static String findEncassReferencedGuid(Policy policy, Encass encass, Element encapsulatedAssertionElement, String name) {
        final String guid;
        if (encass == null) {
            if (isNoOpIfConfigMissing(encapsulatedAssertionElement)) {
                LOGGER.log(Level.FINE, "Could not find referenced encass with name: \"{0}\". In policy: \"{1}\". Since NoOp is true, this will be treated as a No Op.", new String[]{name, policy.getPath()});
                guid = ZERO_GUID;
            } else {
                throw new EntityBuilderException("Could not find referenced encass with name: '" + name + "'. In policy: " + policy.getPath());
            }
        } else {
            guid = encass.getGuid();
        }
        return guid;
    }

    private void updateEncapsulatedAssertion(Document policyDocument, Node encapsulatedAssertionElement, Encass encass, String name, String guid, AnnotatedBundle annotatedBundle) {
        String encassName = name;
        String encassGuid = guid;
        AnnotatedEntity annotatedEntity = annotatedBundle != null ? annotatedBundle.getAnnotatedEntity() : null;
        if (encass != null && !encass.isExcluded() && annotatedEntity != null) {
            AnnotatedEntity annotatedEncassEntity = encass.getAnnotatedEntity();
            if (annotatedEntity.isReusable()) {
                if(annotatedEncassEntity != null){
                    if (annotatedEncassEntity.getGuid() != null) {
                        if (IdValidator.isValidGuid(annotatedEncassEntity.getGuid())) {
                            encassGuid = annotatedEncassEntity.getGuid();
                            encass.setGuid(encassGuid);
                        } else {
                            LOGGER.log(Level.WARNING, "ignoring given invalid guid {0} for entity {1}", new String[]{annotatedEncassEntity.getGuid(), name});
                        }
                    }
                    if (annotatedEncassEntity.getId() != null) {
                        if (IdValidator.isValidGoid(annotatedEncassEntity.getId())) {
                            encass.setId(annotatedEncassEntity.getId());
                        } else {
                            LOGGER.log(Level.WARNING, "ignoring given invalid goid {0} for entity {1}", new String[]{annotatedEncassEntity.getId(), name});
                        }
                    }
                }
            } else {
                encassGuid = idGenerator.generateGuid();
                encass.setGuid(encassGuid);
                encass.setId(idGenerator.generate());
                encassName = annotatedBundle.getUniquePrefix() + encassName;
            }
        }
        Element encapsulatedAssertionConfigNameElement = createElementWithAttribute(
                policyDocument,
                ENCAPSULATED_ASSERTION_CONFIG_NAME,
                STRING_VALUE,
                encassName
        );
        Node firstChild = encapsulatedAssertionElement.getFirstChild();
        if (firstChild != null) {
            encapsulatedAssertionElement.insertBefore(encapsulatedAssertionConfigNameElement, firstChild);
        } else {
            encapsulatedAssertionElement.appendChild(encapsulatedAssertionConfigNameElement);
        }

        Element encapsulatedAssertionConfigGuidElement = createElementWithAttribute(
                policyDocument,
                ENCAPSULATED_ASSERTION_CONFIG_GUID,
                STRING_VALUE,
                encassGuid
        );
        encapsulatedAssertionElement.insertBefore(encapsulatedAssertionConfigGuidElement, encapsulatedAssertionElement.getFirstChild());

        ((Element) encapsulatedAssertionElement).removeAttribute(ENCASS_NAME);
    }

    private static boolean isNoOpIfConfigMissing(Element encapsulatedAssertionElement) {
        Element noOpElement = getSingleChildElement(encapsulatedAssertionElement, NO_OP_IF_CONFIG_MISSING, true);
        if (noOpElement == null) {
            return false;
        }
        final String isNoOp = noOpElement.getAttribute(BOOLEAN_VALUE);
        return Boolean.valueOf(isNoOp);
    }

    @VisibleForTesting
    static void prepareIncludeAssertion(Policy policy, Bundle bundle, Element includeAssertionElement, AnnotatedBundle annotatedBundle) {
        Element policyGuidElement;
        try {
            policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not find PolicyGuid element in Include Assertion", e);
        }
        final String policyPath = policyGuidElement.getAttribute(POLICY_PATH);
        LOGGER.log(Level.FINE, "Looking for referenced policy include: {0}", policyPath);

        final AtomicReference<Policy> includedPolicy = new AtomicReference<>(bundle.getPolicies().get(policyPath));
        if (includedPolicy.get() != null) {
            policy.getDependencies().add(includedPolicy.get());
        } else {
            //check policy dependency in bundle dependencies
            bundle.getDependencies().forEach(b -> {
                Policy policyFromDependencies = Optional.ofNullable(b.getPolicies().get(policyPath)).orElse(b.getPolicies().get(PathUtils.extractName(policyPath)));
                if (policyFromDependencies != null) {
                    if (!includedPolicy.compareAndSet(null, policyFromDependencies)) {
                        throw new EntityBuilderException("Found multiple policies in dependency bundles with policy path: " + policyPath);
                    }
                    //add dependent bundle if bundle type is not null
                    DependentBundle dependentBundle = b.getDependentBundle();
                    if (dependentBundle != null && dependentBundle.getType() != null){
                        if(annotatedBundle != null) {
                            annotatedBundle.addDependentBundle(dependentBundle);
                        } else {
                            bundle.addDependentBundle(dependentBundle);
                        }
                    }
                }
            });

            //if policy is not found in any bundles then check if it missing and excluded
            final MissingGatewayEntity missingEntity = bundle.getMissingEntities().get(policyPath);
            if (missingEntity != null && missingEntity.isExcluded()) {
                LOGGER.log(Level.WARNING, "Resolving the referenced policy {0} as known excluded entity with guid: {1}",
                        new Object[] {policyPath, missingEntity.getGuid()});
                includedPolicy.set(new Policy());
                includedPolicy.get().setGuid(missingEntity.getGuid());
            }

        }

        if (includedPolicy.get() == null) {
            throw new EntityBuilderException("Could not find referenced policy include with path: " + policyPath);
        }
        policyGuidElement.setAttribute(STRING_VALUE, includedPolicy.get().getGuid());
        policyGuidElement.removeAttribute(POLICY_PATH);
    }

    private void prepareAssertion(Element policyElement, String assertionTag, Consumer<Element> prepareAssertionMethod) {
        NodeList assertionReferences = policyElement.getElementsByTagName(assertionTag);
        for (int i = 0; i < assertionReferences.getLength(); i++) {
            Node assertionElement = assertionReferences.item(i);
            if (!(assertionElement instanceof Element)) {
                throw new EntityBuilderException("Unexpected assertion node type: " + assertionElement.getNodeName());
            }
            prepareAssertionMethod.accept((Element) assertionElement);
        }
    }

    @VisibleForTesting
    Entity buildPolicyEntity(Policy policy, AnnotatedBundle annotatedBundle, Bundle bundle, Document document) {
        String policyName = policy.getName();
        String policyNameWithPath = policy.getPath();
        AnnotatedEntity annotatedEntity = annotatedBundle != null ? annotatedBundle.getAnnotatedEntity() : null;
        boolean isRedeployableBundle = false;
        boolean isReusable = false;
        if(annotatedEntity != null){
            isRedeployableBundle = annotatedEntity.isRedeployable();
            isReusable = annotatedEntity.isReusable();
        }
        if (annotatedBundle != null) {
            if (!isReusable && !isAnnotatedEntity(policy, annotatedEntity)) {
                policyName = annotatedBundle.getUniquePrefix() + policyName;
                policyNameWithPath = PathUtils.extractPath(policy.getPath()) + policyName;
            }
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
        if (isRedeployableBundle || !(isReusable || isAnnotatedEntity(policy, annotatedEntity))) {
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
