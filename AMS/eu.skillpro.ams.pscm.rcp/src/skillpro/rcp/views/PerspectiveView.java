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

package skillpro.rcp.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import skillpro.rcp.activator.SkillproActivator;
import skillpro.rcp.commands.OpenDefaultPerspective;
import skillpro.rcp.utils.SkillproPerspectiveRegistry;
import skillpro.rcp.utils.SkillproPerspectiveRegistry.PerspectiveInfo;

public class PerspectiveView extends ViewPart implements ISizeProvider {
	public static final String ID = PerspectiveView.class.getName();

	private Composite top;
	private Composite groupComposite;
	private List<Button> listButtons;
	private Button logo;
	private Button selected;

	private final Image smallLogoImg = SkillproActivator.getImageDescriptor(
			"icons/skillpro-tray.png").createImage();

	public PerspectiveView() {
		listButtons = new ArrayList<Button>();
	}

	@Override
	public void createPartControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		top.setLayout(GridLayoutFactory.swtDefaults().numColumns(2)
				.equalWidth(false).create());

		Composite logoComposite = new Composite(top, SWT.LEFT_TO_RIGHT);
		logoComposite.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).align(SWT.BEGINNING,SWT.BOTTOM).minSize(SWT.DEFAULT, SWT.DEFAULT).create());
		logoComposite.setLayout(GridLayoutFactory.fillDefaults().create());
		logo = new Button(logoComposite, SWT.TOGGLE);
		logo.setImage(smallLogoImg);
		logo.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING,SWT.CENTER).grab(false, false).hint(60,60).create());
		listButtons.add(logo);
		logo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IHandlerService handlerService = 
						(IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
				updateButtons(logo);
				try {
					handlerService.executeCommand(OpenDefaultPerspective.ID, null);
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				} catch (NotDefinedException e2) {
					System.out.println("command not defined");
					e2.printStackTrace();
				} catch (NotEnabledException e3) {
					System.out.println("command not activated yet");
					e3.printStackTrace();
				} catch (NotHandledException e4) {
					e4.printStackTrace();
				}
			}
		});

		createHorizontalGroups();
	}
	
	private void updateButtons(Button selectedButton) {
		selected = selectedButton;
		updateButtons();
	}

	@Override
	public void setFocus() {
		groupComposite.setFocus();
	}

	public void update() {
		groupComposite.dispose();
		createHorizontalGroups();
	}

	private void updateButtons() {
		for (Button button : listButtons) {
			if (button.equals(selected)) {
				button.setSelection(true);
			} else {
				button.setSelection(false);
			}
		}
	}

	private void createHorizontalGroups() {
		groupComposite = new Composite(top, SWT.RIGHT_TO_LEFT);
		List<String> groupsList = new ArrayList<>();
		List<String> perspectiveIDs = SkillproPerspectiveRegistry
				.getAllRegisteredPerspectives();
		for (String perspectiveID : perspectiveIDs) {
			PerspectiveInfo pInfo = SkillproPerspectiveRegistry
					.getPerspectiveInfoForPerspective(perspectiveID);
			if (!groupsList.contains(pInfo.getGroupID())) {
				int i = 0;
				boolean smallerThanPrev = false;
				while (i < groupsList.size() && !smallerThanPrev) {
					String name = groupsList.get(i);
					if (pInfo.getGroupID().compareTo(name) > 0) {
						i++;
					} else {
						smallerThanPrev = true;
					}
				}
				groupsList.add(i, pInfo.getGroupID());
			}
		}
		int numOfCols = groupsList.size();
		groupComposite.setLayoutData(GridDataFactory.fillDefaults()
				.grab(true, false).create());
		groupComposite.setLayout(GridLayoutFactory.fillDefaults()
				.equalWidth(false).numColumns(numOfCols).create());

		for (String groupName : groupsList) {
			Group group = new Group(groupComposite, SWT.NONE);
			int numberOfPerspectivesInGroup = 0;
			
			GridDataFactory groupGD = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(false, true);

			for (String perspectiveID : perspectiveIDs) {
				PerspectiveInfo pInfo = SkillproPerspectiveRegistry
						.getPerspectiveInfoForPerspective(perspectiveID);
				if (pInfo.getGroupID().equals(groupName)) {
					Button button = createButton(group, pInfo);
					listButtons.add(button);
					numberOfPerspectivesInGroup++;
				}
			}
			group.setLayout(GridLayoutFactory.fillDefaults()
					.numColumns(numberOfPerspectivesInGroup).margins(4, 3)
					.create());
			group.setLayoutData(groupGD.create());
			group.setText(groupName);
		}
	}

	private Button createButton(final Composite buttonBox,
			final PerspectiveInfo pInfo) {
		final Button spButton = new Button(buttonBox, SWT.TOGGLE); // switchPerspectiveButton
		spButton.setLayoutData(GridDataFactory.swtDefaults().hint(40, 40)
				.align(SWT.FILL, SWT.FILL).create());
		spButton.setToolTipText(buttonText(pInfo.getPerspectiveName()));
		if (pInfo.getImage() != null) {
			spButton.setImage(pInfo.getImage());
		}
		spButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IHandlerService handlerService = (IHandlerService) PlatformUI
						.getWorkbench().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(pInfo.getCommandID(), null);
					updateButtons(spButton);
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				} catch (NotDefinedException e2) {
					System.out.println("command not defined");
					e2.printStackTrace();
				} catch (NotEnabledException e3) {
					System.out.println("command not activated yet");
					e3.printStackTrace();
				} catch (NotHandledException e4) {
					e4.printStackTrace();
				}
			}
		});
		return spButton;
	}

	private String buttonText(String perspectiveName) {
		return perspectiveName;
	}

	@Override
	public int getSizeFlags(boolean width) {
		return SWT.MIN | SWT.MAX;
	}

	@Override
	public int computePreferredSize(boolean width, int availableParallel,
			int availablePerpendicular, int preferredResult) {

		return width ? 80 : 80;
	}
}