package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class FolderFilter implements EntityFilter<Folder> {

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return Collections.emptySet();
    }

    @Override
    public List<Folder> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        return bundle.getFolderTree().stream()
                // keep all folders that are children of the given folder path
                .filter(f -> {
                    String path = bundle.getFolderTree().getFormattedPath(f);
                    // Children folders have a path that starts with the folder path
                    return ("/" + path).startsWith(folderPath);
                }).collect(Collectors.toList());
    }

    /**
     * Returns a list of parent folders from the given bundle for the folder with the given path
     *
     * @param folderPath The folder path to get parent folders for
     * @param bundle     The bundle to find the parent folders in
     * @return The list of parent folders for the given folder path
     */
    @SuppressWarnings("squid:S1075")
    public static List<Folder> parentFolders(String folderPath, Bundle bundle) {
        String folderPathWithSuffix = folderPath + "/";
        Stream<Folder> folderStream = bundle.getFolderTree().stream()
                // keep all folders that are parents of the given folder path
                .filter(f -> {
                    String path = bundle.getFolderTree().getFormattedPath(f);
                    // Parent folders have a path that is the beginning of the folder path
                    return folderPathWithSuffix.startsWith("/" + path + "/");
                });
        return Stream.concat(Stream.of(bundle.getFolderTree().getRootFolder()), folderStream).collect(Collectors.toList());
    }
}
