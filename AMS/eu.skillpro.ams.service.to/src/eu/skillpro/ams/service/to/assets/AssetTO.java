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
import java.util.Collection;
import java.util.List;
import java.util.Random;

import skillpro.model.assets.FactoryNode;
import skillpro.model.assets.Resource;
import skillpro.model.assets.Setup;
import skillpro.model.assets.State;
import skillpro.model.skills.ResourceSkill;

/**
 * @author caliqi
 * @date 10.03.2014
 * 
 */
public class AssetTO {
	public enum AssetType {
		ROOM("room"), ASSET("asset"), NO_FLY_ZONE("no-fly-zone");

		private String stringRepresentation;

		AssetType(String stringRepresentation) {
			this.stringRepresentation = stringRepresentation;
		}

		@Override
		public String toString() {
			return stringRepresentation;
		}
	}

	protected String id;
	private List<AssetTO> children = new ArrayList<AssetTO>();
	protected String name;
	//types: room, asset or no-fly-zone
	protected String type;
	private List<AttributeTO> attributes = new ArrayList<AttributeTO>();
	private List<SkillTO> skills = new ArrayList<SkillTO>();
	private State condition;

	private static Random r = new Random();

	private static String getRandomId() {
		return String.valueOf(r.nextInt(100000) + 1);
	}

	public AssetTO() {
	}

	/**
	 * A copy constructor, no deep copying.
	 * 
	 * @param assetTO
	 */
	public AssetTO(AssetTO assetTO) {
		this.id = assetTO.id;
		this.children = assetTO.children;
		this.name = assetTO.name;
		this.type = assetTO.type;
		this.attributes = assetTO.attributes;
		this.skills = assetTO.skills;
		this.condition = assetTO.condition;
	}

	public static AssetTO createNoFlyZone(String name, double currentX,
			double currentY, double width, double height) {
		AssetTO result = new AssetTO();
		result.id = getRandomId();
		result.name = name;
		result.type = AssetType.NO_FLY_ZONE.toString();
		result.attributes.add(new AttributeTO(getRandomId(), "currentX", String
				.valueOf(currentX)));
		result.attributes.add(new AttributeTO(getRandomId(), "currentY", String
				.valueOf(currentY)));
		result.attributes.add(new AttributeTO(getRandomId(), "width", String
				.valueOf(width)));
		result.attributes.add(new AttributeTO(getRandomId(), "height", String
				.valueOf(height)));
		return result;
	}

	public AssetTO(FactoryNode factoryNode) {
		this.id = factoryNode.getNodeID();
		this.name = factoryNode.getName();
		attributes.add(new AttributeTO(getRandomId(), "currentX", "" + factoryNode.getCurrentCoordinates().x));
		attributes.add(new AttributeTO(getRandomId(), "currentY", "" + factoryNode.getCurrentCoordinates().y));
		attributes.add(new AttributeTO(getRandomId(), "currentZ", "" + factoryNode.getCurrentCoordinates().z));
		attributes.add(new AttributeTO(getRandomId(), "height", "" + factoryNode.getHeight()));
		attributes.add(new AttributeTO(getRandomId(), "width", "" + factoryNode.getLength()));
		attributes.add(new AttributeTO(getRandomId(), "length", "" + factoryNode.getWidth()));

		if (!(factoryNode instanceof Resource)) {
			this.type = AssetType.ROOM.toString();
		} else {
			this.type = AssetType.ASSET.toString();
			Resource asset = (Resource) factoryNode;
			this.condition = asset.getState();
			List<Setup> confs = asset.getSetups();
			for (Setup conf : confs) {
				for (ResourceSkill rs : conf.getResourceSkills()) {
					skills.add(new SkillTO(rs));
				}
			}

		}

		List<FactoryNode> subNodes = factoryNode.getSubNodes();
		if (subNodes != null) {
			for (FactoryNode sn : subNodes) {
				children.addAll(retrieveNotVirtualChildren(sn));
			}
		}
	}

	/**
	 * @param factoryNode
	 * @return
	 */
	public static Collection<? extends AssetTO> retrieveNotVirtualChildren(
			FactoryNode factoryNode) {
		List<AssetTO> assetTOs = new ArrayList<AssetTO>();
		if (factoryNode.isLayoutable()) {
			assetTOs.add(new AssetTO(factoryNode));
		} else {
			List<FactoryNode> subNodes = factoryNode.getSubNodes();
			if (subNodes != null) {
				for (FactoryNode sn : subNodes) {
					assetTOs.addAll(retrieveNotVirtualChildren(sn));
				}
			}
		}
		return assetTOs;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the children
	 */
	public List<AssetTO> getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<AssetTO> children) {
		this.children = children;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
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

	/**
	 * @return the skills
	 */
	public List<SkillTO> getSkills() {
		return skills;
	}

	/**
	 * @param skills
	 *            the skills to set
	 */
	public void setSkills(List<SkillTO> skills) {
		this.skills = skills;
	}

	public AttributeTO getAttribute(String name) {
		for (AttributeTO att : attributes) {
			if (att.getName().equalsIgnoreCase(name))
				return att;
		}
		return null;
	}

	/**
	 * @return the condition
	 */
	public State getCondition() {
		return condition;
	}

	/**
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(State condition) {
		this.condition = condition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AssetTO [id=" + id + ", children=" + children + ", name="
				+ name + ", type=" + type + ", attributes=" + attributes
				+ ", skills=" + skills + ", condition=" + condition + "]";
	}

}
