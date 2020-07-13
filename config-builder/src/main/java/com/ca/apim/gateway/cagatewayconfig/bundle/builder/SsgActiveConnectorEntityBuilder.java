package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.getDeploymentBundle;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.LISTEN_PORT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.SSG_ACTIVE_CONNECTOR;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

public class SsgActiveConnectorEntityBuilder implements EntityBuilder {
    private static final Integer ORDER = 1600;
    private final IdGenerator idGenerator;

    @Inject
    SsgActiveConnectorEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        if (bundle instanceof AnnotatedBundle) {
            Map<String, SsgActiveConnector> activeConnectorMap = Optional.ofNullable(bundle.getSsgActiveConnectors()).orElse(Collections.emptyMap());
            return buildEntities(activeConnectorMap, ((AnnotatedBundle) bundle).getFullBundle(), bundleType, document);
        } else {
            return buildEntities(bundle.getSsgActiveConnectors(), bundle, bundleType, document);
        }
    }

    private List<Entity> buildEntities(Map<String, SsgActiveConnector> entities, Bundle bundle, BundleType bundleType, Document document) {

        switch (bundleType) {
            case DEPLOYMENT:
                final Stream<Entity> activeConnectorOnlyMappings = entities.entrySet().stream().map(ssgActiveConnectorEntry ->
                        EntityBuilderHelper.getEntityWithOnlyMapping(EntityTypes.SSG_ACTIVE_CONNECTOR,
                                bundle.applyUniqueName(ssgActiveConnectorEntry.getKey(), BundleType.ENVIRONMENT, false),
                                generateId(ssgActiveConnectorEntry.getValue())));
                return activeConnectorOnlyMappings.collect(toList());
            case ENVIRONMENT:
                final Stream<Entity> activeConnectors = entities.entrySet().stream().map(ssgActiveConnectorEntry ->
                        buildActiveConnectorEntity(bundle, bundle.applyUniqueName(ssgActiveConnectorEntry.getKey(),
                                BundleType.ENVIRONMENT, false), ssgActiveConnectorEntry.getValue(), document));
                return activeConnectors.collect(toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    // also visible for testing
    Entity buildActiveConnectorEntity(Bundle bundle, String name, SsgActiveConnector ssgActiveConnector, Document document) {
        Element activeConnectorElement = document.createElement(ACTIVE_CONNECTOR);

        String id = generateId(ssgActiveConnector);
        activeConnectorElement.setAttribute(ATTRIBUTE_ID, id);
        activeConnectorElement.appendChild(createElementWithTextContent(document, NAME, name));
        activeConnectorElement.appendChild(createElementWithTextContent(document, ENABLED, TRUE.toString()));
        activeConnectorElement.appendChild(createElementWithTextContent(document, TYPE, ssgActiveConnector.getConnectorType()));


        if (ssgActiveConnector.getTargetServiceReference() != null) {
            activeConnectorElement.appendChild(createServiceElement(bundle, name, ssgActiveConnector, document));
        }
        updatePasswordRef(bundle, ssgActiveConnector);
        updatePrivateKeyRef(bundle, ssgActiveConnector);
        buildAndAppendPropertiesElement(ssgActiveConnector.getProperties(), document, activeConnectorElement);

        return EntityBuilderHelper.getEntityWithNameMapping(SSG_ACTIVE_CONNECTOR, name, id, activeConnectorElement);
    }

    private String generateId(SsgActiveConnector ssgActiveConnector) {
        if (ssgActiveConnector != null && ssgActiveConnector.getAnnotatedEntity() != null && ssgActiveConnector.getAnnotatedEntity().getId() != null) {
            return ssgActiveConnector.getAnnotatedEntity().getId();
        }
        return idGenerator.generate();
    }

    private void updatePasswordRef(Bundle bundle, SsgActiveConnector entity) {
        entity.getProperties().entrySet().stream().forEach(entry -> {
            if (entry.getKey().endsWith("SecurePasswordOid")) {
                String value = (String) entry.getValue();
                if (value == null || value.isEmpty()) {
                    return;
                }
                StoredPassword storedPassword = bundle.getEntities(StoredPassword.class).values().stream().filter(s -> s.getKey().equals(value)).findFirst().orElse(null);
                if (storedPassword == null) {
                    throw new EntityBuilderException("Could not find password for Active Connector: " + entity.getName() + ". Password Reference: " + value);
                }
                entry.setValue(storedPassword.getId());
            }
        });
    }

    private void updatePrivateKeyRef(Bundle bundle, SsgActiveConnector entity) {
        final AtomicReference<String> privateKeyRef = new AtomicReference<>();
        final AtomicReference<String> key = new AtomicReference<>();
        entity.getProperties().entrySet().stream().forEach(entry -> {
            if (entry.getKey().endsWith("SslKeystoreAlias")) {
                String value = (String) entry.getValue();
                if (value == null || value.isEmpty()) {
                    return;
                }
                PrivateKey privateKey = bundle.getEntities(PrivateKey.class).values().stream().filter(s -> s.getAlias().equals(value)).findFirst().orElse(null);
                if (privateKey == null) {
                    throw new EntityBuilderException("Could not find private key for Active Connector: " + entity.getName() + ". Private Key Reference: " + value);
                }
                final String[] id = privateKey.getId().split(":");
                privateKeyRef.set(id[0]);
                key.set(entry.getKey().replace("SslKeystoreAlias", "SslKeystoreId"));
            }
        });
        entity.getProperties().put(key.get(), privateKeyRef.get());
    }

    private Element createServiceElement(Bundle bundle, String name, SsgActiveConnector ssgActiveConnector, Document document) {
        String targetServiceReference = ssgActiveConnector.getTargetServiceReference();
        Service service = bundle.getServices().get(targetServiceReference);

        if (service == null || service.getId() == null) {
            service = getDeploymentBundle().getServices().get(targetServiceReference);
        }

        if (service == null) {
            throw new EntityBuilderException("Could not find service binded to active connector " + name + ". Service Path: " + targetServiceReference);
        }

        return createElementWithTextContent(document, HARDWIRED, service.getId());
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
