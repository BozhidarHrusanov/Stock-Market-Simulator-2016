package gameLogic;

import java.util.ArrayList;

public class Player {
	//each cell contains the amount of shares owned for that company
	private int[] shares = new int[4];
	private ArrayList<Card> cardsArray = new ArrayList<Card>();
	private int money;
	private int bid;
	private String name;

	public Player() {
		generateCards();
		generateStocks();
		money = 1000;
	}

	/* Distribute 10 stocks randomly for the 4 companies. */
	public void generateStocks() {
		int location;
		for (int i = 0; i < 10; i++) {
			location = (int) (Math.random() * 4);
			shares[location]++;
		}
	}

	public void generateCards() {
		int location;
		for (int i = 0; i < 5; i++) {
			location = (int) (Math.random() * Card.DECK.size());
			this.cardsArray.add(Card.DECK.get(location));
			Card.DECK.remove(location);
		}
	}

	public void modifyShares(char companyInitials, int amountSold) {
		switch (companyInitials) {
		case 'A':
			shares[0] += amountSold;
			break;
		case 'C':
			shares[1] += amountSold;
			break;
		case 'G':
			shares[2] += amountSold;
			break;
		case 'M':
			shares[3] += amountSold;
			break;
		default:
			System.out.println("invalid company initials");
		}
	}

	/* Returns the amount of shares owned for a specific company;
	 * Parameter: the first char of the company name. */
	public int getSharesAmount(char companyInitials) {
		switch (companyInitials) {
		case 'A':
			return shares[0];
		case 'C':
			return shares[1];
		case 'G':
			return shares[2];
		case 'M':
			return shares[3];
		default:
			System.out.println("invalid company initials");
		}
		//should't happen
		return 0;
	}
	
	public String displayPlayerShares() {
		StringBuffer sb = new StringBuffer();
		sb.append("Apple: " + shares[0] + "<br>");
		sb.append("Cisco: " + shares[1] + "<br>");
		sb.append("Google: " + shares[2] + "<br>");
		sb.append("Microsoft: " + shares[3] + "<br>");
		return sb.toString();
	}
	
	public void substractBid() {
		money -= bid;
		bid = 0;
	}

	public String printCardsInHand() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cardsArray.size(); i++) {
			sb.append((i+1) + ". ");
			sb.append(cardsArray.get(i).name);
			sb.append("<br>");
		}
		return sb.toString();
	}
	
	/* should be called only when the game is over. */
	public void sellAllShares() {
		modifyMoney(Stock.APPLE.price * shares[0]);
		modifyMoney(Stock.CISCO.price * shares[1]);
		modifyMoney(Stock.GOOGLE.price * shares[2]);
		modifyMoney(Stock.MICROSOFT.price * shares[3]);
	}
	
	public Card getCardByPlayerInput(int playerInput) {
		return cardsArray.get(playerInput - 1);
	}
	
	/* method to manipulate the value of money;
	 * Use a negative value to subtract. */
	public synchronized void modifyMoney(int additionalMoney) {
		money += additionalMoney;
	}
	
	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}
	
	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}
	
	public void addCardToHand(Card card) {
		cardsArray.add(card);
	}
	
	public void removeCardFromHand(Card card) {
		cardsArray.remove(card);
	}
	
	public int getNumberOfCardsInHand() {
		return cardsArray.size();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/* TODO
	 * 
	 * display to the player before 1st sell phase: DONE
	 * -what shares he has; how much money he has; what cards he has DONE 
	 * inform players of new round beginning, how many rounds are left ********DERP HAS BEGUN*******
	 * method which sells all player shares at the appropriate price
	 * Game finished - player standings, total cash balance
	 * 					*********************************************
	 * 					* 		SHARES SELLING PHASE HAS BEGUN		*
	 * 					*********************************************
	 * 					=============================================
	 * 					=		SHARES SELLING PHASE HAS BEGUN		=
	 * 					=============================================
	 * PHASE:SHARES SELLING PHASE HAS BEGUN
	 * 
	 */
}
