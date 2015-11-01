package gameLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Card implements Comparable<Card> {
	public final String name;
	public final Stock[] stocks;
	public final Type type;

	public static enum Type {
		PLUS5, PLUS10, MINUS10
	};

	public static final Map<String, Card> MAP = new TreeMap<>();
	public static final List<Card> DECK = new ArrayList<>();

	static {
		init();
		Collections.sort(DECK);
	}

	private Card(Stock[] stocks, Type type) {
		this.name = cardName(stocks, type);
		this.stocks = stocks;
		this.type = type;
	}

	public void execute() {
		switch (type) {
		case PLUS5: {
			stocks[0].price += 5;
			stocks[1].price -= 5;
			break;
		}
		case PLUS10: {
			stocks[0].price += 10;
			stocks[1].price -= 5;
			stocks[2].price -= 5;
			break;
		}
		case MINUS10: {
			stocks[0].price -= 10;
			stocks[1].price += 5;
			stocks[2].price += 5;
			break;
		}
		}
	}

	public static Card getCardOnTable() {
		return DECK.get(0);
	}

	public static Card getAndRemoveCardOnTable() {
		return DECK.remove(0);
	}
	
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (!Arrays.equals(stocks, other.stocks))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int compareTo(Card c) {
		return this.name.compareTo(c.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public static Card parseCard(String s) {
		return MAP.get(s);
	}

	public static void init() {
		for (Stock s1 : Stock.values()) {
			for (Stock s2 : Stock.values()) {
				if (!s1.equals(s2)) {
					DECK.add(new Card(new Stock[] { s1, s2 }, Type.PLUS5));
					List<Stock> otherStocks = new ArrayList<>();
					for (Stock s : Stock.values()) {
						if (!s.equals(s1) && !s.equals(s2)) {
							otherStocks.add(s);
						}
					}
					Stock[] stocks = new Stock[] { s1, otherStocks.get(0), otherStocks.get(1) };
					DECK.add(new Card(stocks, Type.PLUS10));
					DECK.add(new Card(stocks, Type.MINUS10));
				}
			}
		}
		for (Card c : DECK) {
			MAP.put(c.name, c);
		}
	}

	public static String cardName(Stock[] stocks, Type type) {
		String[] effects = null;
		switch (type) {
		case PLUS5:
			effects = new String[] { "+5", "-5" };
			break;
		case PLUS10:
			effects = new String[] { "+10", "-5" };
			break;
		case MINUS10:
			effects = new String[] { "-10", "+5" };
		}
		StringBuffer sb = new StringBuffer("(");
		sb.append(stocks[0].firstLetter());
		sb.append(effects[0]);
		sb.append("/");
		for (int i = 1; i < stocks.length; i++)
			sb.append(stocks[i].firstLetter());
		sb.append(effects[1]);
		sb.append(")");
		return sb.toString();
	}
	
}
