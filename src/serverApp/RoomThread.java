package serverApp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 部屋を表すスレッド，その部屋の全プレイヤーの入出力を管理する
 * ルールに手札を処理させる
 */
public class RoomThread extends Thread {
	private boolean isStart = false;
	private int room_id = -1;	
	private int max_entry = 0;
	private ConcurrentLinkedQueue<Player> player_waiting = new ConcurrentLinkedQueue<Player>();
	Host host = null;	
	private ArrayList<BlackJack> threads = new ArrayList<BlackJack>();
	private BlackJack host_process = null;
	
	public RoomThread(int id, int max_entry){
		this.room_id = id;
		this.max_entry = max_entry;		
		this.host = new Host(ServerCom.getConnection());		
		host_process = new BlackJack(host, this);
	}
	
	public void addPlayer_waiting(Player player) {
		player_waiting.add(player);
		return;
	}
	
	/**
	 * 全てのプレイヤーの手が確定しているかどうかを判定する
	 * @return
	 */
	private boolean checkAllPlayerFinished() {		
		int mem_num = getMemberNum();
		for(int i = 0; i < mem_num; i++){
			//決めていないプレイヤーがいた時点でfalseを返す
			Player player = threads.get(i).player;
			if(!player.getIsDecided()) return false;			
		}
		
		// 親が手を確定させていないなら、falseを返す
		// ほぼ間違いなく終わっているはずなので、通常実行されない
		if(!host.getIsDecided()) return false;
		
		// 全てのプレイヤーが手を確定させていたら、trueを返す		
		return true;
	}

	/**
	 * 参加待ちプレイヤーがいるかどうかチェックする
	 * いた場合、参加可能なら参加させ、不可能なら接続を切断する
	 * 状態を変更させる
	 */
	private void checkEntry() {
		// 待ちプレイヤーがいた場合、entryProcedureを呼び、プレイヤーを参加させる
		while(!player_waiting.isEmpty()){ 
			entryProcedure(player_waiting.poll());			
		}
		return;
	}

	private void clear(){
		int len = threads.size();
		
		for(int i = 0; i < len; i++){			
			BlackJack thread = threads.get(i);
			renewThread(thread);
		}
		
		renewThread(host_process);
		
		return;
	}

	/**
	 * 部屋を閉鎖する
	 * 全てのプレイヤーがゲームを終了した段階でスレッドを終了させる
	 * 全てのストリームの解放などを行う
	 */
	private void closeRoom(){
		int mem_num = getMemberNum();
		for(int i = 0; i < mem_num; i++){
			threads.get(i).player.bye();
		}
		host.bye();
	}

	// 渡されたスレッドと同じIDのスレッドを削除し、threadsを先頭に詰める
	void deleteThread(BlackJack rm_thread) {
		threads.remove(rm_thread);		
		return;
	}
	
	/**
	 * @param player
	 */
	private void draw(Player player) {
		player.systemMessage("YOU DRAW");
		return;
	}
	
	/**
	 * playerが参加できるかどうかを判定し、可能ならPlayersに追加し，出来ないならplayerを切断する
	 * @return
	 */
	private void entryProcedure(Player player){
		// ここで比較すると効率が悪い気がするが，そんなに多くのエントリーを想定していない
		// 良しとしておく
		// 再接続と部屋選択に困難はないはずだ
		int mem_num = getMemberNum();
				
		if(mem_num >= max_entry){					
			player.systemMessage("Room" + room_id + "は，満員です．");
			player.bye();
		} else {
			// 他のPlayerに参加したPlayerを通知
			systemNotifyAllPlayer(player + "が参加しました．");
			player.systemMessage("Room" + room_id + "にようこそ！");
			
			//players.add(player);			
			BlackJack thread = new BlackJack(player, this);
			thread.start();
			threads.add(thread);
		}
		
		return;
	}
	
	public int getMemberNum(){
		return threads.size();
	}
	
	public int getRoomId() {
		return room_id;
	}
	
	public boolean isStart(){
		return isStart;
	}
	
	/**
	 * @param player
	 */
	private void lose(Player player) {
		player.systemMessage("YOU LOSE...");
		return;
	}
	
	/**
	 * 勝ち負けを外部サーバで判定し、結果をプレイヤーに通知し、接続を切断する
	 * :TODO 繰り返しゲームをできるようにし、賭け金システムを実装したい
	 */
	private void notifyResult(){
		// 仕様により
		int WIN = 1;
		int LOSE = -1;
		int DRAW = 0;
		
		ServerJudge judge = new ServerJudge();
		ArrayList<Player> players = new ArrayList<Player>();
		int mem_num = getMemberNum();
		
		for(int i = 0; i < mem_num; i++){
			players.add(threads.get(i).player);
		}
		
		ArrayList<Integer> result_list = judge.judge(host, players);
		
		for(int i = 0; i < result_list.size(); i++){
			Player player = players.get(i);
			int result = result_list.get(i);
			
			if(result == WIN){
				win(player);
			} else if (result == LOSE){
				lose(player);
			} else if (result == DRAW){
				draw(player);
			}
		}
	}

	void playerNotifyAllPlayer(Player player, String message){
		int player_num = getMemberNum();
		
		for(int i = 0; i < player_num; i++){
			threads.get(i).player.message("Player" + player + ": " + message);
		}	
		
		return;
	}

	/**
	 * 次のゲームを開始できる状況にする
	 * @param thread
	 */
	private void renewThread(BlackJack thread) {
		synchronized (thread) {
			try {
				System.out.println("lock: " + room_id);
				thread.wait(3000);
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
			thread.resetPlayerStatus();
			thread.notify();
			System.out.println("release: " + room_id);
		}	
	}

	@Override
	public void run() {
		// initialize
		isStart = true;
		boolean isFinishedAllPlayer = true;

		// main logic
		while (true) {
			// player entry
			// check player num. if over, connection close
			checkEntry();

			// game start

			// hostの手が確定していないなら、プロセスをスタートさせる
			// 繰り返しゲームをするときのためここで処理する
			if (!host.getIsDecided()) {
				host_process.hostPlay();
			}

			// 全てのプレイヤーが手を確定させたかをチェックする
			isFinishedAllPlayer = checkAllPlayerFinished();

			// 手が決まったら、手の強さで勝敗を決定させる
			if (isFinishedAllPlayer && host.getIsDecided()) {
				notifyResult();
				isFinishedAllPlayer = false;
				clear();
			}

			// :TODO 賭け金システム実装
		}
	}
	
	/**
	 *  全てのプレイヤーにメッセージを表示する
	 * @param message
	 */
	void systemNotifyAllPlayer(String message){
		int player_num = getMemberNum();
		
		for(int i = 0; i < player_num; i++){
			threads.get(i).player.systemMessage(message);
		}	
		
		return;
	}

	/**
	 * @param player
	 */
	private void win(Player player) {
		player.systemMessage("YOU WIN!");
		player.systemMessage("Congratulation!");
		return;
	}
}
