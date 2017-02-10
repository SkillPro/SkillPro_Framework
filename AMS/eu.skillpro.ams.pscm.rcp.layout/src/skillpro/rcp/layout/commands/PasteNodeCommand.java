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

package skillpro.rcp.layout.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import skillpro.rcp.layout.model.GEFNode;
import skillpro.rcp.layout.model.GEFWorkingPlace;
import skillpro.rcp.layout.model.Room;

public class PasteNodeCommand extends Command {
	private HashMap<GEFNode, GEFNode> list = new HashMap<GEFNode, GEFNode>();

	@Override
	@SuppressWarnings("unchecked")
	public boolean canExecute() {
		ArrayList<GEFNode> bList = (ArrayList<GEFNode>) Clipboard.getDefault()
				.getContents();
		if (bList == null || bList.isEmpty())
			return false;

		Iterator<GEFNode> it = bList.iterator();
		while (it.hasNext()) {
			GEFNode node = it.next();
			if (isPastableNode(node)) {
				list.put(node, null);
			}
		}
		return true;
	}

	@Override
	public void execute() {
		if (!canExecute())
			return;

		Iterator<GEFNode> it = list.keySet().iterator();
		while (it.hasNext()) {
			GEFNode node = it.next();
			try {
				if (node instanceof Room) {
					Room room = (Room) node;
					list.put(node, room.clone());
				} else if (node instanceof GEFWorkingPlace) {
					GEFWorkingPlace wp = (GEFWorkingPlace) node;
					GEFWorkingPlace clone = (GEFWorkingPlace) wp.clone();
					list.put(node, clone);
				}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}

		redo();
	}

	@Override
	public void redo() {
		Iterator<GEFNode> it = list.values().iterator();
		while (it.hasNext()) {
			GEFNode node = it.next();
			if (isPastableNode(node)) {
				node.getParent().addChild(node);
			}
		}
	}

	@Override
	public boolean canUndo() {
		return !(list.isEmpty());
	}

	@Override
	public void undo() {
		Iterator<GEFNode> it = list.values().iterator();
		while (it.hasNext()) {
			GEFNode node = it.next();
			if (isPastableNode(node)) {
				node.getParent().removeChild(node);
			}
		}
	}

	public boolean isPastableNode(GEFNode node) {
		if (node instanceof Room || node instanceof GEFWorkingPlace)
			return true;
		return false;
	}
}
