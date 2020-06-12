package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.beans.SsgActiveConnector;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.ServiceLinker.getServicePath;

public class SsgActiveConnectorLinker implements EntityLinker<SsgActiveConnector> {

    @Override
    public Class<SsgActiveConnector> getEntityClass() {
        return SsgActiveConnector.class;
    }

    @Override
    public void link(SsgActiveConnector entity, Bundle bundle, Bundle targetBundle) {
        linkService(bundle, entity);
        linkPassword(bundle, entity);
    }

    private void linkPassword(Bundle bundle, SsgActiveConnector entity) {
        entity.getProperties().entrySet().stream().forEach(entry -> {
            if (entry.getKey().endsWith("SecurePasswordOid")) {
                String value = (String) entry.getValue();
                if (value == null || value.isEmpty()) {
                    return;
                }
                StoredPassword storedPassword = bundle.getEntities(StoredPassword.class).values().stream().filter(s -> s.getId().equals(value)).findFirst().orElse(null);
                if (storedPassword == null) {
                    throw new LinkerException("Could not find password for Active Connector: " + entity.getName() + ". Password Reference: " + value);
                }
                entry.setValue(storedPassword.getKey());
            }
        });


    }

    private void linkService(Bundle bundle, SsgActiveConnector entity) {
        if (entity.getTargetServiceReference() == null || entity.getTargetServiceReference().isEmpty()) {
            return;
        }
        Service service = bundle.getEntities(Service.class).values().stream().filter(s -> s.getId().equals(entity.getTargetServiceReference())).findFirst().orElse(null);
        if (service == null) {
            throw new LinkerException("Could not find Service for Active Connector: " + entity.getName() + ". Service Reference: " + entity.getTargetServiceReference());
        }
        entity.setTargetServiceReference(getServicePath(bundle, service));
    }

}
