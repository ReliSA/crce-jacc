package cz.zcu.kiv.crce.metadata.java.internal.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import cz.zcu.kiv.jacc.javatypes.JClass;
import cz.zcu.kiv.jacc.javatypes.JField;
import cz.zcu.kiv.jacc.javatypes.JMethod;
import cz.zcu.kiv.jacc.javatypes.JType;

import cz.zcu.kiv.crce.metadata.Attribute;
import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.MetadataFactory;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaClassAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaFieldAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaMethodAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaPackageAPI;
import cz.zcu.kiv.crce.metadata.java.parser.JavaMetadataParser;

/**
 * Recursive {@link JavaMetadataParser} implementation.
 *
 * Date: 24.11.15
 *
 * @author Jakub Danek
 */
@ParametersAreNonnullByDefault
public class RecursiveJavaMetadataParser implements JavaMetadataParser {

    private MetadataFactory metadataFactory;   //DI

    @Override
    public Set<Capability> parse(Set<JClass> classes) {
        Map<String, Capability> packages = new HashMap<>();

        String pckgName;
        Capability pckg;
        for (JClass jClass : classes) {
            pckgName = jClass.getPackage().getName();
            pckg = packages.get(pckgName);
            if(pckg == null) {
                pckg = createPackage(pckgName);
                packages.put(pckgName, pckg);
            }

            pckg.addChild(parseClass(jClass));
        }

        return new HashSet<>(packages.values());
    }

    @Override
    public Set<JClass> map(Set<Capability> capabilities) {
        throw new UnsupportedOperationException("Not supported yet!");
    }

    /**
     *
     * @param jClass class to be transformed into capability
     * @return capability representing the given class
     */
    private Capability parseClass(JClass jClass) {
        boolean firstLevel = jClass.getOuterClass() == null;
        Capability cap;
        if(firstLevel) {
            cap = metadataFactory.createCapability(NsCrceJavaClassAPI.NAMESPACE__JAVA_CLASS);
        } else {
            cap = metadataFactory.createCapability(NsCrceJavaClassAPI.NAMESPACE__JAVA_INNER_CLASS);
        }

        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaClassAPI.NAME, jClass.getShortName()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaClassAPI.ABSTRACT, jClass.getModifiers().isAbstract()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaClassAPI.ENUM, jClass.isEnum()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaClassAPI.INTERFACE, jClass.isInterface()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaClassAPI.ANNOTATION, jClass.isAnnotation()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaClassAPI.FINAL, jClass.getModifiers().isFinal()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaClassAPI.STATIC, jClass.getModifiers().isStatic()));

        for (JMethod jMethod : jClass.getMethods()) {
            cap.addChild(parseMethod(jMethod));
        }

        for (JField jField : jClass.getFields()) {
            cap.addChild(parseField(jField));
        }

        for (JClass aClass : jClass.getInnerClasses()) {
            cap.addChild(parseClass(aClass));
        }

        return cap;
    }

    /**
     *
     * @param jField field to be transformed into capability
     * @return capability representing the given field
     */
    private Capability parseField(JField jField) {
        Capability cap = metadataFactory.createCapability(NsCrceJavaFieldAPI.NAMESPACE__JAVA_FIELD);

        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaFieldAPI.NAME, jField.getName()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaFieldAPI.FINAL, jField.getModifiers().isFinal()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaFieldAPI.STATIC, jField.getModifiers().isStatic()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaFieldAPI.VOLATILE, jField.getModifiers().isVolatile()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaFieldAPI.TYPE, jField.getType().getName()));

        return cap;
    }

    /**
     *
     * @param jMethod method to be transformed into capability
     * @return capability representing the given method
     */
    private Capability parseMethod(JMethod jMethod) {
        Capability cap = metadataFactory.createCapability(NsCrceJavaMethodAPI.NAMESPACE__JAVA_METHOD);

        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.NAME, jMethod.getName()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.CONSTRUCTOR, jMethod.isConstructor()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.ABSTRACT, jMethod.getModifiers().isAbstract()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.FINAL, jMethod.getModifiers().isFinal()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.STATIC, jMethod.getModifiers().isStatic()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.SYNCHRONIZED, jMethod.getModifiers().isSynchronized()));
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.RETURN_TYPE, jMethod.getReturnType().getName()));
        List<String> tmp = new LinkedList<>();
        for (JType jType : jMethod.getParameterTypes()) {
            tmp.add(jType.getName());
        }
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.PARAM_TYPES, tmp));
        tmp = new LinkedList<>();
        for (JType jType : jMethod.getExceptionTypes()) {
            tmp.add(jType.getName());
        }
        cap.setAttribute(metadataFactory.createAttribute(NsCrceJavaMethodAPI.EXCEPTIONS, tmp));

        return cap;
    }

    /**
     *
     * @param packageName name of the package for which the capability is created
     * @return capability representing the given package
     */
    private Capability createPackage(String packageName) {
        Capability cap = metadataFactory.createCapability(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE);
        Attribute<String> att = metadataFactory.createAttribute(NsCrceJavaPackageAPI.NAME, packageName);
        cap.setAttribute(att);
        return cap;
    }
}
