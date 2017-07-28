package cz.zcu.kiv.crce.metadata.java.internal;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.crce.concurrency.model.Task;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.dao.MetadataDao;
import cz.zcu.kiv.crce.metadata.java.JavaApiExtractor;
import cz.zcu.kiv.crce.metadata.java.inheritance.JavaTypeBuilder;
import cz.zcu.kiv.crce.metadata.service.MetadataService;

/**
 * Background task for creating all possible combinations of resources based on
 * subtyping rules.
 *
 * See {@link JavaTypeBuilder} for better description.
 *
 * Date: 24.7.17
 *
 * @author Jakub Danek
 */
public class JavaTypeViewBuilderTask extends Task<Object> {

    private static final Logger logger = LoggerFactory.getLogger(JavaTypeViewBuilderTask.class);

    private final MetadataDao metadataDao;
    private final MetadataService metadataService;
    private final JavaTypeBuilder typeBuilder;

    private final List<Resource> resources;

    public JavaTypeViewBuilderTask(String id, MetadataDao metadataDao, MetadataService metadataService, JavaTypeBuilder typeBuilder, Resource resource) {
        this(id, metadataDao, metadataService, typeBuilder, Collections.singletonList(resource));
    }

    public JavaTypeViewBuilderTask(String id, MetadataDao metadataDao, MetadataService metadataService, JavaTypeBuilder typeBuilder, List<Resource> resources) {
        super(id, "Builds resource tuples based on subclassing to create all possible combinations of subclass-superclass pairs.", "crce-java-api-extractor");
        
        this.metadataDao = metadataDao;
        this.metadataService = metadataService;
        this.typeBuilder = typeBuilder;
        this.resources = resources;
    }

    @Override
    protected Object run() throws Exception {
        logger.debug("Running with scissors!");
        for (Resource resource : resources) {
            synchronized (resource) {
                resource = metadataDao.loadResource(resource.getId(), true);
                if (resource == null) continue; //not very likely
                if (!metadataService.getCategories(resource).contains(JavaApiExtractor.CATEGORY_PROVIDED)) {
                    logger.debug("Resource {} doesnt have correct category!", resource.getId());
                    continue;
                }

                logger.debug("Starting to build views for resource {}", resource.getId());
                List<Resource> views = typeBuilder.buildTypeViews(resource);
                logger.debug("For resource {} I have built {} views.", resource.getId(), views.size());

                for (Resource view : views) {
                    metadataDao.saveResource(view);
                }
            }
        }

        return true;
    }
}
