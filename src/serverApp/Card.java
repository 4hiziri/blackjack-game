package serverApp;

public class Card {
	private int number = 0;
	private Suit suit = null;
	
	public Card(int num, Suit suit){
		this.number = num;
		this.suit = suit;		
	}

	public int getNumber() {
		return number;
	}

	public Suit getSuit() {
		return suit;
	}
}
