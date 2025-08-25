package br.com.gunbound.emulator.model.entities.game;

import java.util.List;

import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.MenuDAO;
import br.com.gunbound.emulator.model.entities.DTO.MenuDTO;

public class GameMenu {
    private static GameMenu instance;
    private List<MenuDTO> menuList;

    private GameMenu() {
    	load(); //carrega o banco
    }

    public static synchronized GameMenu getInstance() {
        if (instance == null) {
            instance = new GameMenu();
        }
        return instance;
    }

    // Acesso somente leitura ao cache
    public List<MenuDTO> getMenus() {
        return menuList;
    }

    //buscar por No, Idx, etc.
    public MenuDTO getByNo(int no) {
        return menuList.stream()
            .filter(menu -> menu.getNo() != null && menu.getNo().equals(no))
            .findFirst()
            .orElse(null);
    }
    
    private void load() {
    	MenuDAO factory = DAOFactory.CreateMenuDao();
        this.menuList = factory.getAll(); // Carrega TODOS os itens uma vez
    }
    
    public void reload() {
    	load();
    }
}