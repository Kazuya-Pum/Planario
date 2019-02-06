package planario.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;

import java.io.InputStreamReader;
import java.awt.Dimension;
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
					PlanarioServer.sendAll(str);// サーバに来たメッセージは接続しているクライアント全員に配る
				}
			}
		} catch (Exception e) {
			// ここにプログラムが到達するときは，接続が切れたとき
			PlanarioServer.addText("Disconnect from client No." + number);
			PlanarioServer.removeClient(number);// 接続が切れたのでフラグを下げる
			PlanarioServer.sendAll("Disconnect " + number);
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

	private static Queue<String> guiTextQueue = new ArrayDeque<String>();
	private static JLabel guiText = new JLabel();
	private static int maxLine = 25;

	public static int getFieldSize() {
		return fieldSize;
	}

	public static int getMaxPlankton() {
		return maxPlankton;
	}

	public static int getMaxConnection() {
		return maxConnection;
	}

	// Guiのテキストを更新
	public static void addText(String str) {
		System.out.println(str);
		guiTextQueue.add(str);

		// 溢れたらふるいテキストを捨てる
		if (guiTextQueue.size() > maxLine) {
			guiTextQueue.poll();
		}

		StringBuilder buf = new StringBuilder();
		buf.append("<html>");
		for (String s : guiTextQueue) {
			buf.append(s);
			buf.append("<br>");
		}
		buf.append("</html>");

		guiText.setText(buf.toString());
	}

	// 全員にメッセージを送る
	public static void sendAll(String str) {
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

		boolean gui = true;
		int port = 10000;

		// 引数チェック
		if (args.length > 0) {
			addText("options: ");
			for (String arg : args) {
				if (arg.matches("maxPlayer=[0-9]+")) {
					int max = getCount(arg);
					maxConnection = max;
					addText("maxPlayer=" + max);

				} else if (arg.matches("field=[0-9]+")) {
					int field = getCount(arg);
					fieldSize = field;
					addText("field=" + field);

				} else if (arg.matches("maxPlankton=[0-9]+")) {
					int max = getCount(arg);
					maxPlankton = max;
					addText("maxPlankton=" + max);

				} else if (arg.matches("nogui")) {
					gui = false;
					addText("nogui");

				} else if (arg.matches("port=[0-9]+")) {
					port = getCount(arg);
					maxPlankton = port;
					addText("port=" + port);

				}
			}
		}

		if (gui) {
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(500, 500);
			frame.setTitle("Planar.io Server");

			Dimension d = frame.getContentPane().getSize();

			guiText.setSize(d.width, d.height);
			guiText.setVerticalAlignment(JLabel.TOP);
			frame.add(guiText);

			frame.setVisible(true);
		}

		myClientProcThread = new ConcurrentHashMap<Integer, ClientProcThread>();

		plankton = new PopThread();
		incomingThread = new IncomingThread(port); // 定員時にスレッドを待機させるために別スレッド
		incomingThread.start();
	}
}

class IncomingThread extends Thread {
	private static int member;// 接続しているメンバーの数

	private ServerSocket server;
	private final int port;

	public IncomingThread(int port) {
		this.port = port;
	}

	public void run() {
		int n = 1;
		member = 0;// 誰も接続していないのでメンバー数は０
		try {
			server = createSocket();
			PlanarioServer.addText("The Planar.io Server has launched!");

			PlanarioServer.plankton.start();
			while (true) {
				Socket incoming = server.accept();
				PlanarioServer.addText("Accept client No." + n);
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
			PlanarioServer.addText("ソケット作成時にエラーが発生しました: " + e);
		}
	}

	public static void updateMemberCount() {
		member = PlanarioServer.myClientProcThread.size();
	}

	private ServerSocket createSocket() throws IOException {
		// 10000番ポートを利用する
		return new ServerSocket(port);
	}

	synchronized public void checkCapacity() {
		PlanarioServer.addText("member: " + member + "/" + PlanarioServer.getMaxConnection());

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
