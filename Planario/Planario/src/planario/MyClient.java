package planario;

import java.net.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Point;
import java.io.*;

public class MyClient {
	PrintWriter out;// 出力用のライター

	public ConcurrentHashMap<Integer, PlayerData> playerData = new ConcurrentHashMap<Integer, PlayerData>();
	public int myNumberInt;
	Drow drow;
	final int planktonSize = 10;
	final int planktonScore = 1;
	final int defualtSize = 30;
	PlayerData planktons;
	public static int fieldSize = 4000;
	public int score = 0;
	public boolean loginFlag = false;

	MesgSendThread mst;

	private static boolean accessFlag = false;

	public MyClient() {
		drow = new Drow(this);
		drow.setVisible(true);
	}

	public void Access(String serverIP) {

		// 重複してメソッドを実行させないようにフラグで管理
		if (accessFlag) {
			return;
		} else {
			accessFlag = true;
		}

		if (serverIP.equals("")) {
			serverIP = "localhost";
		}

		// サーバに接続する
		Socket socket = null;
		try {
			InetSocketAddress endpoint = new InetSocketAddress(serverIP, 10000);
			socket = new Socket();
			socket.connect(endpoint, 4000); // 4000msでtimeout
		} catch (Exception e) {
			System.err.println("エラーが発生しました: " + e);

			String err;
			switch (e.getClass().getSimpleName()) {
			case "SocketTimeoutException":
				err = "タイムアウトしました";
				break;
			case "UnknownHostException":
				err = "ホストを特定できません";
				break;
			default:
				err = "サーバーに接続できません";
			}

			drow.title.setErrorMsg(err);
			accessFlag = false;
			return;
		}

		playerData.put(0, new PlayerData(0));
		planktons = GetPlayer(0);

		MesgRecvThread mrt = new MesgRecvThread(socket);// 受信用のスレッドを作成する
		mrt.start();// スレッドを動かす（Runが動く）

		mst = new MesgSendThread(this);

		drow.title.hideErrorMsg();
		drow.hideTilePane();
	}

	// メッセージ受信のためのスレッド
	public class MesgRecvThread extends Thread {

		Socket socket;

		public MesgRecvThread(Socket s) {
			socket = s;
		}

		// 通信状況を監視し，受信データによって動作する
		public void run() {
			try {
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);

				int skin = SKINS.getSelect();
				out.println(skin);// 接続の最初にskin番号を送る

				myNumberInt = GetMyNumber(br);

				Join(myNumberInt, skin);

				getOptions(br);

				drow.Login();
				score = 0;
				SendMessage("Join " + myNumberInt + " " + skin);

				while (true) {
					String inputLine = br.readLine();// データを一行分だけ読み込んでみる
					if (inputLine != null) {// 読み込んだときにデータが読み込まれたかどうかをチェックする
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
							Join(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]));
							break;
						case "Disconnect":
							Disconnect(Integer.parseInt(inputTokens[1]));
							break;
						case "Pop":
							Pop(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]),
									Integer.parseInt(inputTokens[3]), Integer.parseInt(inputTokens[4]));
							break;
						case "Skin":
							setSkin(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]));
							break;
						default:
							break;
						}
					} else {
						break;
					}

					// 自身のプラナリアの数が0になればゲームオーバー判定
					if (GetPlayer(myNumberInt).planariaData.size() == 0) {
						drow.setGameOver();
						SendMessage("BYE");
						resetField();
						loginFlag = false;
						accessFlag = false; // アクセスメソッドを使用可能に

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

	private void setSkin(int userID, int skinID) {
		if (userID != myNumberInt) {
			Join(userID, skinID);
		}
	}

	private void getOptions(BufferedReader br) {
		String serverFieldSize;
		try {
			serverFieldSize = br.readLine();
			String[] inputTokens = serverFieldSize.split(" ");
			if (inputTokens[0].equals("field")) {
				fieldSize = Integer.parseInt(inputTokens[1]);
				System.out.println("fieldSize=" + fieldSize);
				drow.changeFieldSize(fieldSize);
			} else {
				getOptions(br);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// userID planariaID posX posY size
	public void Update(int userID, int planariaID, int posX, int posY, int size) {
		if (userID != myNumberInt) {

			PlayerData player = GetPlayer(userID);

			if (player == null) {
				return;
			}

			if (player.planariaData.containsKey(planariaID)) {
				((Planaria) player.planariaData.get(planariaID)).setCurrent(posX, posY, size);
			} else {
				System.out.println("NotFound : " + planariaID);
				drow.Create(player.getSkin(), posX, posY, size, userID, planariaID);
			}
		}
	}

	public void Pop(int planktonID, int posX, int posY, int virus) {
		if (!planktons.planariaData.containsKey(planktonID)) {
			Plankton plankton;

			if (virus == 1) {
				plankton = drow.PopVirus(posX, posY, planktonID);
			} else {
				plankton = drow.PopPlankton(posX, posY, planktonID);
			}
			planktons.planariaData.put(planktonID, plankton);
		}
	}

	public PlayerData GetPlayer(int userID) {
		if (playerData.containsKey(userID)) {
			return playerData.get(userID);
		} else {
			return null;
		}
	}

	// userID planariaID
	public void Delete(int userID, int planariaID) {
		drow.Delete(GetPlayer(userID).planariaData.get(planariaID));
		GetPlayer(userID).planariaData.remove(planariaID);
	}

	// userID posX posY
	public PlayerData Join(int ID, int skin) {

		PlayerData p = new PlayerData(ID, skin);
		playerData.put(ID, p);

		return p;
	}

	public void Disconnect(int userID) {
		for (EatableObj p : GetPlayer(userID).planariaData.values()) {
			drow.Delete(p);
			p = null;
		}
		playerData.remove(userID);
	}

	private void resetField() {

		playerData.clear();
		drow.fieldReset();
	}

	public void SendMessage(String msg) {
		try {
			out.println(msg);
			out.flush();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void SendMyPlanariaData(Planaria p) {
		StringBuilder buf = new StringBuilder();
		buf.append("Update ");
		buf.append(myNumberInt);
		buf.append(" ");
		buf.append(p.getID());
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
			for (EatableObj planaria : player.planariaData.values()) {
				if (planaria == p) {
					continue;
				}

				if (Math.hypot(planaria.current.x - p.current.x, planaria.current.y - p.current.y) <= p.size / 3) {
					if (planaria.size < p.size * 0.9) {
						Eat(p, player.getID(), planaria);
					} else if (myNumberInt == player.getID()) {
						int x = p.current.x - planaria.current.x;
						int y = p.current.y - planaria.current.y;

						double diff = Math.hypot(x, y);

						x = (int) (x / diff * (p.size + planaria.size) / 4);
						y = (int) (y / diff * (p.size + planaria.size) / 4);

						p.setData(planaria.current.x + x, planaria.current.y + y, -1);
					}
				}
			}
		}
	}

	public void Eat(Planaria myPlanaria, int userID, EatableObj p) {
		if (userID != myNumberInt) {
			if (userID == 0) {
				p.size = planktonScore;

				if (((Plankton) p).isVirus()) {
					drow.VirusSpilit(myPlanaria);
				}
			}
			score += p.size;
		}

		myPlanaria.setData(-1, -1, myPlanaria.size + p.size);
		Delete(userID, p.getID());
		SendMessage("Delete " + userID + " " + p.getID());

		if (userID == 0) {
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
			if (player.getID() == 0) {
				continue;
			}

			for (EatableObj planaria : player.planariaData.values()) {

				if (Math.hypot(planaria.current.x - x, planaria.current.y - y) <= planaria.size / 3) {
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
