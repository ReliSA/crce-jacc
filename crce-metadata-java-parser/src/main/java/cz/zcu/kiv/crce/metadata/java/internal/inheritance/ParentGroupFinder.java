package cz.zcu.kiv.crce.metadata.java.internal.inheritance;

import java.util.List;
import java.util.Set;

import cz.zcu.kiv.crce.metadata.Resource;

/**
 * Interface for finding groups of resources which are capable of providing
 * parent classes.
 *
 * Date: 26.7.17
 *
 * @author Jakub Danek
 */
public interface ParentGroupFinder {

    /**
     * This method returns list of resource groups, from which each
     * group provides all of the required classes in compatible fashion.
     *
     * In java world that means e.g. that there is only one representation of
     * each of the classes in the whole group.
     *
     * Supposedly the returned list contains several options which can be used to provide
     * those classes.
     *
     *
     * @param requiredClasses list of required classes
     * @param candidates list of candidates as the classes providers
     * @return list resource groups which provide the classes
     */
    List<Set<Resource>> find(List<String> requiredClasses, List<Resource> candidates);
}
