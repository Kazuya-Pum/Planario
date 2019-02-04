package planario.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

//スレッド部（各クライアントに応じて）
class ClientProcThread extends Thread {
	private int number;// 自分の番号
	@SuppressWarnings("unused")
	private Socket incoming;
	@SuppressWarnings("unused")
	private InputStreamReader myIsr;
	private BufferedReader myIn;
	private PrintWriter myOut;
	private String mySkin = "0";// 接続者のskin番号

	public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
		number = n;
		incoming = i;
		myIsr = isr;
		myIn = in;
		myOut = out;
	}

	public void run() {
		try {
			myOut.println(number);// 初回だけ呼ばれる
			myOut.println("field " + PlanarioServer.getFieldSize());
			PlanarioServer.sendAllPlankton(myOut);
			mySkin = myIn.readLine();// 初めて接続したときの一行目は名前
			PlanarioServer.sendAllPlayerSkin(myOut);

			while (true) {// 無限ループで，ソケットへの入力を監視する
				String str = myIn.readLine();
				if (str != null) {// このソケット（バッファ）に入力があるかをチェック
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						throw new Exception();
					} else {

						String[] inputTokens = str.split(" ");
						if (inputTokens[0].equals("Delete") && inputTokens[1].equals("0")) {
							PopThread.delete(Integer.parseInt(inputTokens[2]));
						}
					}
					PlanarioServer.SendAll(str);// サーバに来たメッセージは接続しているクライアント全員に配る
				}
			}
		} catch (Exception e) {
			// ここにプログラムが到達するときは，接続が切れたとき
			System.out.println("Disconnect from client No." + number);
			PlanarioServer.removeClient(number);// 接続が切れたのでフラグを下げる
			PlanarioServer.SendAll("Disconnect " + number);
		}
	}

	public PrintWriter getOut() {
		return myOut;
	}

	public String getSkin() {
		return mySkin;
	}

	public int getNumber() {
		return number;
	}
}

class PlanarioServer {
	public static ConcurrentHashMap<Integer, ClientProcThread> myClientProcThread;
	private static IncomingThread incomingThread;
	public static PopThread plankton; // プランクトンを生成するスレッド
	private static int fieldSize = 4000;
	private static int maxPlankton = 300;
	private static int maxConnection = 20;// 最大接続数

	public static int getFieldSize() {
		return fieldSize;
	}

	public static int getMaxPlankton() {
		return maxPlankton;
	}

	public static int getMaxConnection() {
		return maxConnection;
	}

	// 全員にメッセージを送る
	public static void SendAll(String str) {
		// 送られた来たメッセージを接続している全員に配る
		for (ClientProcThread c : myClientProcThread.values()) {
			c.getOut().println(str);
			c.getOut().flush();// バッファをはき出す＝＞バッファにある全てのデータをすぐに送信する
		}
	}

	// プランクトンのデータを指定のクライアントに送信する
	public static void sendAllPlankton(PrintWriter myOut) {
		for (Plankton p : PopThread.getData()) {
			myOut.println(p.toString());
			myOut.flush();
		}
	}

	public static void sendAllPlayerSkin(PrintWriter myOut) {
		for (ClientProcThread c : myClientProcThread.values()) {
			myOut.println("Skin " + c.getNumber() + " " + c.getSkin());
			myOut.flush();
		}
	}

	// クライアントの情報を削除する
	public static void removeClient(int n) {
		myClientProcThread.remove(n);
		IncomingThread.updateMemberCount();

		incomingThread.checkCapacity();
	}

	// 引数から数字を返す
	private static int getCount(String arg) {
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(arg);

		if (m.find()) {
			return Integer.parseInt(m.group());
		}
		return 0;
	}

	// mainプログラム
	public static void main(String[] args) {

		if (args.length > 0) {
			System.out.println("options: ");
			for (String arg : args) {
				if (arg.matches("maxPlayer=[0-9]+")) {
					int max = getCount(arg);
					maxConnection = max;
					System.out.println("maxPlayer=" + max);

				} else if (arg.matches("field=[0-9]+")) {
					int field = getCount(arg);
					fieldSize = field;
					System.out.println("field=" + field);

				} else if (arg.matches("maxPlankton=[0-9]+")) {
					int max = getCount(arg);
					maxPlankton = max;
					System.out.println("maxPlankton=" + max);

				}
			}
		}

		myClientProcThread = new ConcurrentHashMap<Integer, ClientProcThread>();

		plankton = new PopThread();
		incomingThread = new IncomingThread(); // 定員時にスレッドを待機させるために別スレッド
		incomingThread.start();
	}
}

class IncomingThread extends Thread {
	private static int member;// 接続しているメンバーの数

	private ServerSocket server;

	public IncomingThread() {

	}

	public void run() {
		int n = 1;
		member = 0;// 誰も接続していないのでメンバー数は０
		try {
			server = createSocket();
			System.out.println("The Planar.io Server has launched!");

			PlanarioServer.plankton.start();
			while (true) {
				Socket incoming = server.accept();
				System.out.println("Accept client No." + n);
				// 必要な入出力ストリームを作成する
				InputStreamReader isr = new InputStreamReader(incoming.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

				PlanarioServer.myClientProcThread.put(n, new ClientProcThread(n, incoming, isr, in, out));// 必要なパラメータを渡しスレッドを作成
				PlanarioServer.myClientProcThread.get(n).start();// スレッドを開始する
				updateMemberCount(); // メンバーの数を更新する
				n++;

				checkCapacity(); // 定員を確認
			}
		} catch (Exception e) {
			System.err.println("ソケット作成時にエラーが発生しました: " + e);
		}
	}

	public static void updateMemberCount() {
		member = PlanarioServer.myClientProcThread.size();
	}

	private ServerSocket createSocket() throws IOException {
		// 10000番ポートを利用する
		return new ServerSocket(10000);
	}

	synchronized public void checkCapacity() {
		System.out.println("member: " + member + "/" + PlanarioServer.getMaxConnection());

		try {
			if (member >= PlanarioServer.getMaxConnection()) {
				server.close(); // ソケットを閉じる
				wait(); // スレッドを待機
			} else if (server.isClosed()) {
				server = createSocket(); // ソケットを作成
				notify(); // スレッドを再開
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
