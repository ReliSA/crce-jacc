package cz.zcu.kiv.crce.jacc.deps.internal;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 11.9.15
 *
 * @author Jakub Danek
 */
public class Activator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    public void start(BundleContext bundleContext) throws Exception {
        initVfs();
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

    private void initVfs() {
        logger.info("Initializing VFS.");
        StandardFileSystemManager fsm = new StandardFileSystemManager();
        fsm.setClassLoader(fsm.getClass().getClassLoader());
        try {
            FieldUtils.writeDeclaredStaticField(VFS.class, "instance", fsm, true);
            fsm.init();
            logger.info("VFS initialized.");
        } catch (FileSystemException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
