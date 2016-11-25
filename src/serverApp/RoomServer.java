package serverApp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RoomServer {
	final static int ROOM_LIMIT = 10;
	final static int ENTRY_LIMIT = 10;
	private final static int PORT = 59630;
	
	@SuppressWarnings("resource")
	public static void main( String[] args ) throws IOException {
   
      ServerSocket sSock = null;          // サーバ側のソケット
      Socket client_sock = null;                 // クライアント側のソケット

      // サーバスタート
      
      sSock = new ServerSocket(PORT);    // 59630番ポートをサーバとして起動
      System.out.println("Server Start");
      while( true ) {
	  client_sock = sSock.accept();    // クライアントからの接続待ち
	
	  // スレッドの生成と起動
	  AssignRoomThread t = new AssignRoomThread(client_sock);
	  t.start();
      }
    }
}