package planario;

import java.net.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.Point;
import java.io.*;

public class MyClient {
	PrintWriter out;// 出力用のライター

	public ConcurrentHashMap<Integer, PlayerData> playerData = new ConcurrentHashMap<Integer, PlayerData>();
	private int myNumberInt;
	private Drow drow;
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

	public void access(String serverIP) {

		// 重複してメソッドを実行させないようにフラグで管理
		if (accessFlag) {
			return;
		} else {
			accessFlag = true;
		}

		if (serverIP.equals("")) {
			serverIP = "localhost";
		}

		String ips[] = serverIP.split(":");

		int port = 10000;

		if (ips.length == 2) {
			serverIP = ips[0];
			port = Integer.parseInt(ips[1]);
		}

		// サーバに接続する
		Socket socket = null;
		try {
			InetSocketAddress endpoint = new InetSocketAddress(serverIP, port);
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

			drow.setTitleError(err);
			accessFlag = false;
			return;
		}

		playerData.put(0, new PlayerData(0));
		planktons = getPlayer(0);

		MesgRecvThread mrt = new MesgRecvThread(socket);// 受信用のスレッドを作成する
		mrt.start();// スレッドを動かす（Runが動く）

		mst = new MesgSendThread(this);

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

				initMyNumber(br); // 自分のIDを取得

				joinGame(getMyID(), skin);

				getOptions(br);

				drow.Login();
				score = 0;
				sendMessage("Join " + getMyID() + " " + skin);

				while (true) {
					String inputLine = br.readLine();// データを一行分だけ読み込んでみる
					if (inputLine != null) {// 読み込んだときにデータが読み込まれたかどうかをチェックする
						String[] inputTokens = inputLine.split(" "); // 入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];// コマンドの取り出し．１つ目の要素を取り出す

						switch (cmd) {
						case "Update":
							update(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]),
									Integer.parseInt(inputTokens[3]), Integer.parseInt(inputTokens[4]),
									Integer.parseInt(inputTokens[5]));
							break;
						case "Delete":
							delete(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]));
							break;
						case "Join":
							if (Integer.parseInt(inputTokens[1]) == getMyID()) {
								break;
							}
							joinGame(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]));
							break;
						case "Disconnect":
							disconnect(Integer.parseInt(inputTokens[1]));
							break;
						case "Pop":
							pop(Integer.parseInt(inputTokens[1]), Integer.parseInt(inputTokens[2]),
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
					if (getPlayer(getMyID()).planariaData.size() == 0) {
						drow.setGameOver();
						sendMessage("BYE");
						endGame();

						break;
					}
				}
				socket.close();
			} catch (IOException e) {
				System.err.println("エラーが発生しました: " + e);
				drow.setTitlePane();
				drow.setTitleError("サーバーとの接続が切れました");
				endGame();
			}
		}
	}

	private void initMyNumber(BufferedReader br) {
		String myNumberStr;
		try {
			myNumberStr = br.readLine();
			this.myNumberInt = Integer.parseInt(myNumberStr);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			initMyNumber(br);
		}
	}

	public int getMyID() {
		return this.myNumberInt;
	}

	private void setSkin(int userID, int skinID) {
		if (userID != getMyID()) {
			joinGame(userID, skinID);
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
	public void update(int userID, int planariaID, int posX, int posY, int size) {
		if (userID != getMyID()) {

			PlayerData player = getPlayer(userID);

			if (player == null) {
				return;
			}

			if (player.planariaData.containsKey(planariaID)) {
				((Planaria) player.planariaData.get(planariaID)).setData(posX, posY, size);
			} else {
				System.out.println("NotFound : " + planariaID);
				drow.create(player.getSkin(), posX, posY, size, userID, planariaID);
			}
		}
	}

	public void pop(int planktonID, int posX, int posY, int virus) {
		if (!planktons.planariaData.containsKey(planktonID)) {
			Plankton plankton;

			if (virus == 1) {
				plankton = drow.popVirus(posX, posY, planktonID);
			} else {
				plankton = drow.popPlankton(posX, posY, planktonID);
			}
			planktons.planariaData.put(planktonID, plankton);
		}
	}

	public PlayerData getPlayer(int userID) {
		return playerData.get(userID);
	}

	// userID planariaID
	public void delete(int userID, int planariaID) {
		drow.delete(getPlayer(userID).planariaData.get(planariaID));
		getPlayer(userID).planariaData.remove(planariaID);
	}

	// userID posX posY
	public PlayerData joinGame(int ID, int skin) {

		PlayerData p = new PlayerData(ID, skin);
		playerData.put(ID, p);

		return p;
	}

	public void disconnect(int userID) {
		for (EatableObj p : getPlayer(userID).planariaData.values()) {
			drow.delete(p);
			p = null;
		}
		playerData.remove(userID);
	}

	private void endGame() {
		loginFlag = false;
		resetField();
		accessFlag = false; // アクセスメソッドを使用可能に

		AUDIO.BGM.stop();
		AUDIO.END.play();
	}

	private void resetField() {

		playerData.clear();
		drow.fieldReset();
	}

	public void sendMessage(String msg) {
		try {
			out.println(msg);
			out.flush();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void sendMyPlanariaData(Planaria p) {
		StringBuilder buf = new StringBuilder();
		buf.append("Update ");
		buf.append(getMyID());
		buf.append(" ");
		buf.append(p.getID());
		buf.append(" ");
		buf.append(p.current.x);
		buf.append(" ");
		buf.append(p.current.y);
		buf.append(" ");
		buf.append(p.size);
		sendMessage(buf.toString());
	}

	public void search(Planaria p) {

		for (PlayerData player : playerData.values()) {
			for (EatableObj planaria : player.planariaData.values()) {
				if (planaria == p) {
					continue;
				}

				if (Math.hypot(planaria.current.x - p.current.x, planaria.current.y - p.current.y) <= p.size / 3) {
					if (planaria.size < p.size * 0.9) {
						eat(p, player.getID(), planaria);
					} else if (getMyID() == player.getID()) {
						int x = p.current.x - planaria.current.x;
						int y = p.current.y - planaria.current.y;

						double diff = Math.hypot(x, y);

						x = (int) (x / diff * (p.size + planaria.size) / 4);
						y = (int) (y / diff * (p.size + planaria.size) / 4);

						p.setNext(planaria.current.x + x, planaria.current.y + y);
					}
				}
			}
		}
	}

	public void eat(Planaria myPlanaria, int userID, EatableObj p) {
		int size = p.size;

		if (userID != getMyID()) {
			if (userID == 0) {
				size = planktonScore;

				if (((Plankton) p).isVirus()) {
					drow.virusSpilit(myPlanaria);
				}
			}
			score += size;
		}

		myPlanaria.setEatSize(myPlanaria.size + size);
		delete(userID, p.getID());
		sendMessage("Delete " + userID + " " + p.getID());

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
