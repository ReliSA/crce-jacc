package cz.zcu.kiv.crce.metadata.java.internal;

import java.io.File;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.jacc.javatypes.JBlackBoxComponent;
import cz.zcu.kiv.jacc.loader.JClassLoaderFacade;

import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.dao.MetadataDao;
import cz.zcu.kiv.crce.metadata.java.parser.JavaMetadataParser;
import cz.zcu.kiv.crce.metadata.service.MetadataService;
import cz.zcu.kiv.crce.repository.Store;
import cz.zcu.kiv.crce.repository.plugins.AbstractActionHandler;

/**
 * Extracts and stores public Java API information for the resource.
 *
 * Date: 13.11.15
 *
 * @author Jakub Danek
 */
public class JavaApiIndexer extends AbstractActionHandler {
    private static final Logger logger = LoggerFactory.getLogger(JavaApiIndexer.class);

    private JClassLoaderFacade classLoader;
    private JavaMetadataParser metadataParser; //DI
    private MetadataService metadataService; //DI
    private MetadataDao resourceDAO; //DI

    public JavaApiIndexer() {
        classLoader = JClassLoaderFacade.JAR_MEMORY_FACADE;
    }


    @Override
    public Resource afterPutToStore(Resource resource, Store store) {
        try {
            JBlackBoxComponent API = classLoader.getAPI(new File(metadataService.getUri(resource)));

            Set<Capability> apiCapabilities = metadataParser.parse(API.getExports());
            for (Capability apiCapability : apiCapabilities) {
                metadataService.addRootCapability(resource, apiCapability);
            }
            resourceDAO.saveResource(resource);
            logger.info("Successfully processed bundle: {}", resource.getId());
        } catch (Exception e) {
            logger.warn("Unable to process resource file for the resource: {}", resource.getId());
            logger.debug("Exception thrown: ",e);
        }

        return resource;
    }

}
