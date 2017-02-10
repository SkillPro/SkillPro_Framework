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

import java.util.Objects;

public enum State {
	PRE_OPERATIONAL("Pre-operational") {
		@Override
		public String getIdentifier() {
			return "110";
		}

		@Override
		public String getDescription() {
			return "The resource is shut off";
		}
	},
	BREAKDOWN("Breakdown") {
		@Override
		public String getIdentifier() {
			return "120";
		}

		@Override
		public String getDescription() {
			return "The machine is unavailable. We don't known if and when it will return to be operational";
		}
	},
	
	IN_MAINTENANCE("In-maintenance") {
		@Override
		public String getIdentifier() {
			return "130";
		}

		@Override
		public String getDescription() {
			return "The machine is in maintenance. But there is a near-term timestamp when we assume it to become available again.";
		}
	},
	
	IN_CONFIGURATION("In-configuration") {
		@Override
		public String getIdentifier() {
			return "140";
		}

		@Override
		public String getDescription() {
			return "Asset is currently in (re)configuration";
		}
	},
	
	READY("Ready") {
		@Override
		public String getIdentifier() {
			return "2XX";
		}

		@Override
		public String getDescription() {
			return "Asset is operational";
		}
	},
	
	ERROR("Error") {
		@Override
		public String getIdentifier() {
			return "3XX";
		}

		@Override
		public String getDescription() {
			return "Different error types";
		}
	},
	
	NOT_INTEGRATED("Not Integrated") {
		@Override
		public String getIdentifier() {
			return "150";
		}

		@Override
		public String getDescription() {
			return "Asset is loaded in production system for testing purposes only. Physically not integrated!";
		}
	};
	
	private String name;
	
	private State(String name) {
		this.name = name;
	}
	
	public abstract String getIdentifier();
	public abstract String getDescription();
	
	@Override
	public String toString() {
		return name;
	}
	
	public static State valueOfIgnoreCase(String name) {
		Objects.requireNonNull(name);
		for (State state : values()) {
			if (name.equalsIgnoreCase(state.name())) {
				return state;
			}
		}
        throw new IllegalArgumentException("No enum constant State." + name);
	}
}
