package cz.zcu.kiv.crce.metadata.java.namespace;

import cz.zcu.kiv.crce.metadata.AttributeType;
import cz.zcu.kiv.crce.metadata.impl.SimpleAttributeType;

/**
 * Namespace definition of Java Field Capability representation.
 *
 * Date: 24.11.15
 *
 * @author Jakub Danek
 */
public class NsCrceJavaFieldAPI {

    public static final String NAMESPACE__JAVA_FIELD = "crce.api.java.field";

    /*
    ################ FIELD ATTRIBUTES #############################
    */

    public static final AttributeType<String> NAME = new SimpleAttributeType<>("name", String.class);

    public static final AttributeType<Boolean> VOLATILE = new SimpleAttributeType<>("volatile", Boolean.class);

    public static final AttributeType<String> TYPE = new SimpleAttributeType<>("type", String.class);

    public static final AttributeType<Boolean> FINAL = new SimpleAttributeType<>("final", Boolean.class);

    public static final AttributeType<Boolean> STATIC = new SimpleAttributeType<>("static", Boolean.class);

}
