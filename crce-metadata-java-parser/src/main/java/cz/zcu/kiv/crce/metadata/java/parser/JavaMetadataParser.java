package cz.zcu.kiv.crce.metadata.java.parser;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import cz.zcu.kiv.jacc.javatypes.JClass;

import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.Requirement;

/**
 * Service interface of the metadata parser - allows transforming
 * JaCC representations into capabilities and vice versa.
 *
 * Date: 24.11.15
 *
 * @author Jakub Danek
 */
@ParametersAreNonnullByDefault
public interface JavaMetadataParser {
    /**
     * Transform JaCC #classes into set of Capabilities.
     * @param classes set of classes to be parsed
     * @return respective capabilities
     */
    Set<Capability> parseIntoCapabilities(Set<JClass> classes);

    /**
     * Transform JaCC #classes into set of Requirements.
     * @param classes set of classes to be parsed
     * @return respective requirements
     */
    Set<Requirement> parseIntoRequirements(Set<JClass> classes);

    /**
     * .The mirror operation to the #parseIntoCapabilities method.
     * @param capabilities set of capabilities representing classes to be mapped
     * @return respective JClass instances
     */
    Set<JClass> mapCapabilities(Set<Capability> capabilities);

    /**
     * .The mirror operation to the #parseIntoRequirements method.
     * @param requirements set of requirements representing classes to be mapped
     * @return respective JClass instances
     */
    Set<JClass> mapRequirements(Set<Requirement> requirements);

}
