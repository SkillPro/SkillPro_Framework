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

package skillpro.model.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import skillpro.model.properties.Property;
import skillpro.model.properties.PropertyConstraint;
import skillpro.model.properties.PropertyDesignator;


/**
 * This class represents the resource configurations needed for the executable skills
 * as part of their pre- and post-conditions.
 * 
 * <div>
	<p>Note (the second proposal was chosen)</p>
	<p>Two proposal for generation of executable skills (taken from <a href="https://iirobweb.ira.uka.de/SkillPro/eucall/event301/"> https://iirobweb.ira.uka.de/SkillPro/eucall/event301/</a>):</p>
	<ol>
		<li>AMS generates executable skills our of ProductionSkill - ResourceSkill mapping and generates new IDs for 
			pre and post resource conditions (pre and post configurations). Then it sends this new skills with the whole 
			conditional information to the SEE which handles internally the new pre and post conditions - it maps the newly 
			generated IDs to the internal states of its resources.
			<ul>
				<li>for example: a P&amp;P-robot will map pre and post conditions to its &quot;grab position&quot; and &quot;release position&quot; each time - even if this positions don't change for different products, in the context of the executable skill they will have unique IDs. A machine that has only one configuration will map both pre and post condition to this &quot;internal    configuration&quot; regardless that they have different IDs.</li>
				<li>pro: we don't extend the SkillPro's AML specification</li>
				<li>contra: additional mapping effort (automatic or manual) for each execution skill; a lot of redundant configuration have to be handled. what if the memory of the SEE is not enough - the Exec.Skill dosn't contain the &quot;real resource configuration&quot; only the generated ID and if the SEE forgets this Exec.Skill it has to be taught again.</li>
			</ul>
		</li>
		
		<li>The asset description provides a list of possible configurations. Each resource skill defines its pre and post configuration.
		<ul>
			<li>for example: a P&amp;P robot defines in advance its &quot;grab position&quot;, &quot;ready position&quot;, &quot;release position&quot;. The P&amp;P skill then appoints one of the defined configurations (positions) as pre condition and another as post condition. All this information is modelled in the PSCM (or AML editor).</li>
			<li>pro: we don't generate meaningless IDs, rather refer to existing objects. no additional effort when generating new executable skills for a resource skill.</li>
			<li>contra: AML specification has to be adapted. more modelling effort for asset description. assets with lots of configurations/positions will have large descriptions.</li>
			<li>possible simplification: each resource defines exactly two or three configurations for pre and post conditions, for example: the mobile platform have positions &quot;any&quot;, &quot;start&quot; &quot;goal&quot; that will be rendered in runtime via Env.Server.</li>
		</ul>
		</li>
	</ol>
	<p>Of course some mixtures of both are possible.</p>
</div>

 * @author caliqi
 * 
 * @version: 25.11.2014
 *
 */
public class ResourceConfiguration {
	private String id;
	private String name;
	private ResourceConfigurationType resourceConfigurationType;
	private List<PropertyDesignator> propertyDesignators = new ArrayList<>();
	private Resource resource;
	
	public ResourceConfiguration(String id, String name, Resource resource) {
		this(id, name, null, resource);
	}
	
	public ResourceConfiguration(String id, String name, ResourceConfigurationType resourceConfigurationType, Resource resource) {
		this.id = id;
		this.name = name;
		setResourceConfigurationType(resourceConfigurationType);
		this.resource = resource;
		
	}
	
	/**
	 * A copy constructor, all fields but the {@link #resourceConfigurationType}
	 * and the {@link #resource} are deeply copied.
	 * 
	 * @param r a ResourceConfiguration
	 */
	public ResourceConfiguration(ResourceConfiguration r) {
		this.id = r.id;
		this.name = r.name;
		this.resourceConfigurationType = r.resourceConfigurationType;
		for (PropertyDesignator pd : r.propertyDesignators) {
			this.propertyDesignators.add(new PropertyDesignator(pd));
		}
		this.resource = r.resource;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public ResourceConfigurationType getResourceConfigurationType() {
		return resourceConfigurationType;
	}

	public void setResourceConfigurationType(ResourceConfigurationType resourceConfigurationType) {
		this.resourceConfigurationType = resourceConfigurationType;
		if (resourceConfigurationType != null) {
			for (Property p : resourceConfigurationType.getProperties()) {
				propertyDesignators.add(new PropertyDesignator(p, null, null));
			}
		}
	}
	
	public List<Property> getProperties() {
		if (resourceConfigurationType == null) {
			return new ArrayList<>();
		} else {
			return new ArrayList<>(resourceConfigurationType.getProperties());
		}
	}
	
	public boolean setProperty(Property p, Object value, List<PropertyConstraint> constraints) {
		for (PropertyDesignator pd : propertyDesignators) {
			if (pd.getProperty().equals(p)) {
				pd.setValue(value.toString());
				pd.setConstraints(constraints);
				return true;
			}
		}
		return false;
	}
	
	public List<PropertyDesignator> getPropertyDesignators() {
		return propertyDesignators;
	}
	
	public boolean updateDesignator(PropertyDesignator propDes) {
		return setProperty(propDes.getProperty(), propDes.getValue(), propDes.getConstraints());
	}
	
	public Resource getResource() {
		return resource;
	}
	
	//should not be used! better to just use addConfiguration from Resource
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResourceConfiguration other = (ResourceConfiguration) obj;
		return Objects.equals(id, other.id);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
