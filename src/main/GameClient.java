package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class GameClient {
	public static final String SERVER = "localhost"; // "82.30.6.34"
	private Scanner networkingIN;
	private PrintWriter networkingOUT;
	private Socket socket;
	private Scanner userInput = new Scanner(System.in);
	private boolean connectedToServer = true;

	public void init() throws UnknownHostException, IOException {
		try {
			socket = new Socket(SERVER, GameServer.PORT);
		} catch (ConnectException ce) {
			System.out.println("The server program in not running!");
			System.exit(0);
		}
		@SuppressWarnings("resource")
		InputStream instream = socket.getInputStream();
		OutputStream outstream = socket.getOutputStream();
		networkingIN = new Scanner(instream);
		networkingOUT = new PrintWriter(outstream, true);
		System.out.println("connected to " + SERVER + ":" + GameServer.PORT);
		// receive the initial server response
		System.out.println(networkingIN.nextLine());
	}

	public static void main(String[] args) throws IOException {
		System.out.println("client version 0.5");
		GameClient gameClient = new GameClient();
		gameClient.init();
		while (gameClient.isConnectedToServer()) {
			gameClient.getAskedAndAnswerServer();
		}
		gameClient.socket.close();
	}

	/* The main tasks of the client application: get asked
	 * by the server and then send the client's input to
	 * the server.*/
	public void getAskedAndAnswerServer() {
		String serverMsg = networkingIN.nextLine();
		// fixes new line on client output console
		serverMsg = serverMsg.replace("<br>", "\n");
		System.out.println(serverMsg);

		/* All special server responses start with 'Error:...'.
		 * A "game is over" message signals that the server
		 * doesn't expect a response from the client. */
		if (serverMsg.contains("Error:") || serverMsg.contains("game is over")) {
			connectedToServer = false;
			return;
		}
		String message = userInput.nextLine();
		networkingOUT.println(message);
	}

	public boolean isConnectedToServer() {
		return connectedToServer;
	}
}
