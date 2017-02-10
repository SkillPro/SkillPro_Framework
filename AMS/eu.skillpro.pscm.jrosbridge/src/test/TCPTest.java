package test;

public class TCPTest {

	public static void main(String[] args) throws Exception {
//		URL oracle = new URL("http://www.google.com");
//        URLConnection yc = oracle.openConnection();
//        BufferedReader in = new BufferedReader(new InputStreamReader(
//                                    yc.getInputStream()));
//        String inputLine;
//        while ((inputLine = in.readLine()) != null) 
//            System.out.println(inputLine);
//        in.close();
//		URL url = new URL("http://192.168.1.3:9020");
//		URLConnection connection = url.openConnection();
//        connection.setDoOutput(true);
//
//        OutputStreamWriter out = new OutputStreamWriter(
//                                         connection.getOutputStream());
//        
//        List<String> arguments = new ArrayList<>();
//        
//        
//        CallServiceTO message = new CallServiceTO("test-ID", "nested_srv", arguments, 256, "none");
////        out.write(JSONUtility.convertToJSON(message));
////        String testString = "{ \"op\" : \"call_service\", \"service\": \"/nested_srv\", \"fragment_size\": 1000, \"message_intervall\": 0.0, \"args\": { \"pose\": {\"position\": {\"y\": 0.0, \"x\": 0.0, \"z\": 0.0}, \"orientation\": {\"y\": 0.0, \"x\": 0.0, \"z\": 0.0, \"w\": 0.0}}} +" +
////        		"#\"count\" : 5000 }";
////        String testString = "{\"op\": \"publish\", \"id\": \"testID\", \"topic\": \"/svh/interpolator/start_stop\", \"msg\": \"std_msgs/Bool\" , \"data\": \"true\"}";
//        out.write(testString);
//        System.out.println(testString);
//        out.close();
//
//        BufferedReader in = new BufferedReader(
//                                    new InputStreamReader(
//                                    connection.getInputStream()));
//        String decodedString;
////        while ((decodedString = in.readLine()) != null) {
////            System.out.println(decodedString);
////        }
//        in.close();
		
		
		
//		String testString = "{\"op\": \"publish\", \"id\": \"testID\", \"topic\": \"/svh/interpolator/start_stop\", \"msg\": \"std_msgs/Bool\" , \"data\": \"true\"}";
//		Socket echoSocket = new Socket("192.168.1.3", 9020);
//		PrintWriter out =
//				new PrintWriter(echoSocket.getOutputStream(), true);
//		BufferedReader in =
//				new BufferedReader(
//						new InputStreamReader(echoSocket.getInputStream()));
//
//		
//		
//		JsonObject object = new JsonObject();
//		object.addProperty("op", "publish");
//		object.addProperty("topic", "/start_stop");
//		
//		JsonObject msg = new JsonObject();
//		msg.addProperty("data", "test publish");
//		
//		object.add("msg", msg);
////		object.addProperty("data", true);
//		
//		object = new JsonObject();
//		String test = "";
//		object.addProperty("op", "call_service");
//		object.addProperty("service", "/rosapi/topics");
//
//		System.out.println(object);
//		out.println(object);
////		System.out.println("response: " + in.readLine());
//		
//		object = new JsonObject();
//		
//		object.addProperty("op", "call_service");
//		object.addProperty("service", "/rosout/set_logger_level");
//		
//		JsonObject rosLevel = new JsonObject();
//		rosLevel.addProperty("logger", "ros");
//		rosLevel.addProperty("level", "warn");
//		object.add("args", rosLevel);
////		object.addProperty("values", test);
//		
//		
//
//		
//		System.out.println(object);
//		out.println(object);
//		
//		object = new JsonObject();
//		
//		object.addProperty("op", "call_service");
//		object.addProperty("service", "rosout/set_logger_level");
//		
//		object.add("args", rosLevel);
////		object.addProperty("values", test);
//		
//		
//
//		
//		System.out.println(object);
//		out.println(object);
//		
//		String read;
//		while ((read = in.readLine()) != null) {
//			System.out.println("response: " + read);
//			
//		}
		
//		BufferedReader stdIn =
//				new BufferedReader(
//						new InputStreamReader(System.in));
//		String userInput;
//		while ((userInput = stdIn.readLine()) != null) {
//		    out.println(userInput);
//		    System.out.println("echo: " + in.readLine());
//		}
	}
	
}
