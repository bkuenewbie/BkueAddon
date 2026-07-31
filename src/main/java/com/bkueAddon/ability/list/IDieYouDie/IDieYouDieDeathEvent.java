package com.bkueAddon.ability.list.IDieYouDie;
import daybreak.abilitywar.ability.event.AbilityEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class IDieYouDieDeathEvent extends AbilityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @Nonnull HandlerList getHandlers() {
        return handlers;
    }

    private final IDieYouDie iDieYouDie;
    private final Participant target;

    IDieYouDieDeathEvent(IDieYouDie iDieYouDie, Participant target) {
        super(iDieYouDie);
        this.iDieYouDie = iDieYouDie;
        this.target = target;
    }

    @Override
    public IDieYouDie getAbility() {
        return iDieYouDie;
    }

    public Participant getTarget() {
        return target;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}