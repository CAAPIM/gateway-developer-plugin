package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;

public class UnsupportedEntityLoader extends EntityLoaderBase<UnsupportedGatewayEntity> implements EntityLoader {

    private final EntityUtils.GatewayEntityInfo gatewayEntityInfo;

    @Inject
    public UnsupportedEntityLoader(final JsonTools jsonTools, final IdGenerator idGenerator) {
        super(jsonTools, idGenerator);
        this.gatewayEntityInfo = EntityUtils.createEntityInfo(UnsupportedGatewayEntity.class);
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        super.load(bundle, rootDir);
        updateItemXml(bundle, new File(rootDir, "config"));
    }

    @Override
    public void load(Bundle bundle, String name, String value, String environmentConfigurationFolderPath) {
        super.load(bundle, name, value);
        File configFolder = new File(environmentConfigurationFolderPath);
        updateItemXml(bundle, configFolder);
    }

    private void updateItemXml(Bundle bundle, File configFolder) {
        final File unsupportedEntityXml = new File(configFolder, "unsupported-entities.xml");
        final Map<String, UnsupportedGatewayEntity> unsupportedGatewayEntityMap = bundle.getUnsupportedEntities();
        try {
            final Document document = DocumentTools.INSTANCE.parse(unsupportedEntityXml);
            final List<Element> items = getChildElements(document.getDocumentElement(), ITEM);
            items.forEach(item -> {
                final String itemName = getSingleChildElementTextContent(item, NAME);
                final Element resource = getSingleChildElement(item, RESOURCE);
                NodeList nodeList = resource.getChildNodes();
                Element resourceXml = null;
                for (int index = 0; index < nodeList.getLength(); index++) {
                    Node node = nodeList.item(index);
                    if (node instanceof Element) {
                        resourceXml = (Element) node;
                    }
                }
                if (itemName != null) {
                    final UnsupportedGatewayEntity unsupportedGatewayEntity = unsupportedGatewayEntityMap.get(itemName);
                    if (unsupportedGatewayEntity != null) {
                        unsupportedGatewayEntity.setElement(resourceXml);
                    }
                }
            });
        } catch (DocumentParseException e) {
            throw new ConfigLoadException("Cannot load unsupported entities", e);
        }
    }

    @Override
    public String getEntityType() {
        return gatewayEntityInfo.getType();
    }

    @Override
    protected Class<UnsupportedGatewayEntity> getBeanClass() {
        return UnsupportedGatewayEntity.class;
    }

    @Override
    protected String getFileName() {
        return gatewayEntityInfo.getFileName();
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, UnsupportedGatewayEntity> entitiesMap) {
        bundle.getUnsupportedEntities().putAll(entitiesMap);
    }
}
