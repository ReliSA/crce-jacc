package cz.zcu.kiv.crce.metadata.java.internal;

import java.io.File;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.jacc.javatypes.JBlackBoxComponent;
import cz.zcu.kiv.jacc.loader.JClassLoaderFacade;

import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.Requirement;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.java.JavaApiExtractor;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaPackageAPI;
import cz.zcu.kiv.crce.metadata.java.parser.JavaMetadataParser;
import cz.zcu.kiv.crce.metadata.service.MetadataService;

/**
 * JavaApiExtractor implementation using JaCC to get the API metadata.
 *
 * Date: 6.1.17
 *
 * @author Jakub Danek
 */
public class JaccJavaApiExtractor implements JavaApiExtractor {

    private static final Logger logger = LoggerFactory.getLogger(JaccJavaApiExtractor.class);

    private JClassLoaderFacade classLoader = JClassLoaderFacade.JAR_MEMORY_FACADE;
    private JavaMetadataParser metadataParser;
    private MetadataService metadataService;

    @Override
    public Resource extractProvidedJavaApi(Resource resource) {
        logger.debug("Extracting Provided Java API for bundle id: {}, name: {}", resource.getId(), metadataService.getExternalId(resource));
        try {
            JBlackBoxComponent API = classLoader.getAPI(new File(metadataService.getUri(resource)));

            Set<Capability> apiCapabilities = metadataParser.parseIntoCapabilities(API.getExports());
            for (Capability apiCapability : apiCapabilities) {
                metadataService.addRootCapability(resource, apiCapability);
            }

            metadataService.addCategory(resource, JavaApiExtractor.CATEGORY_PROVIDED);
            logger.debug("Successfully processed bundle: {}", resource.getId());
        } catch (Exception e) {
            logger.error("Unable to process resource file for the resource: {}", resource.getId());
            logger.error("Exception thrown: ",e);
        }

        return resource;
    }

    @Override
    public Resource extractProvidedOsgiApi(Resource resource) {
        return resource;
    }

    @Override
    public Resource extractRequiredJavaApi(Resource resource) {
        logger.debug("Extracting Required Java API for bundle id: {}, name: {}", resource.getId(), metadataService.getExternalId(resource));
        try {
            JBlackBoxComponent API = classLoader.getAPI(new File(metadataService.getUri(resource)));

            Set<Requirement> apiRequirements = metadataParser.parseIntoRequirements(API.getImports());
            for (Requirement apiRequirement : apiRequirements) {
                if(apiRequirement.getNamespace().equals(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE)) {
                    boolean system = apiRequirement.getAttributes(NsCrceJavaPackageAPI.NAME).get(0).getValue().startsWith("java");
                    if(system) continue;
                }
                metadataService.addRequirement(resource, apiRequirement);
            }

            metadataService.addCategory(resource, JavaApiExtractor.CATEGORY_REQUIRED);
            logger.debug("Successfully processed bundle: {}", resource.getId());
        } catch (Exception e) {
            logger.error("Unable to process resource file for the resource: {}", resource.getId());
            logger.error("Exception thrown: ",e);
        }

        return resource;
    }

    @Override
    public Resource extractRequiredOsgiApi(Resource resource) {
        return resource;
    }
}
