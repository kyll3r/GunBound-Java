package br.com.gunbound.emulator.model.entities.game;

import java.sql.Timestamp;

import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;

public class PlayerAvatar {
	private int idx;
	private int item;
	private String wearing="0";
	private String acquisition;
	private Timestamp expire;
	private int volume=1;
	private String placeOrder="0";
	private String recovered="0";
	private String expireType="I";

	public PlayerAvatar() {}
	
	public PlayerAvatar(ChestDTO chestDTO) {
		this.idx = chestDTO.getIdx();
		this.item = chestDTO.getItem();
		this.wearing = chestDTO.getWearing();
		this.acquisition = chestDTO.getAcquisition();
		this.expire = chestDTO.getExpire();
		this.volume = chestDTO.getVolume();
		this.placeOrder = chestDTO.getPlaceOrder();
		this.recovered = chestDTO.getRecovered();
		this.expireType = chestDTO.getExpireType();
	}

	// Getters e setters:
	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public Integer getItem() {
		return item;
	}

	public void setItem(Integer item) {
		this.item = item;
	}

	public String getWearing() {
		return wearing;
	}

	public void setWearing(String wearing) {
		this.wearing = wearing;
	}

	public String getAcquisition() {
		return acquisition;
	}

	public void setAcquisition(String acquisition) {
		this.acquisition = acquisition;
	}

	public Timestamp getExpire() {
		return expire;
	}

	public void setExpire(Timestamp expire) {
		this.expire = expire;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public String getPlaceOrder() {
		return placeOrder;
	}

	public void setPlaceOrder(String placeOrder) {
		this.placeOrder = placeOrder;
	}

	public String getRecovered() {
		return recovered;
	}

	public void setRecovered(String recovered) {
		this.recovered = recovered;
	}

	public String getExpireType() {
		return expireType;
	}

	public void setExpireType(String expireType) {
		this.expireType = expireType;
	}
}
