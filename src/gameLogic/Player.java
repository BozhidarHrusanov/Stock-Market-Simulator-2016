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
	
	public Card getCardByPlayerInput(int playerInput) {
		return cardsArray.get(playerInput - 1);
	}
	
	public void modifyMoney(int additionalMoney) {
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
	 * inform players of new round beginning, how many rounds are left
	 * method which sells all player shares at the appropriate price
	 * Game finished - player standings, total cash balance
	 * 
	 */
}
