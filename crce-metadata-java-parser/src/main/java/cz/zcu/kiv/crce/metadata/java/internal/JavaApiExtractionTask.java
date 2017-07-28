package cz.zcu.kiv.crce.metadata.java.internal;

import java.util.Collections;
import java.util.List;

import cz.zcu.kiv.crce.concurrency.model.ChainableTask;
import cz.zcu.kiv.crce.concurrency.model.Task;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.dao.MetadataDao;
import cz.zcu.kiv.crce.metadata.java.JavaApiExtractor;
import cz.zcu.kiv.crce.metadata.java.inheritance.JavaTypeBuilder;
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
public class JavaApiExtractionTask extends ChainableTask<Object> {

    public enum Target {
        JAVA_PROVIDED,
        JAVA_REQUIRED,
        OSGI_PROVIDED,
        OSGI_REQUIRED
    }

    private List<Resource> resources;
    private MetadataDao resourceDAO;
    private JavaApiExtractor extractor;

    private MetadataService metadataService;
    private JavaTypeBuilder typeBuilder;

    private final Target target;

    /**
     * @param id       ID of the task, for tracking
     * @param resource resource for which the compatibility data shall be computed
     * @param typeBuilder
     * @param metadataService
     */
    public JavaApiExtractionTask(String id, Resource resource, Target target, JavaApiExtractor extractor, MetadataDao resourceDAO, MetadataService metadataService, JavaTypeBuilder typeBuilder) {
        this(id, Collections.singletonList(resource), target, extractor, resourceDAO, metadataService, typeBuilder);
    }

    public JavaApiExtractionTask(String id, List<Resource> resources, Target target, JavaApiExtractor extractor, MetadataDao resourceDAO, MetadataService metadataService, JavaTypeBuilder typeBuilder) {
        super(id, "Extracts java api from the given resource.", "crce-api-java-extractor");
        this.resources = resources;
        this.extractor = extractor;
        this.resourceDAO = resourceDAO;

        this.target = target;
        this.metadataService = metadataService;
        this.typeBuilder = typeBuilder;
    }

    @Override
    protected Task<Object> doWork() throws Exception {
        for (Resource resource : resources) {
            synchronized (resource) {
                resource = resourceDAO.loadResource(resource.getId(), true);
                resource = extractData(resource);
                if (resource == null) {
                    continue;
                }
                resourceDAO.saveResource(resource);
            }
        }

        return target == Target.JAVA_PROVIDED ? new JavaTypeViewBuilderTask("JAVA TYPE BUILD", resourceDAO, metadataService, typeBuilder, resources) : null;
    }

    private Resource extractData(Resource resource) {
        switch (target) {
            case JAVA_PROVIDED:
                return extractor.extractProvidedJavaApi(resource);
            case JAVA_REQUIRED:
                return extractor.extractRequiredJavaApi(resource);
            case OSGI_PROVIDED:
                return extractor.extractProvidedOsgiApi(resource);
            case OSGI_REQUIRED:
                return extractor.extractRequiredOsgiApi(resource);
            default:
                return null;
        }
    }


}
