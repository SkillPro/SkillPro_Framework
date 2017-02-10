package pscm.rosbridge.communication;

import javax.json.Json;

import edu.wpi.rail.jrosbridge.Ros;
import edu.wpi.rail.jrosbridge.Topic;
import edu.wpi.rail.jrosbridge.messages.Message;

public class PSCMTopicPublishes {
	
	private PSCMTopicPublishes() {
		
	}
	
	public static void publishPlayTrajectory(Ros rosClient) {
		Topic topic = new Topic(rosClient, "/svh/pathloader/goal", "fzi_manipulation_msgs/PlayTrajectoryActionGoal");
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
	    
	    topic.publish(toSend);
	}

}
