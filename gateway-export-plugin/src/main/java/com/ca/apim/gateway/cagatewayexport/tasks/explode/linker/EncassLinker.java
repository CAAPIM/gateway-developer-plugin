/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.ServiceAndPolicyLoaderUtil;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayexport.util.policy.EncassPolicyXMLSimplifier;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.L7_TEMPLATE;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.PolicyLinker.getPolicyPath;

@Singleton
public class EncassLinker implements EntityLinker<Encass> {
    private final EncassPolicyXMLSimplifier encassPolicyXMLSimplifier;

    @Inject
    EncassLinker(EncassPolicyXMLSimplifier encassPolicyXMLSimplifier) {
        this.encassPolicyXMLSimplifier = encassPolicyXMLSimplifier;
    }

    @Override
    public Class<Encass> getEntityClass() {
        return Encass.class;
    }

    @Override
    public void link(Encass encass, Bundle bundle, Bundle targetBundle) {
        Policy policy = bundle.getPolicies().values().stream().filter(p -> encass.getPolicyId().equals(p.getId())).findFirst().orElse(null);
        if (policy == null) {
            throw new LinkerException("Could not find policy for Encapsulated Assertion: " + encass.getName() + ". Policy ID: " + encass.getPolicyId());
        }
        final String l7template = encassPolicyXMLSimplifier.simplifyEncassPolicyXML(policy);
        encass.getProperties().put(L7_TEMPLATE, l7template);
        if ("true".equals(l7template) && ServiceAndPolicyLoaderUtil.migratePortalIntegrationsAssertions()) {
            Set<Annotation> annotations = new HashSet<>();
            Annotation bundleEntity = new Annotation(AnnotationType.BUNDLE);
            annotations.add(bundleEntity);
            encass.setAnnotations(annotations);
        }
        encass.setPolicy(policy.getPath());
        encass.setPath(getPolicyPath(policy, bundle, encass));
    }

}
