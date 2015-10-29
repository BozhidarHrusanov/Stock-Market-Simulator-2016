import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameService implements Runnable {

	private Socket socket;
	private Scanner networkingIN;
	private PrintWriter networkingOUT;
	private boolean readyForNextRound = false;
	private boolean connected = true;
	private String clientInput = "empty";
	private String buffer = "";
	private GameSession session;

	public GameService(Socket socket) {
		this.socket = socket;
		try {
			networkingIN = new Scanner(socket.getInputStream());
			networkingOUT = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (connected) {
			if (readyForNextRound) {
				//the server message is based on the game phase
				String currentQuestion = assignQuestion();
				networkingOUT.println(buffer + "<br>" + currentQuestion);
				clientInput = networkingIN.nextLine().trim();
				buffer = "";
				readyForNextRound = false;
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String assignQuestion() {
		switch (session.getGameState()) {
		case "tradingPhaseSell":
			return "Enter the company's first letter followed by the amount"
			+ "of stocks<br>you wish to sell (ex. 'G 3' - this sells 3 Google stocks):";
		case "tradingPhaseBuy":
			return "Enter the company's first letter followed by the amount"
					+ "of stocks<br>you wish to buy (ex. 'G 3' - this buys 3 Google stocks):";
		case "playCardPhase":
			return "Choose the number of the card you wish to play: "
					+ "--print out the players cards here--";
		case "biddingPhase":
			return "Place your bid for the card (card name)(ex. '15' or '0' to skip bidding): ";
		default:
			return "invalid phase";
		}
	}

	// control input at client total
	public int askGameMode() {
		networkingOUT.println("Select the number of total players to play"
				+ " the game and press ENTER.<br>2, 3 or 4");
		String result = networkingIN.nextLine().trim();
		for (int i = 2; i <= 4; i++){
			if (result.contains(String.valueOf(i))){
				return i;
			}
		}
		return 2;
	}
	
	public boolean askBots() {
		networkingOUT.println("Do you want bots - choose \"yes\" or \"no\" and press ENTER.");
		return networkingIN.nextLine().trim().contains("yes") ? true : false;
	}
	
	public void sendMessageToClient(String msg) {
		networkingOUT.println(msg);
	}
	
	/* The frontMsg is appended to the front of the next message to
	 * be sent to a client. The buffer is emptied after the
	 * message has been sent.
	 */
	public void addToMessageBufferToClient(String frontMsg) {
		buffer = frontMsg;
	}
	
	public String getClientInput() {
		return clientInput;
	}

	public synchronized void setClientInput(String clientInput) {
		this.clientInput = clientInput;
	}

	public synchronized void setReadyForNextRound(boolean readyForNextRound) {
		this.readyForNextRound = readyForNextRound;
	}
	
	public synchronized void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public synchronized void setSession(GameSession session) {
		if (this.session == null) {
			this.session = session;
		} else {
			System.out.println("WARNING: Session reassignment attempted!");
		}
	}
}
