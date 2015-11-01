package main;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gameLogic.Card;
import gameLogic.GameStates;

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
		while (matchOngoing) {
			for (int i = 0; i < numberOfServices; i++) {
				services[i].setReadyForNextRound(true);
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
				if (!identicalHighestBids) {
					services[highestBidderIndex].getPlayer().substractBid();
					services[highestBidderIndex].getPlayer().addCardToHand(Card.getAndRemoveCardOnTable());
				}
				break;
			case CARD_PLAY:
				for (int k = 0; k < numberOfServices; k++) {
					services[k].playSelectedCard();
				}
				break;
			default:
				System.out.println("trying to force logic on an invalid state");
				break;
		}
	}

	private void assignPlayerNames(){
		services[0].getPlayer().setName("South");
		services[1].getPlayer().setName("North");
		if (services[2] != null) { services[2].getPlayer().setName("West"); }
		if (services[3] != null) { services[3].getPlayer().setName("East"); }
	}
	
	/* The message is appended to the front of the next message to
	 * be sent to both clients.	 
	private void sendMessageToAll(String msg) {
		for ( int i = 0; i < numberOfServices; i++) {
			services[i].addToMessageBufferToClient(msg);
		}
	}*/
	
	public GameStates getGameState() {
		return gameState;
	}
}
