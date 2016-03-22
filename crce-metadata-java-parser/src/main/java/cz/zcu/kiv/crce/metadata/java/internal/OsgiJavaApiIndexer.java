package cz.zcu.kiv.crce.metadata.java.internal;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.zcu.kiv.jacc.javatypes.JClass;
import cz.zcu.kiv.jacc.loader.JClassLoaderCreator;
import cz.zcu.kiv.obcc.bundleloader.BundleMetadataReaderCreator;
import cz.zcu.kiv.obcc.bundletypes.JOSGiBundle;
import cz.zcu.kiv.obcc.bundletypes.JOSGiExportPackage;
import cz.zcu.kiv.obcc.loader.JOSGiBundleLoader;
import cz.zcu.kiv.obcc.loader.impl.JOSGiBundleLoaderImpl;

import cz.zcu.kiv.crce.metadata.Capability;
import cz.zcu.kiv.crce.metadata.Resource;
import cz.zcu.kiv.crce.metadata.dao.ResourceDAO;
import cz.zcu.kiv.crce.metadata.java.parser.JavaMetadataParser;
import cz.zcu.kiv.crce.metadata.service.MetadataService;
import cz.zcu.kiv.crce.repository.Store;
import cz.zcu.kiv.crce.repository.plugins.AbstractActionHandler;

/**
 * Extracts and stores public OSGi API information for the resource.
 *
 * Date: 24.11.15
 *
 * @author Jakub Danek
 */
public class OsgiJavaApiIndexer extends AbstractActionHandler {
    private static final Logger logger = LoggerFactory.getLogger(OsgiJavaApiIndexer.class);

    private JOSGiBundleLoader bundleLoader;
    private JavaMetadataParser metadataParser; //DI
    private MetadataService metadataService; //DI
    private ResourceDAO resourceDAO; // DI

    public OsgiJavaApiIndexer() {
        this.bundleLoader = new JOSGiBundleLoaderImpl(JClassLoaderCreator.JAR_MEMORY_LOADER, BundleMetadataReaderCreator.JAR_FILE_READER);
    }

    @Override
    public Resource afterPutToStore(Resource resource, Store store) {
        try {
            JOSGiBundle bundle = bundleLoader.loadBundleRepresentation(metadataService.getUri(resource).toString());

            Set<JClass> toParse = new LinkedHashSet<>();
            for (JOSGiExportPackage exported : bundle.getExportedPackages()) {
                toParse.addAll(exported.getClasses());
            }

            Set<Capability> apiCapabilities = metadataParser.parse(toParse);
            for (Capability apiCapability : apiCapabilities) {
                metadataService.addRootCapability(resource, apiCapability);
            }

            resourceDAO.saveResource(resource);
            logger.info("Successfully processed bundle: {}", resource.getId());
        } catch (Exception e) {
            logger.warn("Unable to process resource file for the resource: {}", resource.getId());
            logger.debug("Exception thrown: ",e);
        }

        return resource;
    }
}
