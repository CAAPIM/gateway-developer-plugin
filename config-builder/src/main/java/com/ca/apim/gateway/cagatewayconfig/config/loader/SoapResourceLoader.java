package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.SoapResource;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.SoapResource.WSDL_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.beans.SoapResource.XSD_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.getPath;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

/**
 * An entity loader responsible for reading SoapResource file (.wsdl or .xsd extensions) and primarily loading in the Soap Resource
 * content into a java entity bean representation
 */
@Singleton
public class SoapResourceLoader implements EntityLoader {

    private final FileUtils fileUtils;

    @Inject
    SoapResourceLoader(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    /**
     * Load all SoapResource entities from SoapResource file into a Bundle object
     *
     * @param bundle the bundle object to receive loaded entities
     * @param rootDir the directory containing the entity file (or subdir config)
     */
    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File soapResourceRootDir = FolderLoaderUtils.getSoapResourcesRootDir(rootDir);
        if (soapResourceRootDir == null) return;

        final Map<String, SoapResource> soapResources = new HashMap<>();
        loadSoapResources(soapResourceRootDir, soapResourceRootDir, soapResources);
        bundle.putAllSoapResources(soapResources);
    }

    /**
     * Throws an exception when attempting to load SoapResource individually
     *
     * @param bundle the bundle to load the entity into
     * @param name name of the entity
     * @param value value to be loaded, either json or property value
     */
    @Override
    public void load(Bundle bundle, String name, String value) {
        throw new ConfigLoadException("Cannot load an individual soapResource");
    }

    /**
     * Throws an exception when attempting to load SoapResource individually
     *
     * @param name name of the entity to be loaded
     * @param entitiesFile file that contains the entity
     * @return an exception
     */
    @Override
    public Object loadSingle(String name, File entitiesFile) {
        throw new ConfigLoadException("Cannot load an individual soapResource");
    }

    /**
     * Loads all SoapResource files into a map of SoapResource entities
     *
     * @param currentDir
     * @param rootDir
     * @param soapResources
     */
    private void loadSoapResources(final File currentDir, final File rootDir, final Map<String, SoapResource> soapResources) {
        final File[] children = currentDir.listFiles();
        if (children != null) {
            for (final File child : children) {
                if (child.isDirectory()) {
                    loadSoapResources(child, rootDir, soapResources);
                } else if (equalsAnyIgnoreCase("." + getExtension(child.getName()), WSDL_EXTENSION, XSD_EXTENSION)) {
                    SoapResource soapResource = loadSoapResource(child, rootDir);
                    soapResources.put(soapResource.getPath(), soapResource);
                }
            }
        }
    }

    /**
     * Loads a single SoapResource entity
     *
     * @param soapResourceFile
     * @param rootDir
     * @return a SoapResource entity
     */
    private SoapResource loadSoapResource(final File soapResourceFile, final File rootDir) {
        SoapResource soapResource = new SoapResource();
        String soapResourcePath = getPath(soapResourceFile, rootDir);
        String soapResourceName = soapResourceFile.getName();

        soapResource.setPath(soapResourcePath);
        soapResource.setName(FilenameUtils.getBaseName(soapResourceName));
        soapResource.setContent(fileUtils.getFileAsString(soapResourceFile));
        soapResource.setTypeByExtension(FilenameUtils.getExtension(soapResourceFile.getName()));

        return soapResource;
    }

    /**
     *
     * @return a soapResource entity type
     */
    @Override
    public String getEntityType() { return EntityTypes.WSDL_TYPE; }
}
