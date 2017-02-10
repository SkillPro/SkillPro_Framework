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

package pscm.gui.combo;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.part.NullEditorInput;


@SuppressWarnings("restriction")
public class OpenComboPerspective extends AbstractHandler implements IHandler {
	public static final String ID = OpenComboPerspective.class.getName();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPerspectiveRegistry perspectiveRegistry = HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench()
				.getPerspectiveRegistry();
		IPerspectiveDescriptor pd = perspectiveRegistry.findPerspectiveWithId(ComboPerspective.ID);
		IWorkbenchPage page = HandlerUtil.getActiveSite(event).getPage();
		page.setPerspective(pd);
	
		IEditorReference[] alleditors = page.getEditorReferences();
		for (IEditorReference editorRef:alleditors) {
			page.hideEditor(editorRef);
		}
		try {
			page.openEditor(new NullEditorInput(), "skillpro.rcp.layout.MyGraphicalEditor");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
}
