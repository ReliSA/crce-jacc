package cz.zcu.kiv.crce.metadata.java.internal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.crce.concurrency.model.Task;
import cz.zcu.kiv.crce.concurrency.service.TaskRunnerService;
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

    public static final String CATEGORY = "java-api";

    private volatile JavaMetadataParser metadataParser; //DI
    private volatile MetadataService metadataService; //DI
    private volatile MetadataDao resourceDAO; //DI
    private volatile TaskRunnerService taskRunnerService;   /* injected by dependency manager */

    @Override
    public Resource afterPutToStore(Resource resource, Store store) {
        logger.debug("Java API Extraction task about to schedule for {}.", resource.getId());
        Task task = new JavaApiExtractionTask("JAVA:" + resource.getId(), resource, metadataParser, metadataService, resourceDAO);
        taskRunnerService.scheduleTask(task);
        logger.debug("Java API Extraction task scheduled for {}.", resource.getId());

        return resource;
    }

    /**
     * Ran by OSGi on bundle startup. Schedules indexing of unindexed resources stored in the database.
     * @throws Exception
     */
    protected void init() throws Exception {
        List<Resource> resources = resourceDAO.loadResourcesWithoutCategory(JavaApiIndexer.CATEGORY);

        logger.debug("Bulk Java API Extraction task about to schedule for {} resources.", resources.size());
        Task task = new JavaApiExtractionTask("JAVA API - INIT:", resources, metadataParser, metadataService, resourceDAO);
        taskRunnerService.scheduleTask(task);
        logger.debug("Bulk Java API Extraction task scheduled for {} resources.", resources.size());


    }

}
