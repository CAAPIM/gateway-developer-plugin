package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class AnnotatedBundleTest {

    @Test
    void testApplyUniqueName() {
        String uniqueSeparator = "::";
        String policeName = "TestPolicy";
        Encass encass = new Encass();
        AnnotatedEntity<Encass> encassAnnotatedEntity = new AnnotatedEntity<>(encass);
        encassAnnotatedEntity.setEntityName("TestEncass");
        ProjectInfo projectInfo = new ProjectInfo("TestName", "TestGroup", "unspecified");
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        String uniquePolicyName = annotatedBundle.applyUniqueName(policeName);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + "."
                + annotatedBundle.getBundleName() + uniqueSeparator + policeName, uniquePolicyName);


        projectInfo = new ProjectInfo("TestName", "TestGroup", "1");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniquePolicyName = annotatedBundle.applyUniqueName(policeName);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + "."
                + "TestName-TestEncass" + uniqueSeparator + policeName + uniqueSeparator +"1.0", uniquePolicyName);


        projectInfo = new ProjectInfo("TestName", "TestGroup", "1.");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniquePolicyName = annotatedBundle.applyUniqueName(policeName);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + "."
                + "TestName-TestEncass" + uniqueSeparator + policeName + uniqueSeparator +"1.0", uniquePolicyName);

        projectInfo = new ProjectInfo("TestName", "TestGroup", "1.2");
        encassAnnotatedEntity.setBundleName("TestBundle");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniquePolicyName = annotatedBundle.applyUniqueName(policeName);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + "."
                + "TestBundle" + uniqueSeparator + policeName + uniqueSeparator +"1.2", uniquePolicyName);
    }
}
