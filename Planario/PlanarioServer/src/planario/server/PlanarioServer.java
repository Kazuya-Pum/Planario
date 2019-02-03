package planario.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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
	private String myName;// 接続者の名前

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

			myName = myIn.readLine();// 初めて接続したときの一行目は名前

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
					PlanarioServer.SendAll(str, myName);// サーバに来たメッセージは接続しているクライアント全員に配る
				}
			}
		} catch (Exception e) {
			// ここにプログラムが到達するときは，接続が切れたとき
			System.out.println("Disconnect from client No." + number + "(" + myName + ")");
			PlanarioServer.removeClient(number);// 接続が切れたのでフラグを下げる
			PlanarioServer.SendAll("Disconnect " + number, myName);
		}
	}

	public PrintWriter getOut() {
		return myOut;
	}
}

class PlanarioServer {
	private static int maxConnection = 50;// 最大接続数
	private static ConcurrentHashMap<Integer, ClientProcThread> myClientProcThread;
	private static int member;// 接続しているメンバーの数

	private static PopThread plankton;	// プランクトンを生成するスレッド

	// 全員にメッセージを送る
	public static void SendAll(String str, String myName) {
		// 送られた来たメッセージを接続している全員に配る
		for (ClientProcThread c : myClientProcThread.values()) {
			c.getOut().println(str);
			c.getOut().flush();// バッファをはき出す＝＞バッファにある全てのデータをすぐに送信する
		}
	}

	// プランクトンのデータを指定のクライアントに送信する
	public static void sendAllPlankton(PrintWriter myOut) {
		for (Plankton p : PopThread.planktonData.values()) {
			myOut.println(p.toString());
			myOut.flush();
		}
	}

	// クライアントの情報を削除する
	public static void removeClient(int n) {
		myClientProcThread.remove(n);
		member = myClientProcThread.size();
	}

	// mainプログラム
	public static void main(String[] args) {
		myClientProcThread = new ConcurrentHashMap<Integer, ClientProcThread>();

		int n = 1;
		member = 0;// 誰も接続していないのでメンバー数は０

		try (ServerSocket server = new ServerSocket(10000)) {
			// 10000番ポートを利用する
			System.out.println("The Planar.io Server has launched!");

			plankton = new PopThread();
			plankton.start();
			while (true) {
				if (member >= maxConnection) {
					continue;
				}
				Socket incoming = server.accept();
				System.out.println("Accept client No." + n);
				// 必要な入出力ストリームを作成する
				InputStreamReader isr = new InputStreamReader(incoming.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

				myClientProcThread.put(n, new ClientProcThread(n, incoming, isr, in, out));// 必要なパラメータを渡しスレッドを作成
				myClientProcThread.get(n).start();// スレッドを開始する
				sendAllPlankton(out);
				member = myClientProcThread.size(); // メンバーの数を更新する
				n++;
			}
		} catch (Exception e) {
			System.err.println("ソケット作成時にエラーが発生しました: " + e);
		}
	}
}
