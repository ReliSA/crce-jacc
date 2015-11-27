package cz.zcu.kiv.crce.compatibility.internal;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cz.zcu.kiv.crce.compatibility.CompatibilityFactory;
import cz.zcu.kiv.crce.compatibility.dao.CompatibilityDao;
import cz.zcu.kiv.crce.compatibility.internal.plugin.CompatibilityActionHandler;
import cz.zcu.kiv.crce.compatibility.internal.service.CompatibilityServiceImpl;
import cz.zcu.kiv.crce.compatibility.service.CompatibilitySearchService;
import cz.zcu.kiv.crce.compatibility.service.CompatibilityService;
import cz.zcu.kiv.crce.concurrency.service.TaskRunnerService;
import cz.zcu.kiv.crce.metadata.MetadataFactory;
import cz.zcu.kiv.crce.metadata.dao.ResourceDAO;
import cz.zcu.kiv.crce.metadata.service.MetadataService;
import cz.zcu.kiv.crce.plugin.Plugin;
import cz.zcu.kiv.crce.repository.Store;
import cz.zcu.kiv.crce.repository.plugins.ActionHandler;

/**
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

    //injected by DI
    private CompatibilityService compatibilityService;
    private MetadataService metadataService;
    private ResourceDAO resourceDAO;

    public CompatibilityService getCompatibilityService() {
        return compatibilityService;
    }

    public MetadataService getMetadataService() {
        return metadataService;
    }

    public ResourceDAO getResourceDAO() {
        return resourceDAO;
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
                .setInterface(CompatibilityFactory.class.getName(), null)
                .setImplementation(CompatibilityFactoryImpl.class));


        String services[] = {CompatibilityService.class.getName(), CompatibilitySearchService.class.getName()};
        manager.add(createComponent()
                .setInterface(services, null)
                .setImplementation(CompatibilityServiceImpl.class)
                .add(createServiceDependency().setService(CompatibilityDao.class).setRequired(true))
                .add(createServiceDependency().setService(CompatibilityFactory.class).setRequired(true))
                .add(createServiceDependency().setService(Store.class).setRequired(true))
                .add(createServiceDependency().setService(MetadataFactory.class).setRequired(true))
                .add(createServiceDependency().setService(MetadataService.class).setRequired(true))
        );

        String pluginServices[] = {Plugin.class.getName(), ActionHandler.class.getName()};
        manager.add(createComponent()
                        .setInterface(pluginServices, null)
                        .setImplementation(CompatibilityActionHandler.class)
                        .add(createServiceDependency().setRequired(true).setService(TaskRunnerService.class))
                        .add(createServiceDependency().setRequired(true).setService(CompatibilityService.class))
                        .add(createServiceDependency().setRequired(true).setService(MetadataService.class))
                        .add(createServiceDependency().setRequired(true).setService(ResourceDAO.class))
        );

        //create this component, inject dependencies
        manager.add(createComponent()
                        .setImplementation(this)
                        .add(createServiceDependency().setService(MetadataService.class).setRequired(true))
                        .add(createServiceDependency().setService(ResourceDAO.class).setRequired(true))
                        .add(createServiceDependency().setService(CompatibilityService.class).setRequired(true))
        );
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
