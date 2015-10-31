package Main;


public class GameSession implements Runnable {
	
	private GameService[] services = new GameService[4];
	private int numberOfServices;
	private boolean botsON;
	//if a player disconnects the match thread ends execution
	private boolean matchOngoing = true;
	private String gameState = "settingUp";

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
		for (int i = 1; i < numberOfServices; i++) {
			if (services[i] == null) {
				freeSlotIndex = i;
				break;
			}
		}
		if (freeSlotIndex == 0) {
			otherPlayerSevice.sendMessageToClient("Error: no free slot for you.");
			System.out.println("Player tried to join, but there is no free slot.");
			return;
		}

		services[freeSlotIndex] = otherPlayerSevice;
		services[freeSlotIndex].setSession(this);
		new Thread(this.services[freeSlotIndex]).start();
		System.out.println("playerService" + freeSlotIndex +" thread started!");
		services[freeSlotIndex].sendMessageToClient("You joined a session!");
		System.out.println("Game session has "
					+ (freeSlotIndex+1) + "/" + numberOfServices
					+ " players in.");
		
		//if this is the last player - start game
		if (freeSlotIndex == (numberOfServices - 1)) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			new Thread(this).start();
			System.out.println("Match thread started!");
		}
	}
	
	@Override
	public void run() {
		while (matchOngoing) {
			sendMessageToAll("oi m80");
			for ( int i = 0; i < numberOfServices; i++) {
				services[i].setReadyForNextRound(true);
			}
			for ( int i = 0; i < numberOfServices; i++) {
				System.out.println(services[i].getClientInput());
			}
		}
	}
	
	/* The message is appended to the front of the next message to
	 * be sent to both clients.
	 */
	private void sendMessageToAll(String msg) {
		for ( int i = 0; i < numberOfServices; i++) {
			services[i].addToMessageBufferToClient(msg);
		}
	}
	
	public String getGameState() {
		return gameState;
	}
}
