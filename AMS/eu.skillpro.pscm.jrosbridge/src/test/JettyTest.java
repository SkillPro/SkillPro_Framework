package test;

import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class JettyTest {
	
	public static void main(String[] args) {
		String destUri = "ws://echo.websocket.org";
		if (args.length > 0) {
			destUri = args[0];
		}
		WebSocketClient client = new WebSocketClient();
		SimpleEchoSocket socket = new SimpleEchoSocket();
		try {
//            client.;
			client.start();
			URI echoUri = new URI(destUri);
//            Client request = new ClientUpgradeRequest();
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			System.out.println("trying");
			Future<Session> session = client.connect(socket, echoUri, request);
			System.out.printf("Connecting to : %s%n", echoUri);
			
//			System.out.println(session);
			System.out.println(session.get().getLocalAddress());
			System.out.println("Remote: " + session.get().getRemoteAddress());
			session.get().getRemote().sendString("HELLLLLLLLLO");
//            socket.awaitClose(5, TimeUnit.SECONDS);
		} catch (Throwable t) {
			System.out.println("ERRORRROORORORO");
			t.printStackTrace();
		} finally {
			try {
				client.stop();
				System.out.println("End");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

