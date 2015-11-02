package main;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion.Setting;

import gameLogic.Card;
import gameLogic.GameStates;
import gameLogic.Stock;

public class GameSession implements Runnable {
	
	private GameService[] services = new GameService[4];
	private int numberOfServices;
	private boolean botsON;
	//if a player disconnects the match thread ends execution
	private boolean matchOngoing = true;
	private GameStates gameState = GameStates.SETUP;

	public GameSession(GameService gameServicePlayer1) {
		this.services[0] = gameServicePlayer1;
		new Thread(services[0]).start();
		System.out.println("Waiting for other players to connect...");
		services[0].sendMessageToClient("Waiting for other players to connect...");
		numberOfServices = services[0].askGameMode();
		botsON = services[0].askBots();
		this.services[0].setSession(this);
	}
	
	public void addPlayer(GameService otherPlayerSevice) {
		int freeSlotIndex = 0;
		//find a free slot for the new player
		for (int i = 1; i < numberOfServices; i++) {
			if (services[i] == null) {
				freeSlotIndex = i;
				break;
			}
		}
		//if no free slot is found
		if (freeSlotIndex == 0) {
			otherPlayerSevice.sendMessageToClient("Error: no free slot for you, sorry.");
			System.out.println("Player tried to join, but there is no free slot.");
			return;
		}

		//put the player in the free slot and start its service thread
		services[freeSlotIndex] = otherPlayerSevice;
		services[freeSlotIndex].setSession(this);
		new Thread(this.services[freeSlotIndex]).start();
		System.out.println("playerService" + freeSlotIndex +" thread started!");
		services[freeSlotIndex].sendMessageToClient("You joined a session!");
		System.out.println("Game session has "
					+ (freeSlotIndex+1) + "/" + numberOfServices
					+ " players in.");
		
		//if this is the last player - start game session thread
		if (freeSlotIndex == (numberOfServices - 1)) { 
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			assignPlayerNames();
			new Thread(this).start();
			System.out.println("Match thread started!");
		}
	}
	
	@Override
	public void run() {
		setGameState(GameStates.TRADING_SELL);
		while (matchOngoing) {
			for (int i = 0; i < numberOfServices; i++) {
				services[i].setReadyForNextRound(true);
			}
			
			/* loop execution until valid input has been
			 * received by all players. Loops logic explained:
			 * Suppose the correct input from all players
			 * has been received; if one or more players
			 * are actually not yet ready then the while
			 * condition still holds true and continues looping.*/
			boolean allInputsReceived = false;
			while (!allInputsReceived) {
				allInputsReceived = true;
				for (int i = 0; i < numberOfServices; i++) {
					if (services[i].getClientInput().equals("")) {
						allInputsReceived = false;
					}
				}
			}
			
			for (int i = 0; i < numberOfServices; i++) {
				System.out.println(services[i].getClientInput());
			}
			forceGameLogic();
		}
	}

	/* force the game logic depending on the current game state */
	private void forceGameLogic() {
		switch (gameState) {
			case TRADING_SELL:
				for (int i = 0; i < numberOfServices; i++) {
					services[i].sellShares();
				}
				clearAllServicesClientInput();
				setGameState(GameStates.TRADING_BUY);
				break;
			case TRADING_BUY:
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < numberOfServices; i++) {
					sb.append(services[i].getSharesSoldReport() + "<br>");
					sb.append(services[i].buyShares());
				}
				for (int j = 0; j < numberOfServices; j++) {
					services[j].addToBuffer(sb.toString());
				}
				clearAllServicesClientInput();
				setGameState(GameStates.BIDDING);
				break;
			case BIDDING:
				//find the services index of the bidder with the highest bid
				int highestBidderIndex = 0;
				for (int i = 0; i < numberOfServices; i++) {
					if (services[i].getPlayer().getBid() > services[highestBidderIndex].getPlayer().getBid()
							&& i != highestBidderIndex) {
						highestBidderIndex = i;
					}
				}
				//check if there are more than one highest bids
				boolean identicalHighestBids = false;
				for (int j = 0; j < numberOfServices; j++) {
					if (services[j].getPlayer().getBid() == services[highestBidderIndex].getPlayer().getBid()
							&& j != highestBidderIndex) {
						//another bidder has the highest bid
						identicalHighestBids = true;
					}
				}
				
				StringBuffer sb1 = new StringBuffer();
				for (int i = 0; i < numberOfServices; i++) {
					sb1.append(services[i].getPlayer().getName());
					sb1.append(" placed a bid of ");
					sb1.append(services[i].getPlayer().getBid());
					sb1.append("<br>");
				}
				//if the highest bid is unique
				if (!identicalHighestBids) {
					services[highestBidderIndex].getPlayer().substractBid();
					services[highestBidderIndex].getPlayer().addCardToHand(
							Card.getAndRemoveCardOnTable());
					sb1.append(services[highestBidderIndex].getPlayer().getName());
					sb1.append(" won the bid!<br>The new card on the table is:\t");
					sb1.append(Card.getCardOnTable());
				} else {
					sb1.append("There was no highest bid. Noone takes the card.");
				}
				for (int j = 0; j < numberOfServices; j++) {
					services[j].addToBuffer(sb1.toString());
				}
				clearAllServicesClientInput();
				setGameState(GameStates.CARD_PLAY);
				break;
			case CARD_PLAY:
				StringBuffer sb2 = new StringBuffer();
				for (int k = 0; k < numberOfServices; k++) {
					sb2.append(services[k].getPlayer().getName());
					sb2.append(" played ");
					sb2.append(services[k].playSelectedCard());
					sb2.append(".<br>");
				}
				sb2.append(Stock.printStockPrices());
				for (int j = 0; j < numberOfServices; j++) {
					services[j].addToBuffer(sb2.toString());
				}
				clearAllServicesClientInput();
				setGameState(GameStates.TRADING_SELL);
				break;
			default:
				System.out.println("trying to force logic on an invalid state");
				break;
		}
	}

	/*clear the client input for all services;
	 * If the clientInput field is empty then 
	 * the service is not finished retrieving 
	 * the client input. */
	private void clearAllServicesClientInput() {
		for (int j = 0; j < numberOfServices; j++) {
			services[j].setClientInput("");
		}
	}

	private void assignPlayerNames(){
		services[0].getPlayer().setName("South");
		services[1].getPlayer().setName("North");
		if (services[2] != null) { services[2].getPlayer().setName("West"); }
		if (services[3] != null) { services[3].getPlayer().setName("East"); }
	}
	
	public synchronized void setGameState(GameStates gameState) {
		this.gameState = gameState;
	}
	
	public synchronized GameStates getGameState() {
		return gameState;
	}
}
