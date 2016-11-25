package serverApp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerScore {
	private static final String address = ConnectionSetting.SERVER_SCORE_ADDRESS;
	private static final int port = ConnectionSetting.SERVER_SCORE_PORT;
	private static Client conn = null;
	
	public ServerScore(){
		// :TODO あとで纏める
		while(true){
			try{
				conn = new Client(new Socket(address, port));
			} catch(IOException e) {
				System.out.println("cannot connect ScoreServer, try again...");
				System.out.print(e);				
				continue;
			}
			
			try{
				conn.openStream();
				break;
			} catch(IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	public int score(ArrayList<Card> hand){
		String hand_str = "";
		
		for(int i = 0; i < hand.size(); i++){
			hand_str += hand.get(i).getNumber() + ",";
		}
		conn.println(hand_str.substring(0, hand_str.length() - 1));
		
		String reply = "";
		while(reply.equals("")){
			reply = conn.read();
		}
		
		return Integer.parseInt(reply);
	}
	
	public void close(){
		conn.closeStream();
	}
}
