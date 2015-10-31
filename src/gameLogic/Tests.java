package gameLogic;

public class Tests {
	
	public static void testParseStock() {
		System.out.print ("Round trip test for Stock toString()/parseStock(): "); 
		for (Stock s1 : Stock.values()) {
			Stock s2 = Stock.parseStock(s1.firstLetter());
			if (!(s1.equals(s2))) {
				throw new RuntimeException("testParseStock error: " + s1
						+ " not equal to " + s2);
			}
		}
		System.out.println("passed.\n");
	}

	public static void testParseCard() {
		System.out.print("\nRound-trip test for Card toString()/parseCard(): ");
		for (Card c1 : Card.DECK) {
			Card c2 = Card.parseCard(c1.toString());
			if (!(c1.equals(c2))) {
				throw new RuntimeException("testParseCard error: " + c1 + " not equal to " + c2);
			}
		}
		System.out.println("passed.\n");
	}
	

	public static void testCardExecution() {
		System.out.println("Testing card execution - please check output");
		System.out.println("Initial prices: " + Stock.pricesString());
		for (Card c : Card.DECK) {
			c.execute();
			int sumOfPrices = 0; 
			for (Stock s : Stock.values()){
				sumOfPrices += s.price; 
			}
			if (sumOfPrices != Stock.SUM_OF_PRICES_OF_STOCKS)
				throw new RuntimeException("testCardExecution error: sum of stock prices incorrect: " + sumOfPrices);  
			System.out.println("Card " + c + ": " + Stock.pricesString());
		}
	}

	public static void main(String[] args) {
		System.out.println("*** Stocks *** "); 
		for (Stock s : Stock.values()) 
			System.out.println(s); 
		testParseStock(); 
		System.out.println("*** Cards ***");
		for (int i = 0; i < Stock.NUMBER_OF_STOCKS; i++){
			System.out.println(Card.DECK.subList(i*9, (i+1)*9)); 
		}
		
		testParseCard();
		testCardExecution();
	}

}
