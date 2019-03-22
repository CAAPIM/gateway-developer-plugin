package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.beans.Wsdl;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.createFolder;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.getPath;

/**
 * An entity loader responsible for reading Wsdl file (.wsdl extension) and primarily loading in the Wsdl
 * content into a java entity bean representation
 */
@Singleton
public class WsdlLoader implements EntityLoader {

    private final FileUtils fileUtils;
    private static final String EXTENSION = ".wsdl";

    @Inject
    WsdlLoader(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    /**
     * Load all Wsdl entities from Wsdl file into a Bundle object
     *
     * @param bundle the bundle object to receive loaded entities
     * @param rootDir the directory containing the entity file (or subdir config)
     */
    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File wsdlRootDir = FolderLoaderUtils.getWsdlRootDir(rootDir);
        if (wsdlRootDir == null) return;

        final Map<String, Wsdl> wsdls = new HashMap<>();
        loadWsdls(wsdlRootDir, wsdlRootDir, null, wsdls, bundle);
        bundle.putAllWsdls(wsdls);
    }

    /**
     * Throws an exception when attempting to load Wsdl individually
     *
     * @param bundle the bundle to load the entity into
     * @param name name of the entity
     * @param value value to be loaded, either json or property value
     */
    @Override
    public void load(Bundle bundle, String name, String value) {
        throw new ConfigLoadException("Cannot load an individual wsdl");
    }

    /**
     * Throws an exception when attempting to load Wsdl individually
     *
     * @param name name of the entity to be loaded
     * @param entitiesFile file that contains the entity
     * @return an exception
     */
    @Override
    public Object loadSingle(String name, File entitiesFile) {
        throw new ConfigLoadException("Cannot load an individual wsdl");
    }

    /**
     * Loads all Wsdl files into a map of Wsdl entities
     *
     * @param currentDir
     * @param rootDir
     * @param parentFolder
     * @param wsdls
     * @param bundle
     */
    private void loadWsdls(final File currentDir, final File rootDir, Folder parentFolder, final Map<String, Wsdl> wsdls, Bundle bundle) {
        Folder folder = bundle.getFolders().computeIfAbsent(getPath(currentDir, rootDir), key -> createFolder(currentDir.getName(), key, parentFolder));
        final File[] children = currentDir.listFiles();
        if (children != null) {
            for (final File child : children) {
                if (child.isDirectory()) {
                    loadWsdls(child, rootDir, folder, wsdls, bundle);
                } else if (child.getName().endsWith(EXTENSION)) {
                    Wsdl wsdl = loadWsdl(child, rootDir, folder);
                    wsdls.put(wsdl.getPath(), wsdl);
                }
            }
        }
    }

    /**
     * Loads a single Wsdl entity
     *
     * @param wsdlFile
     * @param rootDir
     * @param parentFolder
     * @return a Wsdl entity
     */
    private Wsdl loadWsdl(final File wsdlFile, final File rootDir, Folder parentFolder) {
        Wsdl wsdl = new Wsdl();
        String wsdlPath = getPath(wsdlFile, rootDir);
        String wsdlName = wsdlFile.getName();

        wsdl.setPath(wsdlPath.substring(0, wsdlPath.length() - EXTENSION.length()));
        wsdl.setName(FilenameUtils.getBaseName(wsdlName));

        wsdl.setParentFolder(parentFolder);

        wsdl.setWsdlXml(fileUtils.getFileAsString(wsdlFile));

        return wsdl;
    }

    /**
     *
     * @return a wsdl entity type
     */
    @Override
    public String getEntityType() { return EntityTypes.WSDL_TYPE; }
}
