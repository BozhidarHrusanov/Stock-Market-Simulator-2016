package main;

import gameLogic.Card;
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
	/* if readyForNextRound is not volatile its value
	 * is not correctly read by other threads */
	private volatile boolean readyForNextRound = false;
	private boolean connected = true;
	private String clientInput = "";
	private String buffer = "";
	/*contains the initials of the company whose shares were sold */
	private char companySharesSold;
	/*contains the string result of the shares selling phase */
	private String sharesSoldReport;
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
				String rawInput = ""; // not yet verified to be used as clientInput
				do {
					// the server message is based on the game phase
					networkingOUT.println(buffer + "<br>" + assignQuestion());
					buffer = "";
					rawInput = networkingIN.nextLine().trim();
				} while (!isInputValid(rawInput));
				/* the setter method is used to ensure atomicity */
				setClientInput(rawInput);
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
		StringBuffer sb = new StringBuffer();
		switch (session.getGameState()) {
		case TRADING_SELL:
			sb.append(printPhaseBanner());
			sb.append("The shares you own are:<br>");
			sb.append(player.displayPlayerShares());
			sb.append("<br>" + "Your money balance is: " + player.getMoney());
			sb.append("<br>The cards in your hand are:<br>" + player.printCardsInHand());
			sb.append("Enter the company's first letter followed by the amount of stocks<br>");
			sb.append("you wish to sell (ex. 'G3' - sells 3 Google stocks; 'A0' - sells nothing)");
			return sb.toString();
		case TRADING_BUY:
			sb.append("Enter the company's first letter followed by the amount of stocks<br>");
			sb.append("you wish to buy (ex. 'G3' - buys 3 Google stocks; 'A0' - buys nothing)");
			return sb.toString();
		case BIDDING:
			sb.append(printPhaseBanner());
			sb.append(player.displayPlayerShares());
			sb.append("<br>" + "Your money balance is: " + player.getMoney());
			sb.append("<br>The cards in your hand are:<br>" + player.printCardsInHand());
			sb.append("Place your bid for the card (ex. '15' or '0' to skip bidding):<br>");
			sb.append("Bid for: " + Card.getCardOnTable());
			 return sb.toString();
		case CARD_PLAY:
			sb.append(printPhaseBanner());
			sb.append("Enter the number of the card you wish to play:<br>");
			sb.append(player.printCardsInHand());
			return sb.toString();
		case GAME_OVER:
			sb.append(printPhaseBanner());
			return sb.toString();
		default:
			return "invalid phase";
		}
	}
	
	/* returns whether the input is valid,
	 * and if it is executes the corresponding method */
	private boolean isInputValid(String message) {
		if (message.isEmpty()) {
			return false;
		}
		switch (session.getGameState()) {
		case TRADING_SELL:
			if (message.charAt(0) != 'A' && message.charAt(0) != 'C'
					&& message.charAt(0) != 'G' && message.charAt(0) != 'M') {
				buffer = "Company initials is incorrect!";
				return false;
			}
			
			/* try to parse the 2nd char in the client msg to string and then
			 * parse that to and int. If an Exception is thrown then the input
			 * is not valid. */
			int amountOfShares;
			try {
				amountOfShares = Integer.parseInt(message.substring(1));
			} catch (NumberFormatException ex) {
				buffer = "Amount of shares to be sold incorrect!";
				return false;
			}
			if (amountOfShares < 0) {
				buffer = "Amount of shares to be sold MUST be equal or bigger than 0!";
				return false;
			}
			if (amountOfShares > player.getSharesAmount(message.charAt(0))) {
				buffer = "You tried to sell " + amountOfShares + " shares, but"
						+ " you only have " + player.getSharesAmount(message.charAt(0))
						+ " for that company!";
				return false;
			}
			
			return true;
			
		case TRADING_BUY:
			if (message.charAt(0) != 'A' && message.charAt(0) != 'C'
					&& message.charAt(0) != 'G' && message.charAt(0) != 'M') {
				buffer = "Company initals is incorrect!";
				return false;
			}
			if (companySharesSold == message.charAt(0)){
				buffer = "You are not allowed to buy and sell shares from the"
						+ " same company in the same turn!";
				return false;
			}
			int singleSharePrice = Stock.parseStock(message.charAt(0)).price;
			int sharesAmount;
			/* try to parse the 2nd char in the client msg to string and then
			 * parse that to an int. If an Exception is thrown then the input
			 * is not valid. */
			try {
				sharesAmount = Integer.parseInt(message.substring(1));
			} catch (NumberFormatException ex) {
				buffer = "Amount of shares to be sold incorrect!";
				return false;
			}
			if (sharesAmount < 0) {
				buffer = "Amount of shares to be sold MUST be equal or bigger than 0!";
				return false;
			}
			if (player.getMoney() < (singleSharePrice * sharesAmount) + sharesAmount) {
				buffer = "You have insufficient funds to buy the amount of shares you have specified!<br>"
						+ "You attempted to buy " +  sharesAmount + " shares, each at a price of "
						+ singleSharePrice + " pounds.<br>That makes a total of "
						+ (singleSharePrice * sharesAmount + sharesAmount) + " pounds (fees included).";
				return false;
			}
			
			return true;
			
		case BIDDING:
			int bid;
			try {
				bid = Integer.parseInt(message.substring(0));
			} catch (NumberFormatException ex) {
				buffer = "You should input a numeric value!";
				return false;
			}
			if (bid > player.getMoney()) {
				buffer = "You do not have enough money!";
				return false;
			}
			if (bid < 0) {
				buffer = "Your bid cannot be negative!";
				return false;
			}
			
			//set the bid variable for the player
			player.setBid(bid);
			
			return true;
			
		case CARD_PLAY:
			int cardNum;
			try {
				cardNum = Integer.parseInt(message.substring(0));
			} catch (NumberFormatException ex) {
				buffer = "You should input a numeric value!";
				return false;
			}
			if (cardNum <= 0 || cardNum > player.getNumberOfCardsInHand()) {
				buffer = "The card number you specified is invalid: " + cardNum;
				return false;
			}
			
			return true;
			
		default:
			System.out.println("invalid phase");
			break;
		}
		return false;
	}

	/* sells the shares at the corresponding price, taking into account
	 * the 1 pound fee for each share sold. */
	public void sellShares() {
		int amountOfShares = Integer.parseInt(clientInput.substring(1));
		player.modifyMoney((Stock.parseStock(clientInput.charAt(0)).price
				* amountOfShares) - (amountOfShares));
		player.modifyShares(clientInput.charAt(0), -amountOfShares);
		companySharesSold = clientInput.charAt(0);
		sharesSoldReport = "<br>" + player.getName() + " sold "
				+ amountOfShares + " " + Stock.initialsToCompanyName(clientInput.charAt(0))
				+ " share(s).";
		
	}
	
	/* remove the cost of the shares + fees from the player's money and
	 * give the player the shares. Returns a report on the buying and
	 * the current money of the player */
	public String buyShares() {
		int singleSharePrice = Stock.parseStock(clientInput.charAt(0)).price;
		int sharesAmount = Integer.parseInt(clientInput.substring(1));
		player.modifyMoney( -(singleSharePrice * sharesAmount + sharesAmount));
		player.modifyShares(clientInput.charAt(0), sharesAmount);
		companySharesSold = ' '; //reset now in case player doesn't sell next turn
		
		return player.getName() + " bought " + sharesAmount + " "
				+ Stock.initialsToCompanyName(clientInput.charAt(0))
				+ " shares.<br>" + printPlayerMoneyReport();
	}
	

	public int askGameMode() {
		networkingOUT.println("Select the number of total players to play"
				+ " the game and press ENTER.<br>2, 3 or 4");
		String result = networkingIN.nextLine().trim();
		for (int i = 2; i <= 4; i++) {
			if (result.contains(String.valueOf(i))) {
				return i;
			}
		}
		System.out.println("Game mode '2 players' chosen because input was invalid!");
		return 2;
	}

	public boolean askBots() {
		networkingOUT.println("Do you want bots - "
				+ "choose \"yes\" or \"no\" and press ENTER.");
		return networkingIN.nextLine().trim().contains("yes") ? true : false;
	}

	public void sendMessageToClient(String msg) {
		networkingOUT.println(msg);
	}
	
	public void sendBufferToClient() {
		networkingOUT.println(buffer);
	}
	
	public String printPlayerMoneyReport() {
		return player.getName() + "'s current money balance is "
				+ player.getMoney() + " pounds.";
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
	
	//returns the name of the played card as a string
	public String playSelectedCard() {
		Card card = player.getCardByPlayerInput(
				Integer.parseInt(clientInput.substring(0)));
		card.execute();
		player.removeCardFromHand(card);
		return card.name;
	}

	public String getSharesSoldReport() {
		return sharesSoldReport;
	}

	/* The buffer is appended to the front of the next message to be sent to a
	 * client. The buffer is emptied after the message has been sent. */
	public synchronized void addToBuffer(String text) {
		buffer += text;
	}

	public Player getPlayer() {
		return player;
	}

	public synchronized boolean isReadyForNextRound() {
		return readyForNextRound;
	}
	
	private String printPhaseBanner(){
		switch (session.getGameState()) {
		case TRADING_SELL:
			return "===============================================<br>"
				+ "\tRound " + session.getCurrentRound()
				+ " TRADING PHASE HAS BEGUN<br>"
				+ "===============================================<br>";
		case BIDDING:
			return "===============================================<br>"
				+ "\tRound " + session.getCurrentRound()
				+ " CARD BIDDING PHASE HAS BEGUN<br>"
				+ "===============================================<br>";
		case CARD_PLAY:
			return "===============================================<br>"
				+ "\tRound " + session.getCurrentRound()
				+ " CARD PLAYING PHASE HAS BEGUN<br>"
				+ "===============================================<br>";
		case GAME_OVER:
			return "===============================================<br>"
				+ "\t\tTHE GAME IS OVER<br>"
				+ "===============================================<br>";
		default:
			return "invalid phase banner";
		}
	}
	
}
