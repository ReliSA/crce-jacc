package cz.zcu.kiv.crce.metadata.java.namespace;

import java.util.List;

import cz.zcu.kiv.crce.metadata.AttributeType;
import cz.zcu.kiv.crce.metadata.impl.SimpleAttributeType;

/**
 * Namespace definition of Java Method Capability representation.
 *
 * Date: 24.11.15
 *
 * @author Jakub Danek
 */
public class NsCrceJavaMethodAPI {

    public static final String NAMESPACE__JAVA_METHOD = "crce.api.java.method";

    /*
    ################ METHOD ATTRIBUTES #############################
     */

    public static final AttributeType<String> NAME = new SimpleAttributeType<>("name", String.class);

    public static final AttributeType<Boolean> CONSTRUCTOR = new SimpleAttributeType<>("constructor", Boolean.class);

    public static final AttributeType<Boolean> ABSTRACT = new SimpleAttributeType<>("abstract", Boolean.class);

    public static final AttributeType<Boolean> FINAL = new SimpleAttributeType<>("final", Boolean.class);

    public static final AttributeType<Boolean> STATIC = new SimpleAttributeType<>("static", Boolean.class);

    public static final AttributeType<Boolean> SYNCHRONIZED = new SimpleAttributeType<>("synchronized", Boolean.class);

    public static final AttributeType<String> RETURN_TYPE = new SimpleAttributeType<>("returnType", String.class);

    public static final AttributeType<List> PARAM_TYPES = new SimpleAttributeType<>("paramTypes", List.class);

    public static final AttributeType<List> EXCEPTIONS = new SimpleAttributeType<>("exceptions", List.class);

}
