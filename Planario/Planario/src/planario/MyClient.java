package planario;

import java.net.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

import javax.swing.*;

public class MyClient extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	PrintWriter out;// 出力用のライター

	Map<Integer, PlayerData> playerData = new HashMap<Integer, PlayerData>();
	int myNumberInt;
	Drow drow;

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

		drow = new Drow(this);
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

		MesgSendThread mst = new MesgSendThread(this);
		mst.start();
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

				myNumberInt = GetMyNumber(br);

				Join(myNumberInt);

				drow.Login();

				SendMessage("Join " + myNumberInt);

				while (true) {
					String inputLine = br.readLine();// データを一行分だけ読み込んでみる
					if (inputLine != null) {// 読み込んだときにデータが読み込まれたかどうかをチェックする
						System.out.println(inputLine);// デバッグ（動作確認用）にコンソールに出力する
						String[] inputTokens = inputLine.split(" "); // 入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];// コマンドの取り出し．１つ目の要素を取り出す

						switch (cmd) {
						case "Update":
							Update(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]),
									Integer.parseInt(inputTokens[3]), Integer.parseInt(inputTokens[4]),
									Integer.parseInt(inputTokens[5]));
							break;
						case "Create":
							Create();
							break;
						case "Delete":
							Delete(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]));
							break;
						case "Join":
							if (Integer.parseInt(inputTokens[1]) == myNumberInt) {
								break;
							}
							Join(Integer.parseInt(inputTokens[1]));
							break;
						case "Disconnect":
							Disconnect(Integer.parseInt(inputTokens[1]));
							break;
						default:
							break;
						}
					} else {
						break;
					}

					if (GetPlayer(myNumberInt).planariaData.size() == 0) {
						// GameOver

					}
				}
				socket.close();
			} catch (IOException e) {
				System.err.println("エラーが発生しました: " + e);
			}
		}
	}

	private int GetMyNumber(BufferedReader br) {
		String myNumberStr;
		try {
			myNumberStr = br.readLine();
			return Integer.parseInt(myNumberStr);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (NumberFormatException e) {
			return GetMyNumber(br);
		}
		return 0;
	}

	// userID planariaID posX posY size
	public void Update(int userID, int planariaID, int posX, int posY, int size) {
		if (userID != myNumberInt) {

			PlayerData player = GetPlayer(userID);

			if (player.planariaData.containsKey(planariaID)) {
				player.planariaData.get(planariaID).setData(posX, posY, size);
			} else {
				System.out.println("NotFound : " + planariaID);
				drow.Create(player.skin, posX, posY, size, userID, planariaID);
			}
		}
	}

	public PlayerData GetPlayer(int userID) {
		if (playerData.containsKey(userID)) {
			return playerData.get(userID);
		} else {
			return Join(userID);
		}
	}

	// userID planariaID posX posY size
	public void Create() {

	}

	// userID planariaID
	public void Delete(int userID, int planariaID) {
		drow.Delete(GetPlayer(userID).planariaData.get(planariaID));
		GetPlayer(userID).planariaData.remove(planariaID);
	}

	// userID posX posY
	public PlayerData Join(int ID) {
		PlayerData p = new PlayerData(ID);
		playerData.put(ID, p);
		return p;
	}

	public void Disconnect(int userID) {
		Collection<Planaria> tmp = GetPlayer(userID).planariaData.values();
		for (Planaria p : tmp) {
			drow.Delete(p);
			p = null;
		}
		playerData.remove(userID);
	}

	public void SendMessage(String msg) {
		try {
			out.println(msg);
			out.flush();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void SendMyPlanariaData(Planaria p) {
		StringBuilder buf = new StringBuilder();
		buf.append("Update ");
		buf.append(myNumberInt);
		buf.append(" ");
		buf.append(p.localId);
		buf.append(" ");
		buf.append(p.posX);
		buf.append(" ");
		buf.append(p.posY);
		buf.append(" ");
		buf.append(p.size);
		SendMessage(buf.toString());
	}

	public void Search(Planaria p) {

		for (PlayerData player : playerData.values()) {
			for (Planaria planaria : player.planariaData.values()) {
				if (planaria == p) {
					continue;
				}

				if (Math.abs(planaria.posX - p.posX) <= p.size / 3 && Math.abs(planaria.posY - p.posY) <= p.size / 3
						&& planaria.size < p.size) {
					Eat(p, player.playerID, planaria.localId, planaria.size);
				}
			}
		}
	}

	public void Eat(Planaria myPlanaria, int userID, int planariaID, int size) {
		myPlanaria.size += size;
		SendMessage("Delete " + userID + " " + planariaID);
	}

	public static void main(String[] args) {
		new MyClient();
	}
}
