package BkueNewbie.bkueAddon.ability.list;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "세컨드 윈드", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §b두 번째 호흡§f: 지친 신체를 가다듬고 생명력을 가속합니다.",
        "- 즉시 자신의 유실된 체력을 6만큼 직접 치유합니다.",
        "- 동시에 6초간 유지되는 노란색 임시 흡수 하트 코팅(흡수 2레벨)을 획득합니다."
})
public class SecondWind extends AbilityBase implements ActiveHandler {

    public SecondWind(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            SecondWind.class, "cooldown", 30, "# 호흡 재조율 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (cooldown.isCooldown()) return false;

            double maxHealth = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double currentHealth = getPlayer().getHealth();

            getPlayer().setHealth(Math.min(maxHealth, currentHealth + 6.0));
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 120, 1));

            getPlayer().sendMessage("§b[세컨드 윈드] 깊은 호흡으로 전열을 정비하고 끝까지 투지를 불태웁니다.");
            SoundLib.ENTITY_PLAYER_BREATH.playSound(getPlayer().getLocation(), 1.2f, 1.0f);
            SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer().getLocation(), 0.8f, 0.7f);

            ParticleLib.HEART.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.4f, 0.4f, 0.4f, 4, 0.0);

            cooldown.start();
            return true;
        }
        return false;
    }
}