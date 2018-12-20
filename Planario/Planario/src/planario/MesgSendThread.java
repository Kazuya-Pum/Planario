package planario;

// TODO 送信部を別スレッドにする場合はこれを使う
public class MesgSendThread extends Thread {
	MyClient mc;

	public MesgSendThread(MyClient mc) {
		this.mc = mc;
	}

	public void run() {
		try {
			int count;
			while(true) {
				count = mc.GetPlayer(mc.myNumberInt).planariaData.size();
				Planaria[] tmp = new Planaria[count];
				mc.GetPlayer(mc.myNumberInt).planariaData.values().toArray(tmp);
				for (Planaria p : tmp) {
					mc.SendMyPlanariaData(p);
				}
				sleep(30);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
