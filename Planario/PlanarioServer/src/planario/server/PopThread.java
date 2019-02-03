package planario.server;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PopThread extends Thread {

	public static ConcurrentHashMap<Integer, Plankton> planktonData = new ConcurrentHashMap<Integer, Plankton>();
	private Random random = new Random();
	private static final int fieldSize = 4000;
	private static final int MAX_PLANKTON = 300;

	public PopThread() {

	}

	public void run() {
		while (true) {

			int count = planktonData.size();
			if (count <= MAX_PLANKTON && random.nextInt(10) <= 1) {

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

		System.out.println("Pop Plankton " + p.getId());
		PlanarioServer.SendAll(p.toString(), "");
	}

	public static void delete(int id) {
		planktonData.remove(id);

		System.out.println("Delete Plankton " + id);
	}
}
