package planario;

import java.util.ArrayDeque;
import java.util.Queue;

public class MesgSendThread extends Thread {
	MyClient mc;
	public Queue<String> msgQueue;

	public MesgSendThread(MyClient mc) {
		this.mc = mc;
		msgQueue = new ArrayDeque<String>();
	}

	public void run() {
		try {
			int count;
			while (mc.loginFlag) {
				count = mc.GetPlayer(mc.myNumberInt).planariaData.size();
				Planaria[] tmp = new Planaria[count];
				mc.GetPlayer(mc.myNumberInt).planariaData.values().toArray(tmp);
				for (Planaria p : tmp) {
					mc.SendMyPlanariaData(p);
				}

				if (msgQueue.size() > 0) {
					mc.SendMessage(msgQueue.poll());
				}

				sleep(30);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
