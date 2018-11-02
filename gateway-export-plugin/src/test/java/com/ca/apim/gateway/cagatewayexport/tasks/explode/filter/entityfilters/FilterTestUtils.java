package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import org.jetbrains.annotations.NotNull;

public final class FilterTestUtils {
    public static final String ROOT_FOLDER_ID = "0000000000000000ffffffffffffec76";

    @NotNull
    public static Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.addEntity(new Folder("Root Node", ROOT_FOLDER_ID, null));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        return bundle;
    }
}
