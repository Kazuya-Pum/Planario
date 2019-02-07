package planario;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {
	private final int playerID;
	private final int skin;

	public ConcurrentHashMap<Integer, EatableObj> planariaData = new ConcurrentHashMap<Integer, EatableObj>();

	public PlayerData(int ID) {
		this(ID, 0);
	}

	public PlayerData(int ID, int skin) {
		this.playerID = ID;
		this.skin = skin;
	}

	public int getID() {
		return playerID;
	}

	public int getSize() {
		return planariaData.size();
	}

	public int getSkin() {
		return skin;
	}
}
