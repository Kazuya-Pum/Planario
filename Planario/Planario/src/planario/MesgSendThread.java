package planario;

public class MesgSendThread extends Thread {
	private MyClient mc;

	public MesgSendThread(MyClient mc) {
		this.mc = mc;
	}

	public void run() {
		try {
			while (mc.loginFlag) {
				for (EatableObj p : mc.GetPlayer(mc.myNumberInt).planariaData.values()) {
					mc.SendMyPlanariaData((Planaria) p);
				}

				sleep(30);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
