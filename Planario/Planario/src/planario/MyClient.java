package planario;

import java.net.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Point;
import java.io.*;

public class MyClient {
	PrintWriter out;// 出力用のライター

	ConcurrentHashMap<Integer, PlayerData> playerData = new ConcurrentHashMap<Integer, PlayerData>();
	int myNumberInt;
	Drow drow;
	int planktonSize = 10;
	int defualtSize = 100;
	PlayerData planktons;
	int fieldSize = 4000;
	int score = 0;
	boolean loginFlag = false;

	MesgSendThread mst;

	static boolean accessFlag = false;

	public MyClient() {
		drow = new Drow(this);
		drow.setVisible(true);
	}

	public void Access(String serverIP) {
		accessFlag = true;
		String myName = "";

		if (serverIP.equals("")) {
			serverIP = "localhost";
		}

		// サーバに接続する
		Socket socket = null;
		try {
			// "localhost"は，自分内部への接続．localhostを接続先のIP
			// Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
			// 10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
			socket = new Socket(serverIP, 10000);
		} catch (Exception e) {
			System.err.println("エラーが発生しました: " + e);
			drow.title.setErrorMsg("サーバーに接続できません");
			accessFlag = false;
			return;
		}

		playerData.put(0, new PlayerData(0));
		planktons = GetPlayer(0);

		MesgRecvThread mrt = new MesgRecvThread(socket, myName);// 受信用のスレッドを作成する
		mrt.start();// スレッドを動かす（Runが動く）

		mst = new MesgSendThread(this);

		drow.title.hideErrorMsg();
		drow.hideTilePane();
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
				score = 0;
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
						case "Pop":
							Pop(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]),
									Integer.parseInt(inputTokens[3]));
							break;
						default:
							break;
						}
					} else {
						break;
					}

					if (GetPlayer(myNumberInt).planariaData.size() == 0) {
						drow.setGameOver();
						loginFlag = false;
						SendMessage("Disconnect " + myNumberInt);
						SendMessage("BYE");
						Disconnect(myNumberInt);
						resetField();
						accessFlag = false;

						AUDIO.BGM.stop();
						AUDIO.END.play();

						break;
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
				((Planaria) player.planariaData.get(planariaID)).setCurrent(posX, posY, size);
			} else {
				System.out.println("NotFound : " + planariaID);
				drow.Create(player.skin, posX, posY, size, userID, planariaID);
			}
		}
	}

	public void Pop(int planktonID, int posX, int posY) {
		if (!planktons.planariaData.containsKey(planktonID)) {
			planktons.planariaData.put(planktonID, drow.PopPlankton(posX, posY, planktonID));
		}
	}

	Random random = new Random();

	public void Pop() {
		Plankton plankton = drow.PopPlankton(random.nextInt(fieldSize), random.nextInt(fieldSize));
		planktons.planariaData.put(plankton.localId, plankton);

		SendPlanktonData(plankton);
	}

	public void LoadPlankton() {
		for (CanEatObj p : planktons.planariaData.values()) {
			SendPlanktonData(p);
		}
	}

	private void SendPlanktonData(CanEatObj p) {
		StringBuilder buf = new StringBuilder();
		buf.append("Pop ");
		buf.append(p.localId);
		buf.append(" ");
		buf.append(p.current.x);
		buf.append(" ");
		buf.append(p.current.y);
//		mst.msgQueue.add(buf.toString());
		SendMessage(buf.toString());
	}

	public PlayerData GetPlayer(int userID) {
		if (playerData.containsKey(userID)) {
			return playerData.get(userID);
		} else {
			return Join(userID);
		}
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

		Object[] pArray = playerData.keySet().toArray();
		Arrays.sort(pArray);

		for (int i : playerData.keySet()) {
			if (i == 0) {
				continue;
			}
			if (i == myNumberInt && ID != myNumberInt) {
				LoadPlankton();
			}
			break;
		}

		return p;
	}

	public void Disconnect(int userID) {
		for (CanEatObj p : GetPlayer(userID).planariaData.values()) {
			drow.Delete(p);
			p = null;
		}
		playerData.remove(userID);
	}

	private void resetField() {
		for (PlayerData p : playerData.values()) {
			Disconnect(p.playerID);
		}

		drow.panel.removeAll();
		drow.repaint();
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
		buf.append(p.current.x);
		buf.append(" ");
		buf.append(p.current.y);
		buf.append(" ");
		buf.append(p.size);
		SendMessage(buf.toString());
	}

	public void Search(Planaria p) {

		for (PlayerData player : playerData.values()) {
			for (CanEatObj planaria : player.planariaData.values()) {
				if (planaria == p) {
					continue;
				}

				if (Math.hypot(planaria.current.x - p.current.x, planaria.current.y - p.current.y) <= p.size / 9
						&& planaria.size < p.size * 0.9) {
					Eat(p, player.playerID, planaria.localId, planaria.size);
				}
			}
		}
	}

	public void Eat(Planaria myPlanaria, int userID, int planariaID, int size) {

		if (userID != myNumberInt) {
			score += size;
		}

		myPlanaria.setData(-1, -1, myPlanaria.size + size);
		Delete(userID, planariaID);
		SendMessage("Delete " + userID + " " + planariaID);

		if(userID == 0) {
			AUDIO.EAT_1.play();
		} else {
			AUDIO.EAT_2.play();
		}
	}

	public Point searchSpawnPoint() {
		Random r = new Random();

		int x, y;

		while (true) {
			x = r.nextInt(fieldSize);
			y = r.nextInt(fieldSize);

			if (canSpawn(x, y)) {
				break;
			}
		}

		return new Point(x, y);
	}

	private boolean canSpawn(int x, int y) {

		for (PlayerData player : playerData.values()) {
			if (player.playerID == 0) {
				continue;
			}

			for (CanEatObj planaria : player.planariaData.values()) {

				if (Math.hypot(planaria.current.x - x, planaria.current.y - y) <= planaria.size / 9) {
					return false;
				}
			}
		}

		return true;
	}

	public static void main(String[] args) {
		new MyClient();
		AUDIO.init();

		AUDIO.BGM.loop();
	}
}
