import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
	public static final int PORT = 8888;
	private GameSession gameSession;

	public static void main(String[] args) throws IOException {
		new GameServer().play();
	}

	public void play() throws IOException {
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("Started GamingServer at port " + PORT);
		System.out.println("Waiting for clients to connect...");

		while (true) {
			@SuppressWarnings("resource")
			Socket socket = serverSocket.accept();
			System.out.println("Client connected.");
			createSession(new GameService(socket));
		}
	}

	public void createSession(GameService service) {
		if (gameSession == null) {
			gameSession = new GameSession(service);
		} else {
			gameSession.addPlayer(service);
		}
		
	}
}
