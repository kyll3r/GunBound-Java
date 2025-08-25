package br.com.gunbound.emulator.model.entities.DTO;

import java.sql.Timestamp;

public class ChestDTO {
	private int idx;


	private int item;
	private String wearing;
	private String acquisition;
	private Timestamp expire;
	private int volume;
	private String placeOrder;
	private String recovered;
	private String ownerId;
	private String expireType;

	public ChestDTO() {}

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

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getExpireType() {
		return expireType;
	}

	public void setExpireType(String expireType) {
		this.expireType = expireType;
	}
}
