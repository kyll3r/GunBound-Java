package br.com.gunbound.emulator.model.DAO;

import java.util.List;

import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;

public interface ChestDAO {
	public List<ChestDTO> getAllAvatarsByOwnerId(String ownerId);
	public ChestDTO getByIdx(int idx);
	public int insert(ChestDTO chest);
	public boolean deleteByIdx(int idx);
	public boolean updatePlaceOrder(int idx, String novoPlaceOrder);
	public boolean updateAvatarWearing(Integer idx, String isWearing, String ownerId);
}
