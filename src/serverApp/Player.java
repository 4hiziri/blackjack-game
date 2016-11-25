package serverApp;

import java.util.ArrayList;

public class Player{
	private boolean isDecided = false;
	Client client = null;
	protected ArrayList<Card> hand = new ArrayList<Card>();
	// :TODO 名前の実装
	private int id = -1;
	
	public Player(Client client){
		this.client = client;
	}
	
	public void setId(int id){
		this.id = id;
		return;
	}
	
	public void receiveCard(Card card){
		this.hand.add(card);		
		systemMessage("カード " + card.getNumber() + "を引きました");
		return;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Card> getCard(){
		return (ArrayList<Card>)hand.clone();
	}
	
	public void message(String str){
		client.println(str);		
		return;
	}
	
	public void systemMessage(String msg){
		client.println("System: " + msg);
		return;
	}
	
	public String listen(){
		return client.read();
	}
	
	public void bye(){
		systemMessage("さよなら!");
		client.closeStream();
		return;
	}
	
	@Override
	public String toString(){
		return "Player" + id;
		
	}

	
	void flushHand() {
		hand.clear();
		return;
	}
	
	void setIsDecided(boolean bool){
		isDecided = bool;
		return;
	}
	
	boolean getIsDecided(){
		return isDecided;
	}
}
