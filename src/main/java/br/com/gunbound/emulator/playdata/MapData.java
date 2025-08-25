package br.com.gunbound.emulator.playdata;

import java.util.List;

public class MapData {
	// Os nomes das variáveis devem ser EXATAMENTE iguais aos do JSON
	private int map_id;
	private String map_name_en;
	private List<SpawnPoint> positions_a_side;
	private List<SpawnPoint> positions_b_side;

	// Getters
	public int getMapId() {
		return map_id;
	}

	public String getMapName() {
		return map_name_en;
	}

	public List<SpawnPoint> getPositionsASide() {
		return positions_a_side;
	}

	public List<SpawnPoint> getPositionsBSide() {
		return positions_b_side;
	}
}