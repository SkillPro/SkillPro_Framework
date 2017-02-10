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

package skillpro.vc.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import skillpro.model.assets.FactoryNode;
import skillpro.model.utils.Pair;
import skillpro.model.utils.Vector3d;
import skillpro.vc.client.gen.datacontract.Asset;

public class VCStartStopHandler extends AbstractHandler implements IHandler{

	private Job synchronizationJob;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (VCSynchronizeHandler.getInput() == null) {
			MessageDialog dialog = new MessageDialog(HandlerUtil.getActiveShellChecked(event), "Synchnorization not configured!", null,
					"Please configure the synchronization and try again", MessageDialog.ERROR, new String[] { "OK"}, 0);
			dialog.open();
			return null;
		}
		if (synchronizationJob == null) {
			synchronizationJob = new Job("Synchrnizing with VIS...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					while (!monitor.isCanceled()) {
						for (Pair<FactoryNode, Asset> pair : VCSynchronizeHandler.getInput()) {
							FactoryNode firstElement = pair.getFirstElement();
							Asset secondElement = pair.getSecondElement();

							if (firstElement != null && secondElement != null) {
								Vector3d position = new Vector3d();
								do{
									position.x += firstElement.getCurrentCoordinates().x;
									position.y += firstElement.getCurrentCoordinates().y;
									position.z += firstElement.getCurrentCoordinates().z;
									firstElement = firstElement.getParent();
								} while (firstElement != null);
								System.out.println(position);
								// x, y are exchanged! - undone now
								secondElement.setWorldPositionMatrixSkillPro(new double[]{position.y, position.x, position.z});						
							}
						}
						try {
							Thread.sleep(1000);
							System.out.println("Synchronizing...");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					monitor.done();
					return org.eclipse.core.runtime.Status.OK_STATUS;
				}
			};
		
		synchronizationJob.setUser(false);
		synchronizationJob.schedule();
		
		} else {
			if (synchronizationJob.getState() == Job.RUNNING) {
				synchronizationJob.cancel();
			} else {
				synchronizationJob.schedule();
			}
		}

		return null;
	}

}
