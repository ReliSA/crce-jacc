package cz.zcu.kiv.crce.handler.versioning.internal;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

import cz.zcu.kiv.osgi.versionGenerator.service.VersionService;

import cz.zcu.kiv.crce.metadata.MetadataFactory;
import cz.zcu.kiv.crce.metadata.dao.MetadataDao;
import cz.zcu.kiv.crce.metadata.indexer.ResourceIndexerService;
import cz.zcu.kiv.crce.metadata.service.MetadataService;
import cz.zcu.kiv.crce.plugin.Plugin;
import cz.zcu.kiv.crce.repository.plugins.ActionHandler;

/**
 * Activator of this bundle.
 *
 * @author Jiri Kucera (kalwi@students.zcu.cz, jiri.kucera@kalwi.eu)
 */
public class Activator extends DependencyActivatorBase {

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        String services[] = {Plugin.class.getName(), ActionHandler.class.getName()};

        manager.add(createComponent()
                .setInterface(services, null)
                .setImplementation(VersioningActionHandler.class)
                .add(createServiceDependency().setRequired(true).setService(VersionService.class))
                .add(createServiceDependency().setRequired(true).setService(MetadataDao.class))
                .add(createServiceDependency().setRequired(true).setService(ResourceIndexerService.class))
                .add(createServiceDependency().setRequired(true).setService(MetadataFactory.class))
                .add(createServiceDependency().setRequired(true).setService(MetadataService.class))
        );

    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager) throws Exception {
        // do nothing
    }

}
