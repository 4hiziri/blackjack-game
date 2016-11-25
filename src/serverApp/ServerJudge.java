package serverApp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerJudge {
	private static final String address = ConnectionSetting.SERVER_JUDGE_ADDRESS;
	private static final int port = ConnectionSetting.SERVER_JUDGE_PORT;
	private Client conn = null;
	private ServerScore server_score = null;
	
	public ServerJudge(){		
		while(true){
			try{	
				conn = new Client(new Socket(address, port));
				server_score = new ServerScore();
			} catch(IOException e) {
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
	
	public ArrayList<Integer> judge(Player host, ArrayList<Player> players){
		int host_score = server_score.score(host.getCard());
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		for(int i = 0; i < players.size(); i++){
			ArrayList<Card> hand = players.get(i).getCard();
			int player_score = server_score.score(hand);
			conn.println(host_score + " " + player_score);
			
			String reply = "";
			while(reply.equals("")){
				reply = conn.read();
			}
			list.add(Integer.parseInt(reply));
		}
				
		return list;
	}
}
