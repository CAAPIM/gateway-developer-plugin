package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.FolderTree;
import org.jetbrains.annotations.NotNull;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER;

public final class FilterTestUtils {

    @NotNull
    public static Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        return bundle;
    }
}
