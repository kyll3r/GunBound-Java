package br.com.gunbound.emulator.model.entities.game;

public class PlayerGameResult {
	Integer normalGold=0;
	Integer BonusGold=0;
	Integer normalGp=0;
	Integer BonusGp=0;

	
	public PlayerGameResult(Integer normalGold, Integer bonusGold, Integer normalGp, Integer bonusGp) {
		super();
		this.normalGold = normalGold;
		BonusGold = bonusGold;
		this.normalGp = normalGp;
		BonusGp = bonusGp;
	}
	
	
	public Integer getNormalGold() {
		return normalGold;
	}
	public void setNormalGold(Integer normalGold) {
		this.normalGold = normalGold;
	}
	public Integer getBonusGold() {
		return BonusGold;
	}
	public void setBonusGold(Integer bonusGold) {
		BonusGold = bonusGold;
	}
	public Integer getNormalGp() {
		return normalGp;
	}
	public void setNormalGp(Integer normalGp) {
		this.normalGp = normalGp;
	}
	public Integer getBonusGp() {
		return BonusGp;
	}
	public void setBonusGp(Integer bonusGp) {
		BonusGp = bonusGp;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PlayerGameResult [normalGold=");
		builder.append(normalGold);
		builder.append(", BonusGold=");
		builder.append(BonusGold);
		builder.append(", normalGp=");
		builder.append(normalGp);
		builder.append(", BonusGp=");
		builder.append(BonusGp);
		builder.append("]");
		return builder.toString();
	}
	
	

}
