package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.IdValidator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class EncapsulatedAssertionBuilder implements PolicyAssertionBuilder {
    private static final Logger LOGGER = Logger.getLogger(EncapsulatedAssertionBuilder.class.getName());
    static final String ENCASS_NAME = "encassName";
    static final String STRING_VALUE = "stringValue";
    static final String BOOLEAN_VALUE = "booleanValue";
    public static final String ZERO_GUID = "00000000-0000-0000-0000-000000000000";

    @Override
    public void buildAssertionElement(Element encapsulatedAssertionElement, PolicyBuilderContext policyBuilderContext) throws DocumentParseException {
        final Bundle bundle = policyBuilderContext.getBundle();
        final AnnotatedBundle annotatedBundle = policyBuilderContext.getAnnotatedBundle();
        final Policy policy = policyBuilderContext.getPolicy();
        if (encapsulatedAssertionElement.hasAttribute(ENCASS_NAME)) {
            final String encassName = encapsulatedAssertionElement.getAttribute(ENCASS_NAME);
            Encass encass = getEncass(bundle, encassName, annotatedBundle);
            final String guid = findEncassReferencedGuid(policy, encass, encapsulatedAssertionElement, encassName);
            updateEncapsulatedAssertion(policyBuilderContext, encapsulatedAssertionElement, encass, encassName, guid);
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
                    DependentBundle dependentBundle = b.getDependentBundleFrom();
                    if (dependentBundle != null && dependentBundle.getType() != null) {
                        if (annotatedBundle != null) {
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
                            new Object[]{name, missingEntity.getGuid()});
                    referenceEncass.set(getExcludedEncass(missingEntity.getGuid()));
                }
            }
        }
        return referenceEncass.get();
    }

    private Encass getExcludedEncass(final String guid) {
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

    private void updateEncapsulatedAssertion(PolicyBuilderContext policyBuilderContext, Node encapsulatedAssertionElement,
                                             Encass encass, String name, String guid) {
        String encassName = name;
        String encassGuid = guid;
        final IdGenerator idGenerator = policyBuilderContext.getIdGenerator();
        final AnnotatedBundle annotatedBundle = policyBuilderContext.getAnnotatedBundle();
        AnnotatedEntity annotatedEntity = annotatedBundle != null ? annotatedBundle.getAnnotatedEntity() : null;
        if (encass != null && !encass.isExcluded() && annotatedEntity != null) {
            AnnotatedEntity annotatedEncassEntity = encass.getAnnotatedEntity();
            if (annotatedEntity.isReusable()) {
                if (annotatedEncassEntity != null) {
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
                encassName = annotatedBundle.applyUniqueName(encassName, EntityBuilder.BundleType.DEPLOYMENT, false);
            }
        }
        Element encapsulatedAssertionConfigNameElement = createElementWithAttribute(
                policyBuilderContext.getPolicyDocument(),
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
                policyBuilderContext.getPolicyDocument(),
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

    @Override
    public String getAssertionTagName() {
        return ENCAPSULATED;
    }
}
