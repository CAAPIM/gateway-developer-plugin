/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.stringToXMLDocument;
import static java.util.Collections.unmodifiableSet;

@Singleton
public class BundleEntityBuilder {
    private static final Logger LOGGER = Logger.getLogger(BundleEntityBuilder.class.getName());
    private final Set<EntityBuilder> entityBuilders;
    private final BundleDocumentBuilder bundleDocumentBuilder;
    private final BundleMetadataBuilder bundleMetadataBuilder;
    private final DocumentTools documentTools;
    private final EntityTypeRegistry entityTypeRegistry;

    @Inject
    BundleEntityBuilder(final Set<EntityBuilder> entityBuilders, final BundleDocumentBuilder bundleDocumentBuilder,
                        final BundleMetadataBuilder bundleMetadataBuilder, final DocumentTools documentTools, final EntityTypeRegistry entityTypeRegistry) {
        // treeset is needed here to sort the builders in the proper order to get a correct bundle builded
        // Ordering is necessary for the bundle, for the gateway to load it properly.
        this.entityBuilders = unmodifiableSet(new TreeSet<>(entityBuilders));
        this.bundleDocumentBuilder = bundleDocumentBuilder;
        this.bundleMetadataBuilder = bundleMetadataBuilder;
        this.documentTools = documentTools;
        this.entityTypeRegistry = entityTypeRegistry;
    }

    public Map<String, Pair<Element, BundleMetadata>> build(Bundle bundle, EntityBuilder.BundleType bundleType,
                                               Document document, String projectName,
                                               String projectGroupName, String projectVersion) {

        Map<String, Pair<Element, BundleMetadata>> artifacts = buildAnnotatedEntities(bundleType, bundle, document, projectName,
                projectGroupName, projectVersion);
        if (artifacts.isEmpty()) {
            List<Entity> entities = new ArrayList<>();
            entityBuilders.forEach(builder -> entities.addAll(builder.build(bundle, bundleType, document)));
            artifacts.put(StringUtils.isBlank(projectVersion) ? projectName : projectName + "-" + projectVersion,
                    ImmutablePair.of(bundleDocumentBuilder.build(document, entities), null));
        }
        return artifacts;
    }

    private Map<String, Pair<Element, BundleMetadata>> buildAnnotatedEntities(EntityBuilder.BundleType bundleType, Bundle bundle,
                                                                              Document document, String projectName,
                                                                              String projectGroupName,
                                                                              String projectVersion) {
        final Map<String, Pair<Element, BundleMetadata>> annotatedElements = new LinkedHashMap<>();

        // Filter the bundle to export only annotated entities
        // TODO : Enhance this logic to support services and policies
        bundle.getEntities(Encass.class).values().stream()
                .filter(Encass::hasAnnotated)
                .map(encass -> createAnnotatedEntity(encass, projectName, projectVersion))
                .forEach(annotatedEntity -> {
                    if (annotatedEntity.isBundleTypeEnabled()) {
                        List<Entity> entities = new ArrayList<>();
                        Map<String, GatewayEntity> entityMap = getEntityDependencies(annotatedEntity.getPolicyName(), bundle);
                        entityMap.put(annotatedEntity.getEntityName(), annotatedEntity.getEntity());
                        entityBuilders.forEach(builder -> entities.addAll(builder.build(entityMap, bundle, bundleType, document)));
                        List<Entity> bundleEntities = renameNonReusableEntities(entities, bundle, annotatedEntity, projectGroupName, projectVersion);
                        // Create bundle
                        final Element annotatedBundle = bundleDocumentBuilder.build(document, bundleEntities);
                        final BundleMetadata bundleMetadata = bundleMetadataBuilder.build(annotatedEntity, bundleEntities
                                , projectGroupName, projectVersion);
                        annotatedElements.put(annotatedEntity.getBundleName(), ImmutablePair.of(annotatedBundle,
                                bundleMetadata));
                    }
                });

        return annotatedElements;
    }

    /**
     * Creates AnnotatedEntity object by scanning all the annotations and gathering all the information required to
     * generate the bundle and its metadata.
     *
     * @param encass Encapsulated assertion
     * @param projectName Project name
     * @param projectVersion Project version
     * @return AnnotatedEntity
     */
    public AnnotatedEntity<Encass> createAnnotatedEntity(final Encass encass, final String projectName,
                                                         final String projectVersion) {
        AnnotatedEntity<Encass> annotatedEntity = new AnnotatedEntity<>(encass);
        encass.getAnnotations().forEach(annotation -> {
            switch (annotation.getType()) {
                case ANNOTATION_TYPE_BUNDLE:
                    String annotatedBundleName = annotation.getName();
                    if (StringUtils.isBlank(annotatedBundleName)) {
                        annotatedBundleName = projectName + "-" + encass.getName();
                    }

                    String description = annotation.getDescription();
                    if (StringUtils.isBlank(description)) {
                        description = encass.getProperties().getOrDefault("description", "").toString();
                    }
                    annotatedEntity.setTags(annotation.getTags());
                    annotatedEntity.setBundleType(true);
                    annotatedEntity.setEntityName(encass.getName());
                    annotatedEntity.setDescription(description);
                    annotatedEntity.setEntityType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
                    annotatedEntity.setBundleName(annotatedBundleName + "-" + projectVersion);
                    annotatedEntity.setPolicyName(encass.getPolicy());
                    annotatedEntity.setUniquePrefix(projectName + "-encass-" + PathUtils.extractName(encass.getName()) + "-");
                    annotatedEntity.setUniqueSuffix("-" + projectVersion);
                    break;
                case ANNOTATION_TYPE_REUSABLE:
                case ANNOTATION_TYPE_REUSABLE_BUNDLE:
                    annotatedEntity.setReusableType(true);
                    break;
                case ANNOTATION_TYPE_REUSABLE_ENTITY:
                    annotatedEntity.setReusableEntity(true);
                    break;
                case ANNOTATION_TYPE_REDEPLOYABLE:
                    annotatedEntity.setRedeployableType(true);
                    break;
                case ANNOTATION_TYPE_EXCLUDE:
                    annotatedEntity.setExcludeType(true);
                    break;
                default:
                    break;
            }
        });
        return annotatedEntity;
    }

    private List<Entity> renameNonReusableEntities(List<Entity> entityList, Bundle bundle, AnnotatedEntity annotatedEntity, String projectGroupName, String projectVersion) {
        List<Entity> bundleEntities = new ArrayList<>();
        final Map<String, Policy> policyMap = bundle.getPolicies();
        final Map<String, Encass> encassMap = bundle.getEncasses();
        for(Entity entity : entityList){
            if(EntityTypes.ENCAPSULATED_ASSERTION_TYPE.equals(entity.getType()) || EntityTypes.POLICY_TYPE.equals(entity.getType())) {
                Encass encassEntity = encassMap.get(entity.getName());
                Policy policyEntity = policyMap.get(entity.getName());
                Set<Annotation> annotations = policyEntity != null ? policyEntity.getAnnotations() : encassEntity.getAnnotations();
                if (annotations != null && annotations.stream().anyMatch(annotation -> ANNOTATION_TYPE_REUSABLE_ENTITY.equals(annotation.getType()))) {
                    bundleEntities.add(entity);
                } else {
                    bundleEntities.add(getUniqueEntity(entity, annotatedEntity, encassMap, projectGroupName, projectVersion));
                }

            } else {
                bundleEntities.add(entity);
            }
        }
        return bundleEntities;
    }

    private Entity getUniqueEntity(final Entity entity, final AnnotatedEntity annotatedEntity, final Map<String, Encass> encassMap, final String projectName, final String projectVersion) {
        final String nameWithPath = entity.getName();
        final String uniqueName = getUniqueName(projectName, projectVersion, annotatedEntity, nameWithPath);
        final String uniqueNameWithPath = PathUtils.extractPath(nameWithPath) + uniqueName;
        Element entityXml = null;
        if(EntityTypes.POLICY_TYPE.equals(entity.getType())){
            entityXml = getUniquePolicyXml(entity.getXml(), annotatedEntity, encassMap, projectName, projectVersion, uniqueName);
        } else if(EntityTypes.ENCAPSULATED_ASSERTION_TYPE.equals(entity.getType())){
            entityXml = (Element) entity.getXml().cloneNode(true);
            final Element nameElement = getSingleChildElement(entityXml, NAME);
            nameElement.setTextContent(uniqueName);
        }  else {
            entityXml = entity.getXml();
        }
        return EntityBuilderHelper.getEntityWithMappings(entity.getType(), uniqueNameWithPath, entity.getId(), entityXml, MappingActions.NEW_OR_UPDATE, entity.getMappingProperties());
    }

    private Element getUniquePolicyXml(final Element entityXml, final AnnotatedEntity annotatedEntity, final Map<String, Encass> encassMap, final String projectName, final String projectVersion, final String uniqueName) {
        Element policyElement = (Element) entityXml.cloneNode(true);
        final Element policyDetails = getSingleChildElement(policyElement, POLICY_DETAIL);
        Element nameElement = getSingleChildElement(policyDetails, NAME);
        nameElement.setTextContent(uniqueName);
        final Element resources = getSingleChildElement(policyElement, RESOURCES);
        final Element resourceSet = getSingleChildElement(resources, RESOURCE_SET);
        final Element resource = getSingleChildElement(resourceSet, RESOURCE);
        final String policyString = resource.getTextContent();
        Document policyDocument;
        try {
            policyDocument = stringToXMLDocument(documentTools, policyString);
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not load policy: " + e.getMessage(), e);
        }
        if (policyDocument != null) {
            Element policy = policyDocument.getDocumentElement();
            NodeList assertionReferences = policy.getElementsByTagName(PolicyXMLElements.ENCAPSULATED);
            if (assertionReferences != null && assertionReferences.getLength() > 0) {
                for (int i = 0; i < assertionReferences.getLength(); i++) {
                    Node assertionElement = assertionReferences.item(i);
                    if (!(assertionElement instanceof Element)) {
                        throw new EntityBuilderException("Unexpected assertion node type: " + assertionElement.getNodeName());
                    }
                    Element encassElement = (Element) assertionElement;
                    Element encassConfigElement = getSingleChildElement(encassElement, PolicyXMLElements.ENCAPSULATED_ASSERTION_CONFIG_NAME);
                    final Node encassNameNode = encassConfigElement.getAttributeNode(ATTRIBUTE_STRING_VALUE);
                    final String encassName = encassNameNode.getNodeValue();
                    final Encass encassEntity = encassMap.get(encassName);
                    Set<Annotation> annotations = encassEntity != null ? encassEntity.getAnnotations() : null;
                    if (annotations == null || ! (annotations.stream().anyMatch(annotation -> ANNOTATION_TYPE_REUSABLE_ENTITY.equals(annotation.getType())))) {
                        encassNameNode.setNodeValue(getUniqueName(projectName, projectVersion, annotatedEntity, encassName));
                    }
                }
                resource.setTextContent(documentTools.elementToString(policy));
            }
        }
        return policyElement;
    }

    private String getUniqueName(final String projectName, final String projectVersion, final AnnotatedEntity annotatedEntity, final String nameWithPath) {
        StringBuilder uniqueName = new StringBuilder(projectName);
        uniqueName.append("-");
        if (EntityTypes.ENCAPSULATED_ASSERTION_TYPE.equals(annotatedEntity.getEntityType())) {
            uniqueName.append("encass");
        } else if (EntityTypes.SERVICE_TYPE.equals(annotatedEntity.getEntityType())) {
            uniqueName.append("service");
        } else {
            uniqueName.append("policy");
        }

        uniqueName.append("-");
        uniqueName.append(PathUtils.extractName(annotatedEntity.getEntityName()));
        uniqueName.append("-");
        uniqueName.append(PathUtils.extractName(nameWithPath));
        uniqueName.append("-");
        uniqueName.append(projectVersion);
        return uniqueName.toString();
    }

    private String getPolicyName(final String fullPath) {
        final int index = fullPath.lastIndexOf("/");
        return index > -1 ? fullPath.substring(index + 1) : fullPath;
    }

    private Map<String, GatewayEntity> getEntityDependencies(String policyNameWithPath, Bundle bundle) {
        Map<String, GatewayEntity> entityDependenciesMap = new HashMap<>();
        final Map<String, Policy> entityMap = bundle.getPolicies();
        final Policy policyEntity = entityMap.get(policyNameWithPath);
        if (policyEntity != null) {
            populateDependentFolders(entityDependenciesMap, policyEntity);
            entityDependenciesMap.put(policyNameWithPath, policyEntity);
            PolicyMetadata policyMetadata = policyEntity.getPolicyMetadata();
            if(policyMetadata != null){
                Set<Dependency> dependencies = policyMetadata.getUsedEntities();
                for (Dependency dependency : dependencies) {
                    Map<String, ? extends GatewayEntity> entities = bundle.getEntities(entityTypeRegistry.getEntityClass(dependency.getType()));
                    GatewayEntity dependentEntity = entities.get(dependency.getName());
                    entityDependenciesMap.put(dependency.getName(), dependentEntity);
                }
            }
        }
        return entityDependenciesMap;
    }

    private void populateDependentFolders(Map<String, GatewayEntity> gatewayEntities, GatewayEntity policyEntity) {
        if (policyEntity instanceof Folderable) {
            Folder folder = ((Folderable) policyEntity).getParentFolder();
            while (folder != null) {
                gatewayEntities.put(folder.getPath(), folder);
                folder = folder.getParentFolder();
            }
        }
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
