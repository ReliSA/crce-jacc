package cz.zcu.kiv.crce.metadata.java.internal.inheritance;

/**
 * Utility methods for handling java naming conventions.
 *
 * Date: 26.7.17
 *
 * @author Jakub Danek
 */
class Utils {

    static String getPackage(String fqClassName) {
        return fqClassName.substring(0, fqClassName.lastIndexOf('.'));
    }

    static String getClassName(String fqClassName) {
        return fqClassName.substring(fqClassName.lastIndexOf('.') + 1);
    }

    static String getFQClassName(String pckg, String clazz) {
        return pckg != null ? pckg + "." + clazz : clazz;
    }
}
