/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: AMS Server
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

package eu.skillpro.ams.service.to.assets;

import java.util.ArrayList;
import java.util.List;

public class GeoDataTO {
	private String parentId = "";
	private List<AttributeTO> attributes = new ArrayList<AttributeTO>();

	public GeoDataTO(AssetTO asset, String parentId) {
		this.parentId = parentId;

		List<AttributeTO> attributesTemp = new ArrayList<AttributeTO>();

		attributesTemp.add(asset.getAttribute("currentX"));
		attributesTemp.add(asset.getAttribute("currentY"));
		attributesTemp.add(asset.getAttribute("currentZ"));
		attributesTemp.add(asset.getAttribute("height"));
		attributesTemp.add(asset.getAttribute("length"));
		attributesTemp.add(asset.getAttribute("width"));
		for (AttributeTO att : attributesTemp) {
			if (att != null) {
				attributes.add(att);
			}
		}
	}

	/**
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the attributes
	 */
	public List<AttributeTO> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(List<AttributeTO> attributes) {
		this.attributes = attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GeoDataTO [parentId=" + parentId + ", attributes=" + attributes
				+ "]";
	}
}
