package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "도곰", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §c반격 태세§f: 다음 공격이 자신에게 적중하면 피해를 무효화하고, 적에게 적중하면 피해가 30% 증가합니다. $[COOLDOWN_CONFIG]",
        "§8아이디어 제공: 도곰"
}, summarize = {
        "다음 공격을 막거나 강화합니다."
})
public class Ddobear extends AbilityBase implements ActiveHandler {

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Ddobear.class, "cooldown", 10,
            "# 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }

        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };

    public Ddobear(Participant participant) {
        super(participant);
    }

    private boolean skill;

    private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown() && !skill) {
            skill = true;
            SoundLib.BLOCK_ANVIL_PLACE.playSound(getPlayer());
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!skill) return;

        if (e.getDamager().equals(getPlayer())) {
            e.setDamage(e.getDamage() * 1.3);
            skill = false;
            cooldownTimer.start();
            SoundLib.BLOCK_ANVIL_PLACE.playSound(getPlayer());
        } else if (e.getEntity().equals(getPlayer())) {
            e.setCancelled(true);
            skill = false;
            cooldownTimer.start();
            SoundLib.BLOCK_ANVIL_PLACE.playSound(getPlayer());
        }
    }
}
