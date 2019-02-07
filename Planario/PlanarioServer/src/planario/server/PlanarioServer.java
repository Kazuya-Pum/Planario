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

//�X���b�h���i�e�N���C�A���g�ɉ����āj
class ClientProcThread extends Thread {
	private int number;// �����̔ԍ�
	@SuppressWarnings("unused")
	private Socket incoming;
	@SuppressWarnings("unused")
	private InputStreamReader myIsr;
	private BufferedReader myIn;
	private PrintWriter myOut;
	private String mySkin = "0";// �ڑ��҂�skin�ԍ�

	public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
		number = n;
		incoming = i;
		myIsr = isr;
		myIn = in;
		myOut = out;
	}

	public void run() {
		try {
			myOut.println(number);// ���񂾂��Ă΂��
			myOut.println("field " + PlanarioServer.getFieldSize());
			PlanarioServer.sendAllPlankton(myOut);
			mySkin = myIn.readLine();// ���߂Đڑ������Ƃ��̈�s�ڂ͖��O
			PlanarioServer.sendAllPlayerSkin(myOut);

			while (true) {// �������[�v�ŁC�\�P�b�g�ւ̓��͂��Ď�����
				String str = myIn.readLine();
				if (str != null) {// ���̃\�P�b�g�i�o�b�t�@�j�ɓ��͂����邩���`�F�b�N
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						throw new Exception();
					} else {

						String[] inputTokens = str.split(" ");
						if (inputTokens[0].equals("Delete") && inputTokens[1].equals("0")) {
							PopThread.delete(Integer.parseInt(inputTokens[2]));
						}
					}
					PlanarioServer.sendAll(str);// �T�[�o�ɗ������b�Z�[�W�͐ڑ����Ă���N���C�A���g�S���ɔz��
				}
			}
		} catch (Exception e) {
			// �����Ƀv���O���������B����Ƃ��́C�ڑ����؂ꂽ�Ƃ�
			PlanarioServer.addText("Disconnect from client No." + number);
			PlanarioServer.removeClient(number);// �ڑ����؂ꂽ�̂Ńt���O��������
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
	public static PopThread plankton; // �v�����N�g���𐶐�����X���b�h
	private static int fieldSize = 4000;	// �t�B�[���h�̍L��
	private static int maxPlankton = 300;
	private static int maxConnection = 20;// �ő�ڑ���

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

	// Gui�̃e�L�X�g���X�V
	public static void addText(String str) {
		System.out.println(str);
		guiTextQueue.add(str);

		// ��ꂽ��ӂ邢�e�L�X�g���̂Ă�
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

	// �S���Ƀ��b�Z�[�W�𑗂�
	public static void sendAll(String str) {
		// ����ꂽ�������b�Z�[�W��ڑ����Ă���S���ɔz��
		for (ClientProcThread c : myClientProcThread.values()) {
			c.getOut().println(str);
			c.getOut().flush();// �o�b�t�@���͂��o�������o�b�t�@�ɂ���S�Ẵf�[�^�������ɑ��M����
		}
	}

	// �v�����N�g���̃f�[�^���w��̃N���C�A���g�ɑ��M����
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

	// �N���C�A���g�̏����폜����
	public static void removeClient(int n) {
		myClientProcThread.remove(n);
		IncomingThread.updateMemberCount();

		incomingThread.checkCapacity();
	}

	// �������琔����Ԃ�
	private static int getCount(String arg) {
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(arg);

		if (m.find()) {
			return Integer.parseInt(m.group());
		}
		return 0;
	}

	// main�v���O����
	public static void main(String[] args) {

		boolean gui = true;
		int port = 10000;

		// �����`�F�b�N
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
		incomingThread = new IncomingThread(port); // ������ɃX���b�h��ҋ@�����邽�߂ɕʃX���b�h
		incomingThread.start();
	}
}

class IncomingThread extends Thread {
	private static int member;// �ڑ����Ă��郁���o�[�̐�

	private ServerSocket server;
	private final int port;

	public IncomingThread(int port) {
		this.port = port;
	}

	public void run() {
		int n = 1;
		member = 0;// �N���ڑ����Ă��Ȃ��̂Ń����o�[���͂O
		try {
			server = createSocket();
			PlanarioServer.addText("The Planar.io Server has launched!");

			PlanarioServer.plankton.start();
			while (true) {
				Socket incoming = server.accept();
				PlanarioServer.addText("Accept client No." + n);
				// �K�v�ȓ��o�̓X�g���[�����쐬����
				InputStreamReader isr = new InputStreamReader(incoming.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);

				PlanarioServer.myClientProcThread.put(n, new ClientProcThread(n, incoming, isr, in, out));// �K�v�ȃp�����[�^��n���X���b�h���쐬
				PlanarioServer.myClientProcThread.get(n).start();// �X���b�h���J�n����
				updateMemberCount(); // �����o�[�̐����X�V����
				n++;

				checkCapacity(); // ������m�F
			}
		} catch (Exception e) {
			PlanarioServer.addText("�\�P�b�g�쐬���ɃG���[���������܂���: " + e);
		}
	}

	// �����o�[�����X�V
	public static void updateMemberCount() {
		member = PlanarioServer.myClientProcThread.size();
	}

	private ServerSocket createSocket() throws IOException {
		// 10000�ԃ|�[�g�𗘗p����
		return new ServerSocket(port);
	}

	synchronized public void checkCapacity() {
		PlanarioServer.addText("member: " + member + "/" + PlanarioServer.getMaxConnection());

		try {
			if (member >= PlanarioServer.getMaxConnection()) {
				server.close(); // �\�P�b�g�����
				wait(); // �X���b�h��ҋ@
			} else if (server.isClosed()) {
				server = createSocket(); // �\�P�b�g���쐬
				notify(); // �X���b�h���ĊJ
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
