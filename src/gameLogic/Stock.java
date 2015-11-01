package gameLogic;

public enum Stock {
	APPLE(100), CISCO(100), GOOGLE(100), MICROSOFT(100);

	public int price;

	Stock(int price) {
		this.price = price;
	}

	// DELETE LATER ON 
	public static final int NUMBER_OF_STOCKS = Stock.values().length;
	public static final int SUM_OF_PRICES_OF_STOCKS = NUMBER_OF_STOCKS * 100;

	public static String pricesString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (Stock s : Stock.values()) {
			sb.append(s.price);
			sb.append(s == MICROSOFT ? "]" : ",");
		}
		return sb.toString();
	}

	public char firstLetter() {
		return this.toString().charAt(0);
	}

	public static Stock parseStock(char c) {
		for (Stock s : Stock.values()) {
			if (s.firstLetter() == c)
				return s;
		}
		// won't happen
		return null;
	}
	
	public static String initialsToCompanyName(char c) {
		switch (c) {
		case 'A':
			return "Apple";
		case 'C':
			return "Cisco";
		case 'G':
			return "Google";
		case 'M':
			return "Microsoft";
		default:
			return "invalid company name";
		}
	}

}
