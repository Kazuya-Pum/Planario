package planario;

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
					StringBuilder buf = new StringBuilder();
					buf.append("Update ");
					buf.append(mc.myNumberInt);
					buf.append(" ");
					buf.append(p.localId);
					buf.append(" ");
					buf.append(p.posX);
					buf.append(" ");
					buf.append(p.posY);
					buf.append(" ");
					buf.append(p.size);
					mc.SendMessage(buf.toString());
				}
				sleep(40);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
