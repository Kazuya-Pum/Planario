package planario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerData {
	final int playerID;
	int skin = 1;
//	public List<Planaria> planariaData = new ArrayList<Planaria>();
	public Map<Integer,Planaria> planariaData = new HashMap<Integer,Planaria>();

	public PlayerData(int ID) {
		playerID = ID;
	}
}
