package cz.zcu.kiv.crce.metadata.java.internal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.crce.concurrency.model.Task;
import cz.zcu.kiv.crce.concurrency.service.TaskRunnerService;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.dao.MetadataDao;
import cz.zcu.kiv.crce.metadata.java.JavaApiExtractor;
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

    private volatile JavaApiExtractor extractor; //DI
    private volatile MetadataDao resourceDAO; //DI
    private volatile TaskRunnerService taskRunnerService;   /* injected by dependency manager */

    @Override
    public Resource afterPutToStore(Resource resource, Store store) {
        logger.debug("Java provided API Extraction task about to schedule for {}.", resource.getId());
        Task task = new JavaApiExtractionTask("JAVA:" + resource.getId(), resource, JavaApiExtractionTask.Target.JAVA_PROVIDED, extractor, resourceDAO);
        taskRunnerService.scheduleTask(task);
        logger.debug("Java provided API Extraction task scheduled for {}.", resource.getId());

        logger.debug("Java required API Extraction task about to schedule for {}.", resource.getId());
        task = new JavaApiExtractionTask("JAVA:" + resource.getId(), resource, JavaApiExtractionTask.Target.JAVA_REQUIRED, extractor, resourceDAO);
        taskRunnerService.scheduleTask(task);
        logger.debug("Java required API Extraction task scheduled for {}.", resource.getId());

        return resource;
    }

    /**
     * Ran by OSGi on bundle startup. Schedules indexing of unindexed resources stored in the database.
     * @throws Exception
     */
    protected void init() throws Exception {
        List<Resource> resources = resourceDAO.loadResourcesWithoutCategory(JavaApiExtractor.CATEGORY_PROVIDED, false);

        logger.debug("Bulk Java API Extraction task about to schedule for {} resources.", resources.size());
        Task task = new JavaApiExtractionTask("JAVA API - INIT:", resources, JavaApiExtractionTask.Target.JAVA_PROVIDED, extractor, resourceDAO);
        taskRunnerService.scheduleTask(task);
        logger.debug("Bulk Java API Extraction task scheduled for {} resources.", resources.size());

        resources = resourceDAO.loadResourcesWithoutCategory(JavaApiExtractor.CATEGORY_REQUIRED, false);

        logger.debug("Bulk Java API Extraction task about to schedule for {} resources.", resources.size());
        task = new JavaApiExtractionTask("JAVA API - INIT:", resources, JavaApiExtractionTask.Target.JAVA_REQUIRED, extractor, resourceDAO);
        taskRunnerService.scheduleTask(task);
        logger.debug("Bulk Java API Extraction task scheduled for {} resources.", resources.size());
    }

}
