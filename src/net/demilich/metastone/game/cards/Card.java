package net.demilich.metastone.game.cards;

import java.util.EnumMap;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.GameTag;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.PlayCardAction;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.spells.desc.BattlecryDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.CardLocation;
import net.demilich.metastone.game.targeting.CardReference;
import net.demilich.metastone.game.targeting.IdFactory;

public abstract class Card extends Entity {

	private String description = "";
	private final CardType cardType;
	private final int manaCost;
	private final Rarity rarity;
	private final HeroClass classRestriction;
	private boolean collectible = true;
	private CardLocation location;
	private BattlecryDesc battlecry;
	private ValueProvider manaCostModifier;
	private final String cardId;

	public Card(CardDesc desc) {
		cardId = desc.id;
		setName(desc.name);
		setDescription(desc.description);
		setCollectible(desc.collectible);
		cardType = desc.type;
		rarity = desc.rarity;
		classRestriction = desc.heroClass;
		manaCost = desc.baseManaCost;
		
		if (desc.attributes != null) {
			tags.putAll(desc.attributes);
		}
		
		if (desc.manaCostModifier != null) {
			manaCostModifier = desc.manaCostModifier.create();
		}
		
		if (desc.passiveTrigger != null) {
			tags.put(GameTag.PASSIVE_TRIGGER, desc.passiveTrigger);
		}
	}
	
	public Card(String name, CardType cardType, Rarity rarity, HeroClass classRestriction, int manaCost) {
		setName(name);
		this.cardType = cardType;
		this.rarity = rarity;
		this.classRestriction = classRestriction;
		this.manaCost = manaCost;
		this.cardId = null;
	}

	@Override
	public Card clone() {
		Card clone = (Card) super.clone();
		clone.tags = new EnumMap<>(getTags());
		return clone;
	}

	public int getBaseManaCost() {
		return manaCost;
	}

	public BattlecryDesc getBattlecry() {
		return battlecry;
	}

	public String getCardId() {
		return cardId;
	}
	
	public CardReference getCardReference() {
		return new CardReference(getOwner(), getLocation(), getId(), getName());
	}

	public CardType getCardType() {
		return cardType;
	}

	public HeroClass getClassRestriction() {
		return classRestriction;
	}

	public Card getCopy() {
		Card copy = clone();
		copy.setId(IdFactory.UNASSIGNED);
		return copy;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public EntityType getEntityType() {
		return EntityType.CARD;
	}

	public CardLocation getLocation() {
		return location;
	}

	public int getManaCost(GameContext context, Player player) {
		int actualManaCost = manaCost + getTagValue(GameTag.MANA_COST_MODIFIER);
		if (manaCostModifier != null) {
			actualManaCost -= manaCostModifier.getValue(context, player, null, this);
		}
		return actualManaCost;
	}

	public Rarity getRarity() {
		return rarity;
	}

	@Override
	public int getTypeId() {
		return getCardId() != null ? getCardId().hashCode() : super.getTypeId();
	}

	public boolean hasBattlecry() {
		return this.battlecry != null;
	}

	public boolean isCollectible() {
		return collectible;
	}

	public boolean matchesFilter(String filter) {
		if (filter == null) {
			return true;
		}
		if (getRarity().toString().toLowerCase().contains(filter)) {
			return true;
		}
		String className = getClass().getName();
		if (filter.contains("gvg") && className.contains("goblins")) {
			return true;
		} else if (filter.contains("naxx") && className.contains("naxx")) {
			return true;
		} else if ((filter.contains("classic") || filter.contains("vanilla")) && !className.contains("naxx")
				&& !className.contains("goblins")) {
			return true;
		}
		String lowerCaseName = getName().toLowerCase();
		return lowerCaseName.contains(filter);
	}

	public abstract PlayCardAction play();

	public void setBattlecry(BattlecryDesc battlecry) {
		this.battlecry = battlecry;
	}

	public void setCollectible(boolean collectible) {
		this.collectible = collectible;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setLocation(CardLocation location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return String.format("[%s '%s' Manacost:%d]", getCardType(), getName(), manaCost);
	}

}
