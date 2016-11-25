package serverApp;

import java.util.ArrayList;
import java.util.List;

/**
 * 全ての部屋を管理する，部屋の数や部屋に入る数も管理する 各部屋へPlayerを送る どの部屋へ送るかはIDで管理する
 */
public class RoomManager {
	List<RoomThread> room_threads = new ArrayList<RoomThread>();
	private int max_room = 0;
	private int max_entry = 0;

	public RoomManager(int max_room, int max_entry) {
		this.max_room = max_room;
		this.max_entry = max_entry;

		for (int i = 0; i < this.max_room; i++) {			
			room_threads.add(new RoomThread(i, this.max_entry));
		}
	}

	// clientを引数にPlayerを作成して，IDがroom_idのスレッドに送る
	// スレッドがスタートしていないなら、スタートさせる
	public void moveToRoom(int room_id, Client client) {
		// guestならidは-1なので，空いているところに放り込む
		if(room_id == -1) {
			try{
				room_id = getVacantRoom();
			} catch(NoVacantRoomException e) {
				client.println("空いている部屋がありません、時間を置いてアクセスしてください");
				client.closeStream();
			}
		}
		
		RoomThread room = room_threads.get(room_id);
		Player player = new Player(client);
		player.setId(room.getMemberNum());		
		room.addPlayer_waiting(player);
		
		if (!room.isStart()) {
			room.start();
		}

		return;
	}
	
	/**
	 * 空いているroom_threadを返す
	 * 先頭から探すようになっているが、先頭からでなくともよい
	 * 楽なので先頭から探している
	 * もし空いている部屋が無いならエラーを返す
	 * @return
	 * @throws NoVacantRoomException 
	 */
	private int getVacantRoom() throws NoVacantRoomException{
		int room_num = room_threads.size();
		
		for(int i = 0; i < room_num; i++){			
			RoomThread room = room_threads.get(i);
			if(room.getMemberNum() >= max_entry) continue;
			else return room.getRoomId();
		}
		
		throw new NoVacantRoomException();
	}
	
	
}