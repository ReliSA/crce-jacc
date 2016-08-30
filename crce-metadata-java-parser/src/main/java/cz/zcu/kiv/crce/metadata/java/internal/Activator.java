package cz.zcu.kiv.crce.metadata.java.internal;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cz.zcu.kiv.crce.metadata.MetadataFactory;
import cz.zcu.kiv.crce.metadata.dao.MetadataDao;
import cz.zcu.kiv.crce.metadata.java.internal.parser.RecursiveJavaMetadataParser;
import cz.zcu.kiv.crce.metadata.java.parser.JavaMetadataParser;
import cz.zcu.kiv.crce.metadata.service.MetadataService;
import cz.zcu.kiv.crce.plugin.Plugin;

/**
 * Activator for the CRCE Metadata Java Parser bundle.
 *
 * Date: 17.11.13
 *
 * @author Jakub Danek
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Injected by dependency manager.")
public class Activator extends DependencyActivatorBase {

    private static Activator instance;

    public static Activator instance() {
        return instance;
    }

     /**
     * Initialize the dependency manager. Here you can add all components and their dependencies.
     * If something goes wrong and you do not want your bundle to be started, you can throw an
     * exception. This exception will be passed on to the <code>start()</code> method of the
     * bundle activator, causing the bundle not to start.
     *
     * @param context the bundle context
     * @param manager the dependency manager
     * @throws Exception if the initialization fails
     */
    @Override
    @edu.umd.cs.findbugs.annotations
            .SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Dependency manager workaround.")
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        instance = this;

        manager.add(createComponent()
                .setInterface(JavaMetadataParser.class.getName(), null)
                .setImplementation(RecursiveJavaMetadataParser.class)
                .add(createServiceDependency().setService(MetadataFactory.class).setRequired(true)));

        manager.add(createComponent()
                .setInterface(Plugin.class.getName(), null)
                .setImplementation(JavaApiIndexer.class)
                .add(createServiceDependency().setService(MetadataService.class).setRequired(true))
                .add(createServiceDependency().setService(MetadataDao.class).setRequired(true))
                .add(createServiceDependency().setService(JavaMetadataParser.class).setRequired(true)));

        manager.add(createComponent()
                .setInterface(Plugin.class.getName(), null)
                .setImplementation(OsgiJavaApiIndexer.class)
                .add(createServiceDependency().setService(MetadataService.class).setRequired(true))
                .add(createServiceDependency().setService(MetadataDao.class).setRequired(true))
                .add(createServiceDependency().setService(JavaMetadataParser.class).setRequired(true)));

    }

    /**
     * Destroy the dependency manager. Here you can remove all components and their dependencies.
     * Actually, the base class will clean up your dependencies anyway, so most of the time you
     * don't need to do anything here.
     * <p/>
     * If something goes wrong and you do not want your bundle to be stopped, you can throw an
     * exception. This exception will be passed on to the <code>stop()</code> method of the
     * bundle activator, causing the bundle not to stop.
     *
     * @param context the bundle context
     * @param manager the dependency manager
     * @throws Exception if the destruction fails
     */
    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
