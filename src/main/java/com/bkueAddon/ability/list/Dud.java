package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(name = "꽝", rank = AbilityManifest.Rank.C, species = AbilityManifest.Species.HUMAN, explain = {
        "꽝!",
})
public class Dud extends AbilityBase {
    public Dud(Participant participant) {
        super(participant);
    }
}