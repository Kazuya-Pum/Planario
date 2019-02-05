package planario;

public class MesgSendThread extends Thread {
	private MyClient mc;

	public MesgSendThread(MyClient mc) {
		this.mc = mc;
	}

	public void run() {
		try {
			while (mc.loginFlag) {

				PlayerData player = mc.getPlayer(mc.getMyID());
				for (EatableObj p : player.planariaData.values()) {
					mc.sendMyPlanariaData((Planaria) p);
				}

				sleep(Drow.FPS);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {

		}
	}
}
