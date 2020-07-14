package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CassandraAssertionBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;
    private CassandraAssertionBuilder cassandraAssertionBuilder = new CassandraAssertionBuilder();
    private PolicyBuilderContext policyBuilderContext;
    private ProjectInfo projectInfo = new ProjectInfo("TestProject", "TestGroup", "1.0.0");

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("test/policy/path.xml");
        bundle = new Bundle(projectInfo);
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
    }

    @Test
    public void testBuildAssertionElement() {
        String connectionName = "cassandraTest";
        Element cassandraAssertion = createCassandraAssertion(document, connectionName);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        cassandraAssertionBuilder.buildAssertionElement(cassandraAssertion, policyBuilderContext);

        Element cassandraConnectionName = getSingleChildElement(cassandraAssertion, CASSANDRA_CONNECTION_NAME, true);
        assertNotNull(cassandraConnectionName);
        assertEquals("::" + projectInfo.getGroupName() + "::" + connectionName + "::1.0", cassandraConnectionName.getAttributes().getNamedItem(STRING_VALUE).getTextContent());
    }

    @NotNull
    private Element createCassandraAssertion(Document document, String connectionName) {
        Element authenticationElement = createElementWithChildren(
                document,
                CASSANDRA_QUERY_ASSERTION,
                createElementWithAttribute(document, PolicyXMLElements.CASSANDRA_CONNECTION_NAME, STRING_VALUE, connectionName)
        );

        return authenticationElement;
    }

}
