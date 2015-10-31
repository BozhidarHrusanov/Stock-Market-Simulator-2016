package gameLogic;

import java.util.ArrayList;

public class Player {
	private ArrayList<Integer> shares = new ArrayList<>();
	
	private ArrayList<Card> CardsArray = new ArrayList<Card>();
	private int money;

	public Player() {
		GenerateCards();
		GenerateStocks();
		money = 1000;
	}

	public void GenerateStocks() {
		int location;
		int[] sharesArr = new int[4];
		for (int i = 0; i < 10; i++) {
			location = (int) (Math.random() * 4);
			sharesArr[location] = sharesArr[location] + 1;
		}
		for (int i = 0; i < sharesArr.length; i++) {
			shares.add(sharesArr[i]);
		}
	}

	public void GenerateCards() {
		int location;
		for (int i = 0; i < 5; i++) {
			location = (int) (Math.random() * Card.DECK.size());
			this.CardsArray.add(Card.DECK.get(location));
			Card.DECK.remove(location);
		}
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public void addToMoney(int additionalMoney) {
		money += additionalMoney;
	}

	public ArrayList<Integer> getShares() {
		return shares;
	}

	/*public removeShares(char initials, int amount) {
		switch (initials) {
		case 'A':
			(int)(shares.get(0)) -= amount;
		}
	}*/

	public ArrayList<Card> getCardsArray() {
		return CardsArray;
	}
}
