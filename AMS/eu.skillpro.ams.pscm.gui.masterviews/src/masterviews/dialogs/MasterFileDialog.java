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

package masterviews.dialogs;

import java.io.File;

import javax.xml.transform.TransformerException;

import masterviews.util.SupportedFileType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class MasterFileDialog {

	private MasterFileDialog() {
	}
	
	public static String getFilenameFromFileDialog(SupportedFileType fileType) {
		FileDialog amlDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
		amlDialog.setText("Open " + fileType.getName() + " file");
		String[] filterNames = new String[]{ fileType.getDescription() + " (" + fileType.getName() + ")",
				"All Files (*)" };
		String[] filterExtensions = new String[]{ "*" + fileType.getName(), "*" };
		amlDialog.setFilterNames(filterNames);
		amlDialog.setFilterExtensions(filterExtensions);
		amlDialog.setFileName("");
		
		return amlDialog.open();
	}
	
	public static File[] getLocalFilesFromFileDialog(SupportedFileType fileType) {
		FileDialog amlDialog = new FileDialog (Display.getDefault().getActiveShell(), SWT.OPEN | SWT.MULTI);
		amlDialog.setText("Open " + fileType.getName() + " file");
		String[] filterNames = new String[]{ fileType.getDescription() + " (" + fileType.getName() + ")",
				"All Files (*)" };
		String[] filterExtensions = new String[]{ "*" + fileType.getName(), "*" };
		amlDialog.setFilterNames(filterNames);
		amlDialog.setFilterExtensions(filterExtensions);
		amlDialog.setFileName("");
		
		String path = amlDialog.open();
		if (path != null && !path.equals("")) {
			 String[] files = amlDialog.getFileNames();
			 File[] realFiles = new File[files.length];
			 for (int i = 0, n = files.length; i < n; i++) {
				 String fileName = "";
				 fileName += (amlDialog.getFilterPath());
				 if (fileName.charAt(fileName.length() - 1) != File.separatorChar) {
					 fileName += (File.separatorChar);
				 }
				 fileName += (files[i]);
				 realFiles[i] = new File(fileName);
			 }
			return realFiles;
		}
		return null;
	}
	
	public static String saveFile(SupportedFileType fileType) throws TransformerException {
		FileDialog amlDialog = new FileDialog (Display.getDefault().getActiveShell(), SWT.SAVE);
		amlDialog.setText("Save " + fileType.getName() + " file");
		String[] filterNames = new String[]{ fileType.getDescription() + " (" + fileType.getName() + ")",
				"All Files (*)" };
		String[] filterExtensions = new String[]{ "*" + fileType.getName(), "*" };
		amlDialog.setFilterNames(filterNames);
		amlDialog.setFilterExtensions(filterExtensions);
		amlDialog.setFileName("");
		
		return amlDialog.open();
	}
	
}
