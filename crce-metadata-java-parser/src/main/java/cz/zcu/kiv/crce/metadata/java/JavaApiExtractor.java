package cz.zcu.kiv.crce.metadata.java;

import cz.zcu.kiv.crce.metadata.Resource;

/**
 * Interface for service used to extract Java API (both provided and required)
 * from resources stored within CRCE.
 *
 * Date: 6.1.17
 *
 * @author Jakub Danek
 */
public interface JavaApiExtractor {

    String CATEGORY_PROVIDED = "java-api";
    String CATEGORY_REQUIRED = "java-api-required";

    /**
     * Retrieve resource's provided (non-private) Java API
     * from the resource's binary and add it as capabilities to the resource
     * metadata.
     * @param resource resource to be processed
     * @return updated resource metadata
     */
    Resource extractProvidedJavaApi(Resource resource);

    /**
     * Retrieve resource's provided (non-private) OSGi API
     * from the resource's binary and add it as capabilities to the resource
     * metadata.
     * @param resource resource to be processed
     * @return updated resource metadata
     */
    Resource extractProvidedOsgiApi(Resource resource);

    /**
     * Retrieve resource's required (imported) Java API
     * from the resource's binary and add it as requirements to the resource
     * metadata.
     * @param resource resource to be processed
     * @return updated resource metadata
     */
    Resource extractRequiredJavaApi(Resource resource);

    /**
     * Retrieve resource's required (non-private) OSGi API
     * from the resource's binary and add it as requirements to the resource
     * metadata.
     * @param resource resource to be processed
     * @return updated resource metadata
     */
    Resource extractRequiredOsgiApi(Resource resource);
}
