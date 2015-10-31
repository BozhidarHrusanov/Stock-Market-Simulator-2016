package Main;

import gameLogic.Player;
import gameLogic.Stock;

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
	private char companySharesSold;
	private GameSession session;
	private Player player;

	public GameService(Socket socket) {
		this.socket = socket;
		player = new Player();
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
				String rawInput = "";
				do {
					// the server message is based on the game phase
					networkingOUT.println(buffer + "<br>" + assignQuestion());
					buffer = "";
					rawInput = networkingIN.nextLine().trim();
				} while (!isInputValid(rawInput));
				clientInput = rawInput;
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
					+ "of stocks<br>you wish to sell (ex. 'G3' - this sells 3 Google stocks):";
		case "tradingPhaseBuy":
			return "Enter the company's first letter followed by the amount"
					+ "of stocks<br>you wish to buy (ex. 'G3' - this buys 3 Google stocks):";
		case "playCardPhase":
			return "Choose the number of the card you wish to play: "
					+ "--print out the players cards here--";
		case "biddingPhase":
			return "Place your bid for the card (card name)(ex. '15' or '0' to skip bidding): ";
		default:
			return "invalid phase";
		}
	}

	private boolean isInputValid(String msg) {
		msg += " ";
		switch (session.getGameState()) {
		case "tradingPhaseSell":
			if (msg.charAt(0) != 'A' || msg.charAt(0) != 'C'
					|| msg.charAt(0) != 'G' || msg.charAt(0) != 'M') {
				buffer = "Company initials is incorrect!";
				return false;
			}
			/*
			 * try to parse the 2nd char in the client msg to string and then
			 * parse that to and int. If an Exception is thrown then the input
			 * is not valid.
			 */
			try {
				Integer.parseInt(msg.substring(1, 2));
			} catch (NumberFormatException ex) {
				buffer = "Amount of shares to be sold incorrect!";
				return false;
			}
			if (Integer.parseInt(msg.substring(1, 2)) < 0) {
				buffer = "Amount of shares to be sold MUST be equal or bigger than 0!";
				return false;
			}
			//sells the shares at the corresponding price, taking into account the 1£ fee for each share sold.
			player.addToMoney((Stock.parseStock(msg.charAt(0)).price
					* Integer.parseInt(msg.substring(1, 2))) - (1 * Integer.parseInt(msg.substring(1, 2))));
			player.
			companySharesSold = msg.charAt(0);
			return true;
			
		case "tradingPhaseBuy":
			if (msg.charAt(0) != 'A' || msg.charAt(0) != 'C'
					|| msg.charAt(0) != 'G' || msg.charAt(0) != 'M') {
				buffer = "Company initals is incorrect!";
				return false;
			}
			if (companySharesSold == msg.charAt(0)){
				buffer = "You are not allowed to buy and sell shares from the same company in the same turn!";
				return false;
			}
			if (player.getMoney() < Stock.parseStock(msg.charAt(0)).price * Integer.parseInt(msg.substring(1, 2))) {
				buffer = "You have insufficient funds to buy the amount of shares you have specified!<br>"
						+ "You attempted to buy " +  Integer.parseInt(msg.substring(1, 2)) + "shares, each at a price of "
						+ Stock.parseStock(msg.charAt(0)).price + ". That makes a total of "
						+ Stock.parseStock(msg.charAt(0)).price * Integer.parseInt(msg.substring(1, 2)) + " £.";
				return false;
			}
			/*
			 * try to parse the 2nd char in the client msg to string and then
			 * parse that to and int. If an Exception is thrown then the input
			 * is not valid.
			 */
			try {
				Integer.parseInt(msg.substring(1, 2));
			} catch (NumberFormatException ex) {
				buffer = "Amount of shares to be sold incorrect!";
				return false;
			}
			if (Integer.parseInt(msg.substring(1, 2)) < 0) {
				buffer = "Amount of shares to be sold MUST be equal or bigger than 0!";
				return false;
			}
			
			return true;
		case "playCardPhase":
			break;
		case "biddingPhase":
			break;
		default:
			System.out.println("invalid phase");
			break;
		}
		return false;
	}

	// control input at client total
	public int askGameMode() {
		networkingOUT.println("Select the number of total players to play"
				+ " the game and press ENTER.<br>2, 3 or 4");
		String result = networkingIN.nextLine().trim();
		for (int i = 2; i <= 4; i++) {
			if (result.contains(String.valueOf(i))) {
				return i;
			}
		}
		return 2;
	}

	public boolean askBots() {
		networkingOUT
				.println("Do you want bots - choose \"yes\" or \"no\" and press ENTER.");
		return networkingIN.nextLine().trim().contains("yes") ? true : false;
	}

	public void sendMessageToClient(String msg) {
		networkingOUT.println(msg);
	}

	/*
	 * The frontMsg is appended to the front of the next message to be sent to a
	 * client. The buffer is emptied after the message has been sent.
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
