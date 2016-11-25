package serverApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/** 
 * クライアントごとにストリームを管理するためのクラス
 */
class Client{
	private Socket sock;
	private InputStream fin = null;        // 入力ストリーム
	private OutputStream fout = null;      // 出力ストリーム
	private BufferedReader in = null;      // 文字列入力ストリーム
	private PrintWriter out = null;        // 文字列出力ストリーム
	
	public Client(Socket socket){
		this.sock = socket;
	}
	
	public void openStream() throws IOException{
		// ストリームの確立
		fin = sock.getInputStream();
		in = new BufferedReader( new InputStreamReader(fin) );
		
		fout = sock.getOutputStream();		
		out = new PrintWriter( fout, true );
		
		return;
	}
	
	public void println(String str){
		this.out.println(str);		
		return;
	}

	public String read(){
		String line = "";
		
		try {
			line = in.readLine();
		} catch (IOException e) {		
			e.printStackTrace();
		}
		
		return line;
	}
	
	public void closeStream(){
		try {
			// クローズ
			if ( in != null ) { in.close(); }
			if ( out != null ) { out.close(); }
			if ( fin != null ) { fin.close(); }
			if ( fout != null ) { fout.close(); }
			if ( sock != null ) { sock.close(); }
		} catch( IOException e ) {
			System.out.println( "error" + e );
		}
		
		return;
	}
}