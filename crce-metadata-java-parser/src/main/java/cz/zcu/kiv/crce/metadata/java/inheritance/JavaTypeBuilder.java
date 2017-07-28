package cz.zcu.kiv.crce.metadata.java.inheritance;

import java.util.List;

import cz.zcu.kiv.crce.metadata.Resource;

/**
 * In Java, in case of inheritance, the complete type is constructed at runtime
 * based on binary versions of classes present on the classpath.
 * In cases when are parent classes/interfaces located in different JAR
 * than their subclass/implementation, the resulting subtype may differ
 * based on which version of the jar containing parent class is present in the bundle.
 *
 * Interface for service creating resource groups based on java subtyping rules.
 *
 * Date: 24.7.17
 *
 * @author Jakub Danek
 */
public interface JavaTypeBuilder {

    /**
     *
     *
     * @param src resource for which we want to build all possible subtyping combinations
     * @return list of resources representing groups
     */
    List<Resource> buildTypeViews(Resource src);

}
