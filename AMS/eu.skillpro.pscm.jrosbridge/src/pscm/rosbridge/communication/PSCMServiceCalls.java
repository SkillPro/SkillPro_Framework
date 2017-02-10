package pscm.rosbridge.communication;

import javax.json.Json;
import javax.json.JsonObject;

import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Service;
import edu.wpi.rail.jrosbridge.services.ServiceRequest;
import edu.wpi.rail.jrosbridge.services.ServiceResponse;

public class PSCMServiceCalls {
	
	private PSCMServiceCalls() {
		
	}
	
	public static JsonObject callTrajectoryList(Ros rosClient) {
		 Service service = new Service(rosClient, "/svh/pathloader/trajectoryList", "fzi_manipulation_msgs/path");

		 ServiceRequest request = new ServiceRequest(Json.createObjectBuilder().add("object", "").add("group", "").build());
		 ServiceResponse response = service.callServiceAndWait(request);
		 
		 return response.toJsonObject();
	}

	public static JsonObject callLoadTrajectory(Ros rosClient, String trajectoryName) {
		 Service service = new Service(rosClient, "/svh/pathloader/loadTrajectory", "fzi_manipulation_msgs/path");

		 ServiceRequest request = new ServiceRequest(Json.createObjectBuilder().add("object", trajectoryName).add("group", "").build());
		 ServiceResponse response = service.callServiceAndWait(request);
		 
		 return response.toJsonObject();
	}
	
}
