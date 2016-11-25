package serverApp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerCards {
	private static final String address = ConnectionSetting.SERVER_CARDS_ADDRESS;
	private static final int port = ConnectionSetting.SERVER_CARDS_PORT;
	private static final String draw_command = "draw";
	private Client conn = null;
    
    public ServerCards(){
    	while(true){
    		try{
    			this.conn = new Client(new Socket(address, port));
    			conn.openStream();
    			break;
    		} catch(java.net.ConnectException e) {
    			System.out.println("can not connect, try again...");
    			e.printStackTrace();
    			try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					return;
				}
    			continue;
    		} catch(IOException e){
    			e.printStackTrace();
    		}
    	}
    }
    
    public List<Card> draw(int draw_num){
    	ArrayList<Card> cards = new ArrayList<Card>();

    	// カードを要求し、cardsに収める。
    	// Suitは使わないため、適当にスペードとする。
    	for(int i = 0; i < draw_num; i++){
    		conn.println(draw_command);
    		String card_str = "";    		
    		while(card_str == null || card_str.equals("")){
    			card_str = conn.read();    			
    		}    		
    		cards.add(new Card(Integer.parseInt(card_str), Suit.SPADE));
    	}
    
		return cards;
    }
    
    public void close(){
    	conn.closeStream();
    	return;
    }
}