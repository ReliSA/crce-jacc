package cz.zcu.kiv.crce.metadata.java.internal.inheritance;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import cz.zcu.kiv.crce.metadata.Attribute;
import cz.zcu.kiv.crce.metadata.AttributeType;
import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.MetadataFactory;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaClassAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaFieldAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaMethodAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaPackageAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaTypeView;
import cz.zcu.kiv.crce.metadata.namespace.NsCrceIdentity;
import cz.zcu.kiv.crce.metadata.service.MetadataService;

/**
 * This class takes child component and parent providers and
 * builds a view out of them.
 *
 * Contains logic and rules for capability merging so that it would make
 * sense in the Java typing world.
 *
 * Date: 26.7.17
 *
 * @author Jakub Danek
 */
public class JavaTypeViewBuilder {

    private MetadataFactory metadataFactory;
    private MetadataService metadataService;

    /**
     * Builds resource group for java typing case including
     * the capability merge.
     *
     *
     * @param child  resource containing subclasses
     * @param parents resources containing parent classes
     * @return
     */
    public Resource buildView(Resource child, Set<Resource> parents) {
        Resource[] resources = new Resource[parents.size() + 1];
        resources = parents.toArray(resources);
        resources[parents.size()] = child;

        Resource view = metadataFactory.createResourceView(resources);
        view = updateIdentity(view, child);
        view = addJavaTypeViewCapability(view, child, parents);
        view = mergeJavaCapabilities(view, child, parents);

        return view;
    }

    /**
     * Update's view identity capability based on child resource.
     * @param view   the resource group
     * @param child the child resource
     * @return the updated view
     */
    private Resource updateIdentity(Resource view, Resource child) {
        Capability identityCap = metadataService.getIdentity(view);
        Capability childIdentity = metadataService.getIdentity(child);
        Attribute<URI> uriAt = childIdentity.getAttribute(NsCrceIdentity.ATTRIBUTE__URI);
        if(uriAt != null) {
            identityCap.setAttribute(metadataFactory.cloneAttribute(uriAt));
        }

        return view;
    }

    /**
     * Merges java related capabilities from child and parents into the view.
     *
     * @param view resulting resource group
     * @param child child resource (containing subclasses)
     * @param parents parent resources (containing parent classes)
     * @return the view
     */
    private Resource mergeJavaCapabilities(Resource view, Resource child, Set<Resource> parents) {
        Map<String, Capability> packages = new HashMap<>();
        Map<String, Capability> classes = new HashMap<>();

        mergeParentViews(parents, packages, classes);

        addChildView(child, packages, classes);

        for (Capability capability : packages.values()) {
            metadataService.addRootCapability(view, capability);
        }
        
        return view;
    }

    /**
     * Adds child (src) resource capabilities to the preprocessed list of joined capabilities of parent resources.
     *
     * @param src child resource, to be merged to parent resource's capabilities
     * @param packages preprocessed map of group's capabilities extracted from parent resources. Key is package name,
     *                 value is the capability itself
     * @param classes preprocessed map of class capabilities for easy access to classes already present in the group
     *                view. Key is fully qualified class name, value is the corresponding class capability
     */
    private void addChildView(Resource src, Map<String, Capability> packages, Map<String, Capability> classes) {
        List<Capability> pckgs;
        String name;
        Capability viewPckg;

        pckgs = src.getRootCapabilities(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE);
        for (Capability pckg : pckgs) {
            name = getName(pckg, NsCrceJavaPackageAPI.NAME);
            if(name != null) {
                viewPckg = packages.get(name);
                if(viewPckg == null) {
                    viewPckg = metadataFactory.createCapability(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE);
                    viewPckg.setAttribute(metadataFactory.createAttribute(NsCrceJavaPackageAPI.NAME, name));
                    packages.put(name, viewPckg);
                }

                processChildClasses(pckg, viewPckg, classes);
            }
        }
    }

    /**
     * Merges two package capabilities.
     *
     * @param srcPckg package capability from which information is merged into the destPckg
     * @param destPckg package capability into which the srcPckg is merged
     * @param classes preprocessed map of class capabilities for easy access to classes already present in the group
     *                view. Key is fully qualified class name, value is the corresponding class capability
     */
    private void processChildClasses(Capability srcPckg, Capability destPckg, Map<String, Capability> classes) {
        List<Capability> children = srcPckg.getChildren();

        Capability newCap, parent;
        for (Capability child : children) {
            if(Objects.equals(NsCrceJavaClassAPI.NAMESPACE__JAVA_CLASS, child.getNamespace())) {
                //TODO check for duplicates and skip view altogether if any
                newCap = metadataFactory.cloneCapability(child);
                Set<String> superClasses = getSuperClasses(child);

                for (String superClass : superClasses) {
                    parent = classes.get(superClass);
                    if(parent == null) throw new RuntimeException("Someone forgot to give me all required classes!");

                    mergeClasses(newCap, parent);
                }

                destPckg.addChild(newCap);
            }
        }
    }

    /**
     * Merge two class capabilities
     * @param dest destination capability
     * @param src source capability
     */
    private void mergeClasses(Capability dest, Capability src) {
        Map<String, Capability> index = buildClassMemberIndex(dest);
        List<Capability> children = src.getChildren();

        String name;
        Capability member;
        for (Capability child : children) {
            name = getMemberName(child);
            if(name == null) continue;

            member = index.get(name);
            if(member != null) continue; //subclass (dest) already contains the member

            dest.addChild(metadataFactory.cloneCapability(child));
        }
    }

    /**
     *
     * @param clazz capability representing the class for which the member index is built
     * @return map of class members (fields, methods), key is fully qualified name of class '.' member name
     */
    private Map<String, Capability> buildClassMemberIndex(Capability clazz) {
        Map<String, Capability> index = new HashMap<>();
        List<Capability> children = clazz.getChildren();

        String name;
        for (Capability child : children) {
            name = getMemberName(child);
            if(name == null) continue;

            index.put(name, child);
        }
        return index;
    }

    /**
     *
     * @param child capability representing the class for which names of super classes and interfaces are
     *              extracted
     * @return set of super class and interfaces for the class
     */
    private Set<String> getSuperClasses(Capability child) {
        Set<String> superclasses = new HashSet<>();
        Attribute<String> scat = child.getAttribute(NsCrceJavaClassAPI.SUPERCLASS);
        if(scat != null) {
            superclasses.add(scat.getValue());
        }

        Attribute<List> ifat = child.getAttribute(NsCrceJavaClassAPI.INTERFACES);
        if(ifat != null) {
            superclasses.addAll(ifat.getValue());
        }

        return superclasses;
    }

    /**
     * Merges java capabilities of the given resources.
     *
     * Resulting package capabilities are stored in the packages map for easy access afterwards.
     * Processed class capabilities are stored in the classes map for easy access afterwards.
     *
     * @param resources list of resources to merge
     * @param packages resulting packages (capabilities) map
     * @param classes map of processed classes (capabilities)
     */
    private void mergeParentViews(Set<Resource> resources, Map<String, Capability> packages, Map<String, Capability> classes) {
        List<Capability> pckgs;
        String name;
        Capability viewPckg;
        for (Resource resource : resources) {
            pckgs = resource.getRootCapabilities(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE);
            for (Capability pckg : pckgs) {
                name = getName(pckg, NsCrceJavaPackageAPI.NAME);
                if(name != null) {
                    viewPckg = packages.get(name);
                    if(viewPckg == null) {
                        viewPckg = softCopyPackage(pckg);
                        packages.put(name, viewPckg);
                    }
                    
                    mergePackageCapability(viewPckg, pckg, name, classes);
                }
            }
        }
    }

    /**
     * Merges two package capabilities. This method presumes that the capabilities dont contains same classes,
     * so they are indexed and added, but no merging takes place at this point.
     *
     * @param dest package capability into which the src is merged
     * @param src package capability from which information is merged into the dest
     * @param pckgName name of the package
     * @param classes preprocessed map of class capabilities for easy access to classes already present in the group
     *                view. Key is fully qualified class name, value is the corresponding class capability
     */
    private void mergePackageCapability(Capability dest, Capability src, String pckgName, Map<String, Capability> classes) {
        List<Capability> children = src.getChildren();

        String className, fqName;
        Capability newCap;
        for (Capability child : children) {
            if(Objects.equals(NsCrceJavaClassAPI.NAMESPACE__JAVA_CLASS, child.getNamespace())) {
                className = getName(child, NsCrceJavaClassAPI.NAME);
                if(className == null) continue; //broken class view, skip, TODO log

                fqName = Utils.getFQClassName(pckgName, className);
                if(classes.containsKey(fqName)) continue; //class image already processed through other view, no need to add again

                newCap = metadataFactory.cloneCapability(child);
                dest.addChild(newCap);
                classes.put(fqName, newCap);
                indexInnerClasses(newCap, fqName, classes);
            }
        }
    }

    /**
     * Searches for inner classes within class capability. If any is found, it is added to the classes index.
     *
     * @param classCap capability representing a class
     * @param fqParentName fully qualified name of the parent
     * @param classes preprocessed map of class capabilities for easy access to classes already present in the group
     *                view. Key is fully qualified class name, value is the corresponding class capability
     */
    private void indexInnerClasses(Capability classCap, String fqParentName, Map<String, Capability> classes) {
        List<Capability> children = classCap.getChildren();

        String className, fqName;
        for (Capability child : children) {
            if(Objects.equals(NsCrceJavaClassAPI.NAMESPACE__JAVA_INNER_CLASS, child.getNamespace())) {
                className = getName(child, NsCrceJavaClassAPI.NAME);
                if(className == null) continue; //broken class view, skip, TODO log

                //this is inner class, for sure hasnt been processed earlier (if yes, the check wont pass on its parent class)
                fqName = Utils.getFQClassName(fqParentName, className);
                classes.put(fqName, child);
            }
        }
    }

    /**
     * Creates soft copy of a package capability - copying only its attributes, but no child capabilities.
     *
     * //TODO move this to metadataService or metadataFactory
     *
     * @param pckg package capability to copy
     * @return new capability with same attributes
     */
    private Capability softCopyPackage(Capability pckg) {
        Capability newPckg = metadataFactory.createCapability(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE);
        for (Attribute<?> at : pckg.getAttributes()) {
            newPckg.setAttribute(metadataFactory.cloneAttribute(at));
        }

        return newPckg;
    }

    /**
     * Extracts name of the class member based on its type.
     * @param clazz capability representing a class member.
     * @return  name of the member
     */
    private String getMemberName(Capability clazz) {
        if(Objects.equals(NsCrceJavaFieldAPI.NAMESPACE__JAVA_FIELD, clazz.getNamespace())) {

            return getName(clazz, NsCrceJavaFieldAPI.NAME);

        } else if(Objects.equals(NsCrceJavaMethodAPI.NAMESPACE__JAVA_METHOD, clazz.getNamespace())) {

            return getName(clazz, NsCrceJavaMethodAPI.NAME);

        } else if(Objects.equals(NsCrceJavaClassAPI.NAMESPACE__JAVA_INNER_CLASS, clazz.getNamespace())) {

            return getName(clazz, NsCrceJavaClassAPI.NAME);

        } else {
            return null;
        }
    }

    /**
     * TODO this is probably replaceable by metadataService
     *
     * @param capability capability for which to get the attribute value
     * @param type attribute type
     * @return string value of the attribute or null if not present
     */
    private String getName(Capability capability, AttributeType<String> type) {
        Attribute<String> nameAt = capability.getAttribute(type);
        return nameAt != null ? nameAt.getValue() : null;
    }

    /**
     * Creates a capability describing the subtyping direcition of this resource group and
     * adds it to the resource group view
     * @param view the resource group view
     * @param child child resource
     * @param parents parent resources
     * @return the view 
     */
    private Resource addJavaTypeViewCapability(Resource view, Resource child, Set<Resource> parents) {
        Capability typeCapability = metadataFactory.createCapability(NsCrceJavaTypeView.NAMESPACE__CRCE_VIEW_IDENTITY);
        typeCapability.setAttribute(NsCrceJavaTypeView.ATTRIBUTE__CHILD, child.getId());

        List<String> parentIds = new ArrayList<>();
        for (Resource parent : parents) {
            parentIds.add(parent.getId());
        }
        typeCapability.setAttribute(NsCrceJavaTypeView.ATTRIBUTE__RESOURCE_PARENTS, parentIds);

        metadataService.addRootCapability(view, typeCapability);

        return view;
    }
}
