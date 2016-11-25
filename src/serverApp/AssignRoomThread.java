package serverApp;

import java.io.IOException;
import java.net.Socket;
import serverApp.Client;

/*	
 * クライアントとの通信し，入力によって部屋に振り分ける
 */
class AssignRoomThread extends Thread {
	private Client client;
	private static RoomManager room_manager = 
			new RoomManager(RoomServer.ROOM_LIMIT, RoomServer.ENTRY_LIMIT);
	
	public AssignRoomThread(Socket socket){
		this.client = new Client(socket);
	}

	// 入力されたルームナンバーを抽出し，Int型にパースする
	// 文字列から変換できない場合は-1を返す
	private int parseInpumRoomNumber(String str_room_num){
		int input_num = -1;
		
		// Int型に変換出来なければ-1をinput_numに代入する
		try{
			input_num = Integer.parseInt(str_room_num);
		} catch (NumberFormatException e){
			input_num = -1;
		}
		
		return input_num;
	}
	
	/** ルームナンバーをクライアントに入力させ，その値を取り出す
	 * IOExceptionが起きたら，再び入力を求める
	 * guest(ランダムなルームで良い)なら-1が返る
	 * 入力を許す値の範囲をmin, maxで指定する
	 * @param min 入力を許す値の下限，-1以下だとguestとの区別がつけられないため正常に動作しない，エラーを返すように
	 * @param max 入力を許す値の上限
	 */ 
	private int readRoomNumberFromClient(int min, int max){				
		// クライアントからのメッセージ受信
		// クライアントにルームナンバーを入力してもらい，その値をroom_numに代入する
		String str_room_num = "";
		int room_id = -1;
		
		// 引数のエラーチェック
		if(min < 0){
			throw new IllegalArgumentException(); 
		}
		
		while(room_id < min){
			client.println("Input Room Number, " + min + "~" + max + " (or input 'guest')");
			
			// 入力をパースしてroom_idに代入する
			// 入力が正しくないなら，-1が入る			
			str_room_num = client.read();
			
			if(str_room_num.equalsIgnoreCase("guest")){
				room_id = -1;
				break;
			}
			
			// 入力を文字列から数値に変換する
			try{
				room_id = parseInpumRoomNumber(str_room_num);
			} catch (NumberFormatException e) { // 変換に失敗した場合，再度入力させる
				client.println("please input number: ");
				continue;
			}
						
			if(room_id < min || room_id > max){ // 範囲を超えた数値が入力された場合，room_id = -1として再度ループさせる 
				client.println("please " + min + " < [input] < " + max);
				room_id = -1; // maxを超えた場合room_id > minとなってしまうため
				continue;
			}	
		}
		
		return room_id;	
	}

	@Override
	public void run(){
		int room_id = -1;
		
		// ストリームの確立
		try {
			client.openStream();
		} catch (IOException e) {		
			e.printStackTrace();
		}
		
		room_id = readRoomNumberFromClient(0, RoomServer.ROOM_LIMIT - 1);
		System.out.println("Access to Room" + room_id);
		room_manager.moveToRoom(room_id, client);
		
		return;
	}
}