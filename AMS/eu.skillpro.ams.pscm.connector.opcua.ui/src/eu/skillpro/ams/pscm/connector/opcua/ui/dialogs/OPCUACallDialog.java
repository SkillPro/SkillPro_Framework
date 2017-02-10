/**
 * 
 */
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

package eu.skillpro.ams.pscm.connector.opcua.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import eu.skillpro.ams.pscm.connector.opcua.Activator;
import eu.skillpro.ams.pscm.connector.opcua.OPCUAServerRepository;

/**
 * @author aleksa
 * 
 * @version: 09.10.2014
 *
 */
public class OPCUACallDialog extends Dialog {
	private static final String NO_CONNECTION = "No Connection to OPC-UA-server established";
	private static final String CURRENT = "Connected to OPC-UA-server: ";
	
	protected Composite container;
	
	public OPCUACallDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		boolean connectionEstablished = checkConnection();
		if (!connectionEstablished) {
			Label l = new Label(area, SWT.CENTER);
			l.setText(NO_CONNECTION);
			l.setForeground(l.getDisplay().getSystemColor(SWT.COLOR_RED));
			l.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
			l = new Label(area, SWT.SEPARATOR|SWT.HORIZONTAL);
			l.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
		} else {
			Label l = new Label(area, SWT.CENTER);
			l.setText(CURRENT + OPCUAServerRepository.getSelectedServerUri());
			l.setForeground(l.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
			l.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
			l = new Label(area, SWT.SEPARATOR|SWT.HORIZONTAL);
			l.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
		}
		
		container = new Composite(area, SWT.NONE);
		container.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).create());
		container.setLayout(GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3).create());
		container.setEnabled(connectionEstablished);
		
		return container;
	}

	protected GridDataFactory getSecondGDF() {
		return GridDataFactory.swtDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.CENTER);
	}

	protected GridDataFactory getViewerGDF() {
		return GridDataFactory.swtDefaults().grab(true, true).span(3, 1).align(SWT.FILL, SWT.FILL).hint(300, 300);
	}

	protected GridDataFactory getLabelGDF() {
		return GridDataFactory.swtDefaults();
	}

	/**
	 * @return true if there is an open connection to an OPC-UA server, false
	 *         otherwise
	 */
	protected boolean checkConnection() {
		String currentUAaddress = Activator.getDefault().getCurrentUAaddress();
		if (currentUAaddress == null) {
			return false;
		} else {
			return (OPCUAServerRepository.testConnection(currentUAaddress));
		}
	}
}
