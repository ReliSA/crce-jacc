package cz.zcu.kiv.crce.metadata.java.internal.inheritance;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cz.zcu.kiv.crce.metadata.Resource;

/**
 * Holds state of resource group search.
 *
 * Used to allow backatracking to earlier stages of the search.
 *
 * Date: 26.7.17
 *
 * @author Jakub Danek
 */
class SearchState {

    static final SearchState INIT = new SearchState(-1, new int[0], new LinkedList<Resource>());
    
    final int resourceIndex;
    final int[] providedClasses;
    final List<Resource> resources;

    SearchState(int resourceIndex, int[] providedClasses, List<Resource> resources) {
        this.resourceIndex = resourceIndex;
        this.providedClasses = Arrays.copyOf(providedClasses, providedClasses.length);
        this.resources = Collections.unmodifiableList(resources);
    }
}
