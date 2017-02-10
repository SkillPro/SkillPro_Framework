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

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import skillpro.asset.views.AssetTreeView;
import skillpro.asset.views.CatalogView;
import skillpro.asset.views.SEEView;
import skillpro.gui.skills.views.AttributeTableView;
import skillpro.gui.skills.views.CompleteSkillTreeView;
import skillpro.gui.skills.views.ProblemsView;
import skillpro.gui.skills.views.SupportedAssetsView;
import skillpro.product.views.ProductTreeView;

public class ComboPerspective implements IPerspectiveFactory {
	public static final String ID = ComboPerspective.class.getName();
	public static final String NAME = "PPR";
	public static final String GROUP_ID = "PPR";
	public static final String IMAGE_PATH = "assets2.png";
	
	private static final String ID_TABS_FOLDER = "pscm.gui.combo.assets";
	private static final String ID_TABS_FOLDER2 = "pscm.gui.combo.probs";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		
		//assets
		layout.addStandaloneView(AssetTreeView.ID, true, IPageLayout.LEFT, 0.25f, editorArea);
		layout.getViewLayout(AssetTreeView.ID).setCloseable(false);
		
		IFolderLayout tabs = layout.createFolder(
				ID_TABS_FOLDER, IPageLayout.BOTTOM, 0.5f, AssetTreeView.ID);
		tabs.addView(CatalogView.ID);
		tabs.addView(IPageLayout.ID_OUTLINE);
		tabs.addView(SEEView.ID);
		layout.getViewLayout(AssetTreeView.ID).setCloseable(false);
		layout.getViewLayout(SEEView.ID).setCloseable(false);
		layout.getViewLayout(CatalogView.ID).setCloseable(false);
		layout.getViewLayout(IPageLayout.ID_OUTLINE).setCloseable(false);
		
		//products
		layout.addStandaloneView(ProductTreeView.ID, true, IPageLayout.RIGHT, 0.5f, editorArea);
		layout.getViewLayout(ProductTreeView.ID).setCloseable(false);

		//skill
		layout.addStandaloneView(SupportedAssetsView.ID, true, IPageLayout.TOP, 0.5f, ProductTreeView.ID);
		layout.addStandaloneView(CompleteSkillTreeView.ID, true, IPageLayout.LEFT, 0.5f, ProductTreeView.ID);
		layout.getViewLayout(CompleteSkillTreeView.ID).setCloseable(false);	
		layout.getViewLayout(SupportedAssetsView.ID).setCloseable(false);
		
		//attributes and problems view
		IFolderLayout lowerTabs = layout.createFolder(
				ID_TABS_FOLDER2, IPageLayout.BOTTOM, 0.5f, editorArea);
		lowerTabs.addView(AttributeTableView.ID);
		lowerTabs.addView(ProblemsView.ID);
	}
}
