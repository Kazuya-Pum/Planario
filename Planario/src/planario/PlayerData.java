package planario;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
	int playerID;
	List<Planaria> planariaData = new ArrayList<Planaria>();

	public PlayerData(int ID) {
		playerID = ID;
	}
}
