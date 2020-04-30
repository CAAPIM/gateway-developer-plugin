package com.ca.apim.gateway.cagatewayconfig.beans;

import java.util.Set;

public interface AnnotatableEntity {
    public Set<Annotation> getAnnotations();

    public void setAnnotations(Set<Annotation> annotations);
}
