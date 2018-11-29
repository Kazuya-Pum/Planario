package planario;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

import javax.swing.*;

public class MyClient extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	PrintWriter out;// 出力用のライター

	List<PlayerData> playerData = new ArrayList<PlayerData>();

	public MyClient() {
		// 名前の入力ダイアログを開く
		String myName = JOptionPane.showInputDialog(null, "名前を入力してください", "名前の入力", JOptionPane.QUESTION_MESSAGE);
		if (myName.equals("")) {
			myName = "No name";// 名前がないときは，"No name"とする
		}

		String serverIP = JOptionPane.showInputDialog(null, "サーバのＩＰアドレス", "IPアドレスの入力", JOptionPane.QUESTION_MESSAGE);
		if (serverIP.equals("")) {
			serverIP = "localhost";
		}

		Drow drow = new Drow();
		drow.setVisible(true);

		// サーバに接続する
		Socket socket = null;
		try {
			// "localhost"は，自分内部への接続．localhostを接続先のIP
			// Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
			// 10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
			socket = new Socket(serverIP, 10000);
		} catch (UnknownHostException e) {
			System.err.println("ホストの IP アドレスが判定できません: " + e);
		} catch (IOException e) {
			System.err.println("エラーが発生しました: " + e);
		}

		MesgRecvThread mrt = new MesgRecvThread(socket, myName);// 受信用のスレッドを作成する
		mrt.start();// スレッドを動かす（Runが動く）
	}

	// メッセージ受信のためのスレッド
	public class MesgRecvThread extends Thread {

		Socket socket;
		String myName;

		public MesgRecvThread(Socket s, String n) {
			socket = s;
			myName = n;
		}

		// 通信状況を監視し，受信データによって動作する
		public void run() {
			try {
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(myName);// 接続の最初に名前を送る

				String myNumberStr = br.readLine();
				int myNumberInt = Integer.parseInt(myNumberStr);
				playerData.add(new PlayerData(myNumberInt));

				while (true) {
					String inputLine = br.readLine();// データを一行分だけ読み込んでみる
					if (inputLine != null) {// 読み込んだときにデータが読み込まれたかどうかをチェックする
						System.out.println(inputLine);// デバッグ（動作確認用）にコンソールに出力する
						String[] inputTokens = inputLine.split(" "); // 入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];// コマンドの取り出し．１つ目の要素を取り出す

						switch (cmd) {
						case "Update":
							Update();
							break;
						case "Create":
							Create();
							break;
						case "Delete":
							Delete();
							break;
						case "Join":
//							Join();
							break;
						default:
							break;
						}
					} else {
						break;
					}

				}
				socket.close();
			} catch (IOException e) {
				System.err.println("エラーが発生しました: " + e);
			}
		}
	}

	// userID planariaID posX posY size
	public void Update() {

	}

	// userID planariaID posX posY size
	public void Create() {

	}

	// userID planariaID
	public void Delete() {

	}

	// userID posX posY
	public void Join(int ID, int posX, int posY) {
		playerData.add(new PlayerData(ID));
		playerData.sort((a, b) -> a.playerID - b.playerID);
	}

	public void SendMessage(String msg) {
		out.println(msg);
		out.flush();
	}

	public static void main(String[] args) {
		new MyClient();
	}
}