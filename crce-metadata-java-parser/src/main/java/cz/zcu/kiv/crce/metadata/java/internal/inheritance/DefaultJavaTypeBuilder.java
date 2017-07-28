package cz.zcu.kiv.crce.metadata.java.internal.inheritance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cz.zcu.kiv.crce.metadata.Attribute;
import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.MetadataFactory;
import cz.zcu.kiv.crce.metadata.Requirement;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.java.inheritance.JavaTypeBuilder;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaClassAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaPackageAPI;
import cz.zcu.kiv.crce.repository.Store;

/**
 * This implementation of {@link JavaTypeBuilder} expects indexed java resource,
 * finds all its possible parent providers and builds possible resource combinations
 * which allow building a full type.
 *
 * Capabilities from parent classes are merged with the child class.
 *
 * Date: 26.7.17
 *
 * @author Jakub Danek
 */
public class DefaultJavaTypeBuilder implements JavaTypeBuilder {

    private MetadataFactory metadataFactory;
    private Store store;
    private ParentGroupFinder parentGroupFinder;
    private JavaTypeViewBuilder typeViewBuilder;

    @Override
    public List<Resource> buildTypeViews(Resource src) {
        List<Resource> views = new ArrayList<>();

        views.addAll(buildViewsAsChild(src));

        return views;
    }

    /**
     * Builds views in which the src component's classes are a child.
     * @param src child component
     * @return list of possible groups
     */
    private List<Resource> buildViewsAsChild(Resource src) {
        List<Resource> views = new ArrayList<>();

        //key - superclass on which some of the resource classes depend
        //values - list of classes from src which depend on the key superclass/interface
        List<String> requiredClasses = new ArrayList<>(extractRequiredClasses(src));
        if(!requiredClasses.isEmpty()) {
            Set<Requirement> requirements = classesToRequirements(requiredClasses);

            List<Resource> superProviders = store.getPossibleResources(requirements, true);

            List<Set<Resource>> groups = parentGroupFinder.find(requiredClasses, superProviders);

            for (Set<Resource> group : groups) {
                views.add(buildView(src, group));
            }
        }

        return views;
    }

    /**
     *
     * @param child
     * @param group
     * @return resource representing group of resources based on group and child parameters
     */
    private Resource buildView(Resource child, Set<Resource> group) {
        return typeViewBuilder.buildView(child, group);
    }


    /**
     * Creates repository query based on list of required classes.
     * @param classes list of classes
     * @return repository query in Requirement format
     */
    private Set<Requirement> classesToRequirements(List<String> classes) {
        Set<Requirement> requirements = new HashSet<>(classes.size());
        for (String clazz : classes) {
            requirements.add(classToRequirement(clazz));
        }

        return requirements;
    }

    /**
     * Creates a requirement based on class name
     * @param clazz wanted class
     * @return a requirement instance representing the class name query
     */
    private Requirement classToRequirement(String clazz) {
        Requirement pckg = metadataFactory.createRequirement(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE);
        pckg.addAttribute(NsCrceJavaPackageAPI.NAME, Utils.getPackage(clazz));

        Requirement cl = metadataFactory.createRequirement(NsCrceJavaClassAPI.NAMESPACE__JAVA_CLASS);
        cl.addAttribute(NsCrceJavaClassAPI.NAME, Utils.getClassName(clazz));
        pckg.addChild(cl);

        return pckg;
    }

    /**
     * Searches the resource's capabilities in order to create a list of all required classes.
     *
     * @param src the resource
     * @return set of required parent classes.
     */
    private Set<String> extractRequiredClasses(Resource src) {
        Set<String> result = new HashSet<>();
        List<Capability> packages = src.getRootCapabilities(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE);

        List<Capability> classes;
        for (Capability pckg : packages) {
            classes = pckg.getChildren();

            for (Capability subCap : classes) {
                if (Objects.equals(NsCrceJavaClassAPI.NAMESPACE__JAVA_CLASS, subCap.getNamespace())) {

                    result.addAll(extractRequiredClasses(subCap));

                }
            }
        }

        return result;
    }

    /**
     * Searches the capability in order to get a list of its superclasses.
     *
     * @param classCapability capability representing a single class
     * @return set of required parent classes.
     */
    private Set<String> extractRequiredClasses(Capability classCapability) {
        Set<String> result = new HashSet<>();

        Attribute<String> superAtt = classCapability.getAttribute(NsCrceJavaClassAPI.SUPERCLASS);
        if(superAtt != null) {
            result.add(superAtt.getValue());
        }
        Attribute<List> interfaceAtt = classCapability.getAttribute(NsCrceJavaClassAPI.INTERFACES);
        if(interfaceAtt != null) {
            result.addAll(interfaceAtt.getValue());
        }

        for (Capability subCap : classCapability.getChildren()) {
            if(Objects.equals(NsCrceJavaClassAPI.NAMESPACE__JAVA_INNER_CLASS, subCap.getNamespace())) {
                result.addAll(extractRequiredClasses(subCap));
            }
        }

        return result;
    }

}
