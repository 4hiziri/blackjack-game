package serverApp;

public class Host extends Player {

	public Host(Client client) {
		super(client);
	}
	
	@Override	
	public void receiveCard(Card card){
		this.hand.add(card);					
		return;
	}
	
	@Override
	public void systemMessage(String msg){		
		return;
	}
}
