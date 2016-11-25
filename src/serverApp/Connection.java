package serverApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

// client と置き換えろ
public class Connection {
	private String address = "";
	private int port = 0;
	
	Socket sock = null;                 // クライアント用ソケット
    InputStream fin = null;             // サーバからの入力ストリーム
    OutputStream fout = null;           // サーバへの出力ストリーム
    BufferedReader in = null;           // 文字列入力ストリーム
    PrintWriter out = null;             // 文字列出力ストリーム
	
	Connection(String address, int port){
		this.address = address;
		this.port = port;		
	}
	
	public void connect(){
		try{
    		// ソケット接続要求
    		sock = new Socket(address, port);

    		// ストリームの確立
    		fin = sock.getInputStream();
    		fout = sock.getOutputStream();
    		in = new BufferedReader( new InputStreamReader( fin ) );
    		out = new PrintWriter( fout, true );
    	} catch(IOException e) {
    		System.out.println(e);
    	}
	}
	
	public void print(String str){
		out.println(str);
		return;
	}
	
	public String read(){
		String str = "";
		
		try{
			str = in.readLine();
		} catch(IOException e) {
			System.out.print(e);
		}
		
		return str;
	}
	
	public void close(){
		try{
    		in.close();
    		out.close();
    		sock.close();
    	} catch(IOException e){
    		System.out.print(e);
    	}
    	
    	return;
    }	
}