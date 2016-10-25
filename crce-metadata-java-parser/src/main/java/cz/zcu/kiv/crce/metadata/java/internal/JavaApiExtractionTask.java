package cz.zcu.kiv.crce.metadata.java.internal;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.jacc.javatypes.JBlackBoxComponent;
import cz.zcu.kiv.jacc.loader.JClassLoaderFacade;

import cz.zcu.kiv.crce.concurrency.model.Task;
import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.dao.MetadataDao;
import cz.zcu.kiv.crce.metadata.java.parser.JavaMetadataParser;
import cz.zcu.kiv.crce.metadata.service.MetadataService;

/**
 * Background task for calculation of compatibility data for a Resource.
 * <p/>
 * Creates compatibility data for all resources with the same crce.identity name and lower version.
 * <p/>
 * Date: 19.11.13
 *
 * @author Jakub Danek
 */
public class JavaApiExtractionTask extends Task<Object> {
    private static final Logger logger = LoggerFactory.getLogger(JavaApiExtractionTask.class);

    private List<Resource> resources;
    private JClassLoaderFacade classLoader;
    private JavaMetadataParser metadataParser;
    private MetadataService metadataService;
    private MetadataDao resourceDAO;

    /**
     * @param id       ID of the task, for tracking
     * @param resource resource for which the compatibility data shall be computed
     */
    public JavaApiExtractionTask(String id, Resource resource, JavaMetadataParser metadataParser, MetadataService metadataService, MetadataDao resourceDAO) {
        this(id, Collections.singletonList(resource), metadataParser, metadataService, resourceDAO);
    }

    public JavaApiExtractionTask(String id, List<Resource> resources, JavaMetadataParser metadataParser, MetadataService metadataService, MetadataDao resourceDAO) {
        super(id, "Extracts java api from the given resource.", "crce-api-java-extractor");
        this.resources = resources;
        this.classLoader = JClassLoaderFacade.JAR_MEMORY_FACADE;
        this.metadataParser = metadataParser;
        this.metadataService = metadataService;
        this.resourceDAO = resourceDAO;
    }

    @Override
    protected Object run() throws Exception {
        for (Resource resource : resources) {
            logger.debug("Extracting Java API for bundle id: {}, name: {}", resource.getId(), metadataService.getExternalId(resource));
            try {
                JBlackBoxComponent API = classLoader.getAPI(new File(metadataService.getUri(resource)));

                Set<Capability> apiCapabilities = metadataParser.parse(API.getExports());
                for (Capability apiCapability : apiCapabilities) {
                    metadataService.addRootCapability(resource, apiCapability);
                }

                metadataService.addCategory(resource, JavaApiIndexer.CATEGORY);
                resourceDAO.saveResource(resource);
                logger.debug("Successfully processed bundle: {}", resource.getId());
            } catch (Exception e) {
                logger.error("Unable to process resource file for the resource: {}", resource.getId());
                logger.error("Exception thrown: ",e);
            }
        }

        return true;
    }


}
