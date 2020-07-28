package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PolicyBuilderContext {
    private String policyName;
    private Document policyDocument;
    private Bundle bundle;
    private IdGenerator idGenerator;
    private AnnotatedBundle annotatedBundle;
    private Policy policy;

    PolicyBuilderContext(String policyName, Document policyDocument, Bundle bundle, IdGenerator idGenerator) {
        this.policyName = policyName;
        this.policyDocument = policyDocument;
        this.bundle = bundle;
        this.idGenerator = idGenerator;
        this.annotatedBundle = annotatedBundle;
    }

    PolicyBuilderContext withPolicy(Policy policy) {
        this.policy = policy;
        return this;
    }

    PolicyBuilderContext withAnnotatedBundle(AnnotatedBundle annotatedBundle) {
        this.annotatedBundle = annotatedBundle;
        return this;
    }

    public String getPolicyName() {
        return policyName;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public Document getPolicyDocument() {
        return policyDocument;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public AnnotatedBundle getAnnotatedBundle() {
        return annotatedBundle;
    }

    public Policy getPolicy() {
        return policy;
    }
}
