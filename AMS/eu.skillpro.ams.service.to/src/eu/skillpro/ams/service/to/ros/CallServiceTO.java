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

package eu.skillpro.ams.service.to.ros;

import java.util.ArrayList;
import java.util.List;

public class CallServiceTO {
	//Not static, because it can't be written as OP because of GSON.toJson();
	private final String op = "call_service";
	private String id;
	private String service;
	private List<String> args = new ArrayList<>();
	//called fragment_size because of the ROS protocol
	private int fragment_size;
	private String compression;
	
	public CallServiceTO(String id, String service, List<String> args, int fragment_size,
			String compression) {
		super();
		this.id = id;
		this.service = service;
		this.args.addAll(args);
		this.fragment_size = fragment_size;
		this.compression = compression;
	}
	
	public List<String> getArgs() {
		return args;
	}
	
	public String getCompression() {
		return compression;
	}
	
	public int getFragment_size() {
		return fragment_size;
	}
	
	public String getId() {
		return id;
	}
	
	public String getOp() {
		return op;
	}
	
	public String getService() {
		return service;
	}
	
	@Override
	public String toString() {
		return "call_service [id=" + id + ", args=" + args + ", service="
				+ service + ", fragment_size=" + fragment_size + ", compression=" + compression + "]";
	}
}
