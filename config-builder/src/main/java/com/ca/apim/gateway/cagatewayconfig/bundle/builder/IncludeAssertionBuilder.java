package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.DependentBundle;
import com.ca.apim.gateway.cagatewayconfig.beans.MissingGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.POLICY_GUID;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

public class IncludeAssertionBuilder implements PolicyAssertionBuilder {
    private static final Logger LOGGER = Logger.getLogger(IncludeAssertionBuilder.class.getName());
    static final String POLICY_PATH = "policyPath";

    @Override
    public void buildAssertionElement(Element includeAssertionElement, PolicyBuilderContext policyBuilderContext) {
        final Bundle bundle = policyBuilderContext.getBundle();
        final AnnotatedBundle annotatedBundle = policyBuilderContext.getAnnotatedBundle();
        final Policy policy = policyBuilderContext.getPolicy();
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

            //if policy is not found in any bundles then check if it missing and excluded
            final MissingGatewayEntity missingEntity = bundle.getMissingEntities().get(policyPath);
            if (missingEntity != null && missingEntity.isExcluded()) {
                LOGGER.log(Level.WARNING, "Resolving the referenced policy {0} as known excluded entity with guid: {1}",
                        new Object[]{policyPath, missingEntity.getGuid()});
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

    @Override
    public String getAssertionTagName() {
        return PolicyXMLElements.INCLUDE;
    }
}
