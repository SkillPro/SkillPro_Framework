/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: Production System Configuration Manager (PSCM)
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This file is part of the AMS (Asset Management System), which has been developed
 * at the PDE department of the FZI, Karlsruhe. It is part of the SkillPro Framework,
 * which is is developed in the SkillPro project, funded by the European FP7
 * programme (Grant Agreement 287733).
 *
 * The SkillPro Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The SkillPro Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the SkillPro Framework. If not, see <http://www.gnu.org/licenses/>.
*****************************************************************************/

package skillpro.rcp.activator;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SkillproActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "eu.skillpro.ams.pscm.rcp"; //$NON-NLS-1$
	
	// The shared instance
	private static SkillproActivator plugin;

	/**
	 * The constructor
	 */
	public SkillproActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SkillproActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
    /**
     * Creates an image by loading it from a file in
     * the plugin's small icon's (16x16) directory.
     *
     * @param imagePath The path to the image file to load.
     *     The image path must not include the plugin's images
     *     path location defined in <code>IMAGES_PATH</code>.
     *     If you want to load <code>IMAGES_PATH/myimage.gif</code>,
     *     simply pass <code>myimage.gif</code> to this method.
     *
     * @return The image object loaded from the image file
     */
    public static Image createIcon(String imagePath, ICONSIZE size) {
		final Bundle pluginBundle = Platform.getBundle(SkillproActivator.PLUGIN_ID);
		final Path imageFilePath = new Path(size.getPath()
				+ imagePath);
		final URL imageFileUrl = FileLocator.find(pluginBundle, imageFilePath,
				null);
		return ImageDescriptor.createFromURL(imageFileUrl).createImage();
	}
    
    public enum ICONSIZE {
    	SMALL {
			@Override
			String getPath() {
				return "icons/16x16/";
			}
		},
    	MEDIUM {
			@Override
			String getPath() {
				return "icons/24x24/";
			}
		},
    	BIG {
			@Override
			String getPath() {
				return "icons/32x32/";
			}
		},
    	VERY_BIG {
			@Override
			String getPath() {
				return "icons/64x64/";
			}
		};
    	abstract String getPath();
    }
}
