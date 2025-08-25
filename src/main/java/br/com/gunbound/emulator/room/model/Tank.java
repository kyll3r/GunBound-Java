package br.com.gunbound.emulator.room.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeração para os Mobiles (Tanks) do GunBound.
 */
public enum Tank {
	ARMOR(0, "Armor"), 
	MAGE(1, "Mage"), 
	NAK(2, "Nak"), 
	TRICO(3, "Trico"), 
	BIGFOOT(4, "Bigfoot"), 
	BOOMER(5, "Boomer"),
	RAON(6, "Raon"), 
	LIGHTNING(7, "Lightning"), 
	JD(8, "J.D."), 
	ASATE(9, "A.Sate"), 
	ICE(10, "Ice"), 
	TURTLE(11, "Turtle"),
	GRUB(12, "Grub"), 
	ADUKA(13, "Aduka"), 
	DRAGON(17, "Dragon"), 
	KNIGHT(18, "Knight"), 
	RANDOM(255, "Random");

	private final int id;
	private final String name;

	private static final Map<Integer, Tank> BY_ID = new HashMap<>();

	static {
		for (Tank t : values()) {
			BY_ID.put(t.id, t);
		}
	}

	Tank(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static Tank fromId(int id) {
		return BY_ID.getOrDefault(id, RANDOM);
	}
}