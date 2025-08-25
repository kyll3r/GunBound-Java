package br.com.gunbound.emulator.model.DAO;

import java.util.List;

import br.com.gunbound.emulator.model.entities.DTO.MenuDTO;

public interface MenuDAO {

	 public List<MenuDTO> getAll();
	 public MenuDTO getByIdx(int idx);
	 public int insert(MenuDTO menu);
	 
}
