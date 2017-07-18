package cz.zcu.kiv.crce.metadata.java.namespace;

import java.util.List;

import cz.zcu.kiv.crce.metadata.AttributeType;
import cz.zcu.kiv.crce.metadata.impl.SimpleAttributeType;

/**
 * Namespace definition of Java Class Capability representation.
 *
 * Date: 24.11.15
 *
 * @author Jakub Danek
 */
public class NsCrceJavaClassAPI {

    public static final String NAMESPACE__JAVA_CLASS = "crce.api.java.class";

    public static final String NAMESPACE__JAVA_INNER_CLASS = "crce.api.java.class.inner";

    /*
    ################ CLASS ATTRIBUTES #############################
     */

    public static final AttributeType<String> NAME = new SimpleAttributeType<>("name", String.class);

    public static final AttributeType<String> SUPERCLASS = new SimpleAttributeType<>("superclass", String.class);

    public static final AttributeType<List> INTERFACES = new SimpleAttributeType<>("interfaces", List.class);

    public static final AttributeType<Boolean> ABSTRACT = new SimpleAttributeType<>("abstract", Boolean.class);

    public static final AttributeType<Boolean> INTERFACE = new SimpleAttributeType<>("interface", Boolean.class);

    public static final AttributeType<Boolean> ANNOTATION = new SimpleAttributeType<>("annotation", Boolean.class);

    public static final AttributeType<Boolean> ENUM = new SimpleAttributeType<>("enum", Boolean.class);

    public static final AttributeType<Boolean> FINAL = new SimpleAttributeType<>("final", Boolean.class);

    public static final AttributeType<Boolean> STATIC = new SimpleAttributeType<>("static", Boolean.class);
}
