import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class GameClient {
	public static final String SERVER = "localhost"; //"82.30.6.34"
	private Scanner networkingIN;
	private PrintWriter networkingOUT;
	private Socket socket;
	private Scanner userInput = new Scanner(System.in);
	private boolean connectedToServer = true;
	
	public void init() throws UnknownHostException, IOException {
		try{
		socket = new Socket(SERVER, GameServer.PORT);
		} catch (ConnectException ce){
			System.out.println("The server program in not running!");
			System.exit(0);
		}
		@SuppressWarnings("resource")
		InputStream instream = socket.getInputStream();
		OutputStream outstream = socket.getOutputStream();
		networkingIN = new Scanner(instream);
		networkingOUT = new PrintWriter(outstream, true);
		System.out.println("connected to " + SERVER + ":" + GameServer.PORT);
		//receive the initial server response
		System.out.println(networkingIN.nextLine());
	}
	
	public static void main(String[] args) throws IOException {
		GameClient gameClient = new GameClient();
		gameClient.init();
		while (gameClient.isConnectedToServer()) {
			gameClient.getAskedAndAnswerServer();
		}
		gameClient.socket.close();
	}
	
	public void getAskedAndAnswerServer() {
		String serverMsg = networkingIN.nextLine();
		/*if (serverMsg.equals("other_quit")){
			connectedToServer = false;
			System.out.println("The other player has quit the game.");
			return;
		}
		//fix new line on client output console*/
		serverMsg = serverMsg.replace("<br>", "\n");
		System.out.println(serverMsg);
		String message = userInput.nextLine();
		networkingOUT.println(message);
		/*//if a 'quit' message was sent, stop looping
		if (message.equals("quit")){
			connectedToServer = false;
		}*/
	}

	public boolean isConnectedToServer() {
		return connectedToServer;
	}
}
