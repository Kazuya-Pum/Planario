package planario.server;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PopThread extends Thread {

	private static ConcurrentHashMap<Integer, Plankton> planktonData = new ConcurrentHashMap<Integer, Plankton>();
	private Random random = new Random();
	private final int fieldSize;
	private final int maxPlankton;

	public PopThread() {
		this.fieldSize = PlanarioServer.getFieldSize();
		this.maxPlankton = PlanarioServer.getMaxPlankton();
	}

	public void run() {
		while (true) {

			int count = planktonData.size();
			if (count <= maxPlankton && random.nextInt(10) <= 1) {

				if (random.nextInt(20) == 0) {
					pop(true);
				} else {
					pop(false);
				}
			}

			try {
				sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void pop(boolean virus) {
		Plankton p = new Plankton(random.nextInt(fieldSize), random.nextInt(fieldSize), virus);
		planktonData.put(p.getId(), p);

		PlanarioServer.SendAll(p.toString());
	}

	public static void delete(int id) {
		planktonData.remove(id);
	}

	public static Collection<Plankton> getData() {
		return planktonData.values();
	}
}
