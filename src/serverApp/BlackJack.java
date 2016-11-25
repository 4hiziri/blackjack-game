package serverApp;

import java.util.ArrayList;

public class BlackJack extends Thread{
	private boolean isFinished = false; // threadが終了しているかどうか
    Player player = null;
    private ServerCards dealer = null;
    private RoomThread parent = null;
    private static ServerScore server_score = new ServerScore();
    
	BlackJack(Player player, RoomThread parent){
		this.parent = parent;
		this.player = player;		
		this.dealer = new ServerCards();
		this.isFinished = false;
		initializeHand();
	}
	
	/**
	 * initialize player's hand, draw two cards
	 */
	private void initializeHand(){		
		ArrayList<Card> cards = (ArrayList<Card>) dealer.draw(2);
		int length = cards.size();
		// :TODO receiveCardsに		
		for(int i = 0; i < length; i++){				
			player.receiveCard(cards.get(i));	
		}
		return;
	}	
	
	/**
	 * run thread, process game routine
	 */
	public void run(){
		while(!isFinished){	
				String command = player.listen();
				parseCommand(command);				
		}

		dealer.close();		
		parent.deleteThread(this);
		return;
	}
	
	/**
	 * process player as host
	 */
	public void hostPlay(){
		while(!player.getIsDecided()){
			String str_hand = "";
			ArrayList<Card> player_hand = player.getCard();

			// サーバへ送る文字列を、適切にフォーマットする
			// 今は、1文字の区切りを入れる形式になっているため、","を間に挟んでいる
			for(int i = 0; i < player_hand.size(); i++){
				str_hand += player_hand.get(i).getNumber() + ",";
			}		
			// 最後に無駄な","がついてしまうので、取り除く
			str_hand = str_hand.substring(0, str_hand.length() - 1);

			// 送信する
			player.message(str_hand);		

			// 送信した手によってサーバが行動するので、手が確定するまで続ける

			parseCommand(player.client.read());
		}		
		
		return;
	}
	
	/**
	 * @param hand
	 * @return
	 */
	private String handToStr(ArrayList<Card> hand){
		String hand_str = "";
		
		for(int i = 0; i < hand.size(); i++){
			hand_str += hand.get(i).getNumber() + ", ";
		}
		
		return hand_str.substring(0, hand_str.length() - 2);
	}
	
	/**
	 * @param hand
	 * @return
	 */
	private String handToStrHostFormat(ArrayList<Card> hand){
		String hand_str = "";
		
		hand_str += hand.get(0).getNumber() + ", ";
		
		for(int i = 1; i < hand.size(); i++){
			hand_str += "*" + ", ";
		}
		
		return hand_str.substring(0, hand_str.length() - 2);		
	}
	
	/**
	 * 命令文字列から関数を実行し、継続するかどうかの真偽値を返す
	 * @param command
	 * @return
	 */
	// :TODO to void, use isDecided
	private void parseCommand(String command){
		if(command == null) return;
		switch(command){
		case "hit":		
			if(player.getIsDecided()) break;
			hit();
			break;
		case "stand":
			stand();
			break;
		case "hand":
			printPlayerHand();
			break;
		case "hand dealer":
			printHostHand();
			break;
		case "quit":
			quitGame();
			break;
		default:
			System.out.println("chat log:[" + player + "]: " + command);
			printPlayerMessage(command);		
			break;
		}
		
		return;
	}

	/**
	 * disconnect player's connection
	 */
	private void quitGame() {
		// :TODO 退出処理
		player.systemMessage("ゲームを終了します");
		parent.systemNotifyAllPlayer(player + "が退出しました");
		player.bye();
		isFinished = true;
	}

	/**
	 * print one player message to all player, which is added prefix "Player[ID]: [msg]"
	 * @param command
	 */
	private void printPlayerMessage(String command) {
		parent.playerNotifyAllPlayer(player, command);
		return;
	}

	/**
	 * print host's hand to only player 
	 */
	private void printHostHand() {
		player.systemMessage("親の手は、");
		player.systemMessage(handToStrHostFormat(parent.host.getCard()));
		return;
	}

	/**
	 * print player's hand to only player
	 */
	private void printPlayerHand() {
		player.systemMessage("あなたの手は、");
		player.systemMessage(handToStr(player.getCard()));
		return;
	}
	
	/**
	 * draw card and added to player's hand
	 */
	private void hit(){		
		player.systemMessage("ヒットします");
		player.receiveCard(dealer.draw(1).get(0));
		
		boolean isBust = isBust(); // バストしたかどうか調べ、出力を行う		
		if(isBust){
			player.systemMessage("バストしてしまった!");
		}
		player.setIsDecided(isBust);
		return;
	}
	
	/**
	 * player's member var, isDecided, false -> true
	 */
	private void stand(){
		player.setIsDecided(true);
		player.systemMessage("スタンドしました");
		return;
	}
	
	/**
	 * check player's hand whether bust or not
	 * @return
	 */
	private boolean isBust(){							
		int score = server_score.score(player.getCard());
		if(score > 21) return true;
		else if (score > 0) return false;
		else return false;
	}
	
	/**
	 * reset player's hand and isDecided true -> false
	 * @param player
	 */
	void resetPlayerStatus() {		
		player.flushHand();
		player.setIsDecided(false);
		initializeHand();
	}
}
