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

package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import skillpro.model.assets.FactoryNode;

public class AssetTransfer extends ByteArrayTransfer {
	private static AssetTransfer instance = new AssetTransfer();
	private static final String TYPE_NAME = "factory-node-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);

	public static AssetTransfer getInstance() {
		return instance;
	}

	private AssetTransfer() {
	}

	@Override
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	@Override
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	@Override
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		return fromByteArray(bytes);
	}

	public FactoryNode[] fromByteArray(byte[] bytes) {
		DataInputStream in = new DataInputStream(
				new ByteArrayInputStream(bytes));

		try {
			int n = in.readInt();
			FactoryNode[] mos = new FactoryNode[n];

			for (int i = 0; i < n; i++) {
				FactoryNode mo = readTypeEntry(in);
				if (mo == null) {
					return null;
				}
				mos[i] = mo;
			}
			return mos;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private FactoryNode readTypeEntry(DataInputStream in) throws IOException {
		String type = in.readUTF();
		String id = in.readUTF();
		String name = in.readUTF();
		try {
			Constructor<?> construtctor = Class.forName(type).getConstructor(
					String.class, String.class);
			FactoryNode object;
			object = (FactoryNode) construtctor.newInstance(id, name);
			return object;
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	protected void javaToNative(Object object, TransferData transferData) {
		byte[] bytes = null;
		Object[] delivery = (Object[]) object;
		int size = delivery.length;
		FactoryNode[] factoryArray = new FactoryNode[size];
		int i = 0;
		while (i < size) {
			factoryArray[i] = (FactoryNode) delivery[i];
			i++;
		}
		bytes = toByteArray(factoryArray);

		if (bytes != null) {
			super.javaToNative(bytes, transferData);
		}
	}

	public byte[] toByteArray(FactoryNode[] mos) {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(byteOut);
		byte[] bytes = null;
		try {
			out.writeInt(mos.length);

			for (int i = 0; i < mos.length; i++) {
				writeAsset(mos[i], out);
			}
			out.close();
			bytes = byteOut.toByteArray();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		return bytes;
	}

	private void writeAsset(FactoryNode object, DataOutputStream dataOut)
			throws IOException {
		dataOut.writeUTF(object.getClass().getName());
		dataOut.writeUTF(object.getId());
		dataOut.writeUTF(object.getName());
	}
}
