package br.com.gunbound.emulator.model.DAO;

import br.com.gunbound.emulator.model.entities.DTO.UserDTO;

public interface UserDAO {
	public UserDTO getUserByUserId(String userIdQuery);
	
	//operacoes com Gold
	void updateMinusGold(String playerId, int value);
	void updateAddGold(String playerId, int value);
	
	//operacoes com Cash
	void updateMinusCash(String playerId, int value);
	void updateAddCash(String playerId, int value);
}
