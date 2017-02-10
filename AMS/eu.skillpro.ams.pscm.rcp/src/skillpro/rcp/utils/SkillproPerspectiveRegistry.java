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

package skillpro.rcp.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import skillpro.rcp.views.PerspectiveView;
import eu.skillpro.ams.pscm.icons.IconActivator;

public class SkillproPerspectiveRegistry {
	private static Map<String, PerspectiveInfo> registry = new HashMap<String, PerspectiveInfo>();
	private static List<String> keys = new ArrayList<String>();
	
	public static boolean putInRegistry(PerspectiveInfo pInfo) {
		if (registry.containsKey(pInfo.getPerspectiveName())) {
			return false;
		} else {
			keys.add(pInfo.getPerspectiveName());
			registry.put(pInfo.getPerspectiveName(), pInfo);
			try {
				((PerspectiveView) (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(PerspectiveView.ID))).update();
			} catch (IllegalStateException e) {
				System.out.println("workbench not created yet");
//				its normal that workbench is not created when starting the program. If a perspective loaded during the runtime, this shouldn't occur
			} catch (NullPointerException e) {
				System.out.println("no active page");
			}
			return true;
		}
	}
	
	public static List<String> getAllRegisteredPerspectives() {
		return keys;
	}
	
	public static PerspectiveInfo getPerspectiveInfoForPerspective(String perspectiveID) {
		return registry.get(perspectiveID);
	}
	
	public static class PerspectiveInfo {
		private String perspectiveID;
		private String perspectiveName;
		private String commandID;
		private String groupID;
		private Image image;
		private String imageName;
		
		public PerspectiveInfo(String perspectiveID, String perspectiveName, String commandID, String groupID, String imageName, Image image) {
			super();
			this.perspectiveID = perspectiveID;
			this.perspectiveName = perspectiveName;
			this.commandID = commandID;
			this.groupID = groupID;
			this.image = image;
			this.imageName = imageName;
		}

		public String getPerspectiveID() {
			return perspectiveID;
		}

		public String getPerspectiveName() {
			return perspectiveName;
		}
		
		public String getCommandID() {
			return commandID;
		}
		
		public String getGroupID() {
			return groupID;
		}
		
		public Image getImage() {
			if (image == null && imageName != null && !imageName.isEmpty()) {
				image = IconActivator.getImageDescriptor("/icons/perspectives/" + imageName).createImage(); 
			}
			return image;
		}
	}
}
