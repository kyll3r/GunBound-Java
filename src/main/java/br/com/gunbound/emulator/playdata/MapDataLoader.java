package br.com.gunbound.emulator.playdata;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MapDataLoader {

	private static final Map<Integer, MapData> MAPS_BY_ID;

	static {
		System.out.println("Carregando dados dos mapas do arquivo map_data.json...");
		List<MapData> loadedMaps = Collections.emptyList();
		Gson gson = new Gson();

		// Define o tipo que esperamos: uma Lista de MapData
		Type mapListType = new TypeToken<List<MapData>>() {
		}.getType();

		// Tenta ler o arquivo a partir da pasta 'resources'
		try (InputStream is = MapDataLoader.class.getClassLoader().getResourceAsStream("map_data.json")) {
			if (is == null) {
				System.err.println("ERRO CRÍTICO: Não foi possível encontrar o arquivo map_data.json nos recursos!");
			} else {
				InputStreamReader reader = new InputStreamReader(is);
				loadedMaps = gson.fromJson(reader, mapListType);
			}
		} catch (Exception e) {
			System.err.println("ERRO CRÍTICO: Falha ao carregar ou analisar o map_data.json!");
			e.printStackTrace();
		}

		// Converte a lista para um mapa para buscas rápidas por ID
		MAPS_BY_ID = loadedMaps.stream().collect(Collectors.toMap(MapData::getMapId, Function.identity()));

		System.out.println(MAPS_BY_ID.size() + " mapas foram carregados com sucesso.");
	}

	/**
	 * Obtém os dados de um mapa pelo seu ID.
	 * 
	 * @param mapId O ID do mapa.
	 * @return O objeto MapData correspondente, ou null se não for encontrado.
	 */
	public static MapData getMapById(int mapId) {
		return MAPS_BY_ID.get(mapId);
	}

	/**
	 * Retorna uma lista com todos os mapas carregados.
	 * 
	 * @return Uma lista de todos os MapData.
	 */
	public static List<MapData> getAllMaps() {
		return List.copyOf(MAPS_BY_ID.values());
	}
}