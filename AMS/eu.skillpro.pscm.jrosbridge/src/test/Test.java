package test;

import java.util.concurrent.ExecutionException;

import javax.json.Json;

import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Service;
import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.Message;
import edu.wpi.rail.jrosbridge.services.ServiceRequest;
import edu.wpi.rail.jrosbridge.services.ServiceResponse;

public class Test {
	
	public static void main(String[] args) throws InterruptedException {
	    Ros ros = new Ros("192.168.1.3", 9020);
	    ros = new Ros("echo.websocket.org", 80);
	    System.out.println("Trying to connect");
//	    
//	    WebSocket ws = null;
//		try {
//			ws = WebSocketFactory.createWebSocketFactory().createWebSocket(new URI(("ws://echo.websocket.org:80")));
//		} catch (URISyntaxException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		System.out.println("kazing: " + ws);
//	    try {
//			ws.connect();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	    
	    
//	    try {
//			ContainerProvider.getWebSocketContainer().connectToServer(new Endpoint() {
//				
//				@Override
//				public void onOpen(Session arg0, EndpointConfig arg1) {
//					System.out.println("hurray");
//					
//				}
//			}, new URI(("ws://echo.websocket.org")));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	    System.out.println("trying for real now");
	    ros.connect();
	    try {
			System.out.println("Connected: " + ros.getFutureSession().get());
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//	    JsonObject object = Json.createObjectBuilder().add("op", "call_service")
//	    		.add("service", "/std_msgs")
//	    		.add("id", "randomID").build();
//		
//		ros.send(object);
//	    Topic echo = new Topic(ros, "/echo", "std_msgs/String");
//	    Message toSend = new Message("{\"data\": \"hello, world!\"}");
//	    echo.publish(toSend);
////
//	    Topic echoBack = new Topic(ros, "/echo_back", "std_msgs/String");
//	    echoBack.subscribe(new TopicCallback() {
//	        @Override
//	        public void handleMessage(Message message) {
//	            System.out.println("From ROS: " + message.toString());
//	        }
//	    });
//
	    
	    
	    Service trajectoryListService = new Service(ros, "/svh/pathloader/trajectoryList", "fzi_manipulation_msgs/path");

	    ServiceRequest trajectoryListRequest = new ServiceRequest(Json.createObjectBuilder().add("object", "").add("group", "").build());
	    ServiceResponse trajectoryListResponse = trajectoryListService.callServiceAndWait(trajectoryListRequest);
	    System.out.println("Response Traj List: " + trajectoryListResponse.toString());
	    
	    Service loadTrajectoryService = new Service(ros, "/svh/pathloader/loadTrajectory", "fzi_manipulation_msgs/path");

	    ServiceRequest loadTrajectoryRequest = new ServiceRequest(Json.createObjectBuilder().add("object", "null_stellung.traj").add("group", "").build());
	    ServiceResponse loadTrajectoryResponse = loadTrajectoryService.callServiceAndWait(loadTrajectoryRequest);
	    System.out.println("Response: " + loadTrajectoryResponse.toString());
	    
	    System.out.println("Sending topic!");
	    Topic echo = new Topic(ros, "/svh/pathloader/goal", "fzi_manipulation_msgs/PlayTrajectoryActionGoal");
	    Message toSend = new Message(Json.createObjectBuilder()
	    		.add("header", Json.createObjectBuilder()
	    				.add("seq", 0)
	    				.add("stamp", Json.createObjectBuilder().add("secs", 0).add("nsecs", 0))
	    				.add("frame_id", "")
	    				.build())
	    		.add("goal_id", Json.createObjectBuilder()
	    				.add("stamp", Json.createObjectBuilder().add("secs", 0).add("nsecs", 0))
	    				.build())
	    		.add("goal", Json.createObjectBuilder()
	    				.add("mode", 0)
	    				.add("repeat", false)
	    				.add("subtrajectory_start_index", 0)
	    				.add("subtrajectory_end_index", 0)
	    				.build())
	    		.build());
	    
	    echo.publish(toSend);
	    
	    ros.disconnect();
	    System.out.println("disconnected");
	}

}
