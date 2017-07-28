package cz.zcu.kiv.crce.metadata.java.namespace;

import java.util.List;

import cz.zcu.kiv.crce.metadata.AttributeType;
import cz.zcu.kiv.crce.metadata.impl.ListAttributeType;
import cz.zcu.kiv.crce.metadata.impl.SimpleAttributeType;

/**
 * Namespace for capability describing subtyping hierarchy in a resource group.
 *
 * Date: 24.7.17
 *
 * @author Jakub Danek
 */
public class NsCrceJavaTypeView {

    public static final String NAMESPACE__CRCE_VIEW_IDENTITY = "crce.view.java-type-identity";

    public static final AttributeType<String> ATTRIBUTE__CHILD = new SimpleAttributeType<>("child", String.class);;

    public static final AttributeType<List<String>> ATTRIBUTE__RESOURCE_PARENTS = new ListAttributeType("parents");

}
