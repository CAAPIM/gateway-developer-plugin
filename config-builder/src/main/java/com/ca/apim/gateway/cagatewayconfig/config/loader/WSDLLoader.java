package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.beans.WSDL;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.createFolder;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.getPath;

@Singleton
public class WSDLLoader implements EntityLoader {

    private final FileUtils fileUtils;
    private static final String EXTENSION = ".wsdl";

    @Inject
    WSDLLoader(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File wsdlRootDir = FolderLoaderUtils.getWSDLRootDir(rootDir);
        if (wsdlRootDir == null) return;

        final Map<String, WSDL> wsdls = new HashMap<>();
        loadWSDLs(wsdlRootDir, rootDir, null, wsdls, bundle);
        bundle.putAllWSDLs(wsdls);
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        throw new ConfigLoadException("Cannot load an individual wsdl");
    }

    @Override
    public Object loadSingle(String name, File entitiesFile) {
        throw new ConfigLoadException("Cannot load an individual wsdl");
    }

    private void loadWSDLs(final File currentDir, final File rootDir, Folder parentFolder, final Map<String, WSDL> wsdls, Bundle bundle) {
        Folder folder = bundle.getFolders().computeIfAbsent(getPath(currentDir, rootDir), key -> createFolder(currentDir.getName(), key, parentFolder));
        final File[] children = currentDir.listFiles();
        if (children != null) {
            for (final File child : children) {
                if (child.isDirectory()) {
                    loadWSDLs(child, rootDir, folder, wsdls, bundle);
                } else if (child.getName().endsWith(EXTENSION)) {
                    WSDL wsdl = loadWSDL(child, rootDir, folder, bundle);
                    WSDL existingWSDL = wsdls.put(wsdl.getPath(), wsdl);
                    if (existingWSDL != null) {
                        throw new ConfigLoadException("Found multiple wsdls with same path but different types. WSDL Path: " + wsdl.getPath());
                    }
                }
            }
        }
    }

    private WSDL loadWSDL(final File wsdlFile, final File rootDir, Folder parentFolder, Bundle bundle) {
        WSDL wsdl = new WSDL();
        String wsdlPath = getPath(wsdlFile, rootDir);
        String wsdlName = wsdlFile.getName();

        wsdl.setPath(wsdlPath.substring(0, wsdlPath.length() - EXTENSION.length()));
        wsdl.setName(wsdlName.substring(0, wsdlName.length() - EXTENSION.length()));

        wsdl.setParentFolder(parentFolder);

        wsdl.setWsdlXml(fileUtils.getFileAsString(wsdlFile));

        return wsdl;
    }

    @Override
    public String getEntityType() { return EntityTypes.WSDL_TYPE; }
}
