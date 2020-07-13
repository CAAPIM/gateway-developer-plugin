package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class AnnotatedBundleTest {

    @Test
    void testApplyUniqueNameForDeployment() {
        String uniqueSeparator = "::";
        String policeName = "TestPolicy";
        Encass encass = new Encass();
        AnnotatedEntity<Encass> encassAnnotatedEntity = new AnnotatedEntity<>(encass);
        encassAnnotatedEntity.setEntityName("TestEncass");
        ProjectInfo projectInfo = new ProjectInfo("TestName", "TestGroup", "unspecified");
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        String uniquePolicyName = annotatedBundle.applyUniqueName(policeName, EntityBuilder.BundleType.DEPLOYMENT, false);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + "."
                + annotatedBundle.getBundleName() + uniqueSeparator + policeName, uniquePolicyName);


        projectInfo = new ProjectInfo("TestName", "TestGroup", "1");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniquePolicyName = annotatedBundle.applyUniqueName(policeName, EntityBuilder.BundleType.DEPLOYMENT, false);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + "."
                + "TestName-TestEncass" + uniqueSeparator + policeName + uniqueSeparator + "1.0", uniquePolicyName);


        projectInfo = new ProjectInfo("TestName", "TestGroup", "1.");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniquePolicyName = annotatedBundle.applyUniqueName(policeName, EntityBuilder.BundleType.DEPLOYMENT, false);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + "."
                + "TestName-TestEncass" + uniqueSeparator + policeName + uniqueSeparator + "1.0", uniquePolicyName);

        projectInfo = new ProjectInfo("TestName", "TestGroup", "1.2");
        encassAnnotatedEntity.setBundleName("TestBundle");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniquePolicyName = annotatedBundle.applyUniqueName(policeName, EntityBuilder.BundleType.DEPLOYMENT, false);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + "."
                + "TestBundle" + uniqueSeparator + policeName + uniqueSeparator + "1.2", uniquePolicyName);
    }

    @Test
    void testApplyUniqueNameForEnvironment() {
        String uniqueSeparator = "::";
        String entityName = "TestConnection";
        Encass encass = new Encass();
        AnnotatedEntity<Encass> encassAnnotatedEntity = new AnnotatedEntity<>(encass);
        encassAnnotatedEntity.setEntityName("TestEncass");
        ProjectInfo projectInfo = new ProjectInfo("TestName", "TestGroup", "unspecified");
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        String uniqueEntityName = annotatedBundle.applyUniqueName(entityName, EntityBuilder.BundleType.ENVIRONMENT, false);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName()
                + uniqueSeparator + entityName, uniqueEntityName);


        projectInfo = new ProjectInfo("TestName", "TestGroup", "1");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniqueEntityName = annotatedBundle.applyUniqueName(entityName, EntityBuilder.BundleType.ENVIRONMENT, false);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + uniqueSeparator + entityName + uniqueSeparator + "1.0", uniqueEntityName);


        projectInfo = new ProjectInfo("TestName", "TestGroup", "1.");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniqueEntityName = annotatedBundle.applyUniqueName(entityName, EntityBuilder.BundleType.ENVIRONMENT, false);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + uniqueSeparator + entityName + uniqueSeparator + "1.0", uniqueEntityName);

        projectInfo = new ProjectInfo("TestName", "TestGroup", "1.2");
        encassAnnotatedEntity.setBundleName("TestBundle");
        annotatedBundle = new AnnotatedBundle(null, encassAnnotatedEntity, projectInfo);
        uniqueEntityName = annotatedBundle.applyUniqueName(entityName, EntityBuilder.BundleType.ENVIRONMENT, false);
        Assert.assertEquals(uniqueSeparator + projectInfo.getGroupName() + uniqueSeparator + entityName + uniqueSeparator + "1.2", uniqueEntityName);
    }
}
