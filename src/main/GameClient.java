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

	public void getAskedAndAnswerServer() {
		String serverMsg = networkingIN.nextLine();
		//formatMessage(serverMsg);
		// fix new line on client output console
		serverMsg = serverMsg.replace("<br>", "\n");
		System.out.println(serverMsg);

		// all special server responses should start with 'Error:...'
		if (serverMsg.contains("Error:")) {
			connectedToServer = false;
			return;
		}

		String message = userInput.nextLine();
		networkingOUT.println(message);
		/*
		 * //if a 'quit' message was sent, stop looping if
		 * (message.equals("quit")){ connectedToServer = false; }
		 */
	}
	
	/* if the message informs the player of a new phase then beautify it*/
	private void formatMessage(String msg) {
		if (msg.startsWith("PHASE:")) {
			String result = "*********************************************"
					+ "\t\t" + msg.substring(7) + "\t\t"
					+ "*********************************************";
			msg = result;
		}
	}

	public boolean isConnectedToServer() {
		return connectedToServer;
	}
}
