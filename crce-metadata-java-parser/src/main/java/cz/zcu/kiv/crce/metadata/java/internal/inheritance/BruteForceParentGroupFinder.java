package cz.zcu.kiv.crce.metadata.java.internal.inheritance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import cz.zcu.kiv.crce.metadata.Attribute;
import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaClassAPI;
import cz.zcu.kiv.crce.metadata.java.namespace.NsCrceJavaPackageAPI;

/**
 * Brute force implementation of the parent group finder.
 *
 * Checks all possible combinations of the candidate selection using simple
 * backtracking algorithm.
 *
 * Date: 26.7.17
 *
 * @author Jakub Danek
 */
public class BruteForceParentGroupFinder implements ParentGroupFinder {

    private static final int PROVIDED = 1;

    @Override
    public List<Set<Resource>> find(List<String> requiredClasses, List<Resource> candidates) {
        List<Set<Resource>> result = new ArrayList<>();

        Stack<SearchState> backtracking = new Stack<>();
        backtracking.push(SearchState.INIT);

        Set<Resource> singleResultSet;
        while (!backtracking.isEmpty()) {
            singleResultSet = backTrackingSearch(requiredClasses, candidates, backtracking);
            if(singleResultSet != null) {
                result.add(singleResultSet);
            }
        }

        return result;

    }

    /**
     * Using the backtracking stack, searches through the candidates set in a loop until it finds a set of resources
     * which provide all the required classes.
     *
     * On the first call, the backtracking stack must contain single item with resourceIndex set to -1.
     *
     * In order to search through all candidates combinations, the backtracking stack must be kept between individual calls.
     *
     * @param requiredClasses set of required classes' names
     * @param candidates list of possible candidates providing the classes
     * @param backtracking stack containing information on previously found suitable resources together with search loop
     *                     index for easy backtracking when end of loop is reached.
     * @return group of resources forming a single viable solution. Call this method until it returns null (no more solutions).
     */
    private Set<Resource> backTrackingSearch(List<String> requiredClasses, List<Resource> candidates, Stack<SearchState> backtracking) {
        SearchState init = backtracking.pop();
        int index = init.resourceIndex + 1; //next

        List<Resource> resultSet;
        int[] providedClasses;
        if(backtracking.isEmpty()) {
            resultSet = new ArrayList<>();
            providedClasses = new int[requiredClasses.size()];
        } else {
            init = backtracking.peek();
            //copy to ensure history is not modified
            resultSet = new ArrayList<>(init.resources);
            providedClasses = Arrays.copyOf(init.providedClasses, init.providedClasses.length);
        }

        if(findResult(index, providedClasses, resultSet, requiredClasses, candidates, backtracking)) {
            return new HashSet<>(resultSet);
        }

        return null;
    }


    /**
     * Searches through candidate space starting at resourceIndex until it finds a suitable result or reaches end of
     * space.
     *
     * @param resourceIndex starting index in the list of candidates
     * @param providedClasses array holding information on which of the classes are already provided by the
     *                        resources in the result list.
     * @param result list of resources forming the resulting group
     * @param requiredClasses list of required classes' names
     * @param candidates list of candidates on provision of the required classes
     * @param backtracking stack holding backtracking information
     * @return true if result is found, false otherwise
     */
    private boolean findResult(int resourceIndex, int[] providedClasses, List<Resource> result, List<String> requiredClasses, List<Resource> candidates, Stack<SearchState> backtracking) {
        Resource resource;

        for(int i = resourceIndex; i < candidates.size(); i++) {
            resource = candidates.get(i);

            int[] newly = testResource(resource, providedClasses, requiredClasses);
            if(newly != null && mergeResult(i, newly, providedClasses, result, resource, backtracking)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Updates state holders if a suitable resource is found to expand the solution.
     *
     * @param resourceIndex index of the resource
     * @param newly array holding information on which classes are provided by the resource
     * @param providedClasses array holding information on which classes are provided by the resources in the result list
     * @param result list of resources forming the solution
     * @param resource to be added to the solution
     * @param backtracking stack holding information for backtracking
     * @return true if solution is complete (all classes are provided), false otherwise
     */
    private boolean mergeResult(int resourceIndex, int[] newly, int[] providedClasses, List<Resource> result, Resource resource, Stack<SearchState> backtracking) {
        boolean done = true;
        for (int i = 0; i < newly.length; i++) {
            providedClasses[i] += newly[i];
            done = done && providedClasses[i] == PROVIDED;
        }

        result.add(resource);
        backtracking.push(new SearchState(resourceIndex, providedClasses, result));

        return done;
    }

    /**
     * Tests whether the resource adds to the solution
     *
     * @param resource checked resource
     * @param providedClasses array representing information on which classes are already provided
     *                        by other resources in the solution
     * @param requiredClasses list of required classes
     * @return array representing which classes are provided by the resource or null, if the resource doesn't
     *         fit the solution for some reason (e.g. provides a class which is already covered in the solution)
     */
    private int[] testResource(Resource resource, int[] providedClasses, List<String> requiredClasses) {
        int[] newlyProvided = new int[providedClasses.length];

        for (int j = 0; j < requiredClasses.size(); j++) {
            boolean provides = checkProvidedClass(resource, requiredClasses.get(j));
            boolean alreadyProvided = providedClasses[j] == PROVIDED;
            if(provides && !alreadyProvided) {
                newlyProvided[j] = PROVIDED;
            } else if(provides) {
                return null;
            }
        }

        return newlyProvided;
    }


    /**
     * Checks whether the resource provides the given class.
     *
     * @param value resource to be checked
     * @param fqnClazz the classname
     * @return true if provides, false otherwise
     */
    private boolean checkProvidedClass(Resource value, String fqnClazz) {
        String pckg = Utils.getPackage(fqnClazz);
        String clazz = Utils.getClassName(fqnClazz);
        for (Capability pckgCap : value.getRootCapabilities(NsCrceJavaPackageAPI.NAMESPACE__JAVA_PACKAGE)) {
            Attribute<String> name = pckgCap.getAttribute(NsCrceJavaPackageAPI.NAME);
            if(name != null && Objects.equals(name.getValue(), pckg)) {
                for (Capability capability : pckgCap.getChildren()) {
                    if(Objects.equals(NsCrceJavaClassAPI.NAMESPACE__JAVA_CLASS, capability.getNamespace())) {
                        name = capability.getAttribute(NsCrceJavaClassAPI.NAME);

                        if(name != null && Objects.equals(name.getValue(), clazz)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
