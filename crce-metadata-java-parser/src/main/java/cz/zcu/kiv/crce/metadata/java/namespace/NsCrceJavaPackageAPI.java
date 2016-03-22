package cz.zcu.kiv.crce.metadata.java.namespace;

import cz.zcu.kiv.crce.metadata.AttributeType;
import cz.zcu.kiv.crce.metadata.impl.SimpleAttributeType;

/**
 * Namespace definition of Java Package Capability representation.
 *
 * Date: 24.11.15
 *
 * @author Jakub Danek
 */
public class NsCrceJavaPackageAPI {

    public static final String NAMESPACE__JAVA_PACKAGE = "crce.api.java.package";

    /*
    ################ PACKAGE ATTRIBUTES #############################
     */

    public static final AttributeType<String> NAME = new SimpleAttributeType<>("name", String.class);

}
