package net.ccbluex.liquidbounce.features.module.modules.other.hackercheck;

import net.ccbluex.liquidbounce.features.module.modules.other.HackerDetector;
import net.ccbluex.liquidbounce.features.module.modules.other.hackercheck.checks.combat.AutoBlockCheck;
import net.ccbluex.liquidbounce.features.module.modules.other.hackercheck.checks.combat.VelocityCheck;
import net.ccbluex.liquidbounce.features.module.modules.other.hackercheck.checks.move.NoSlowCheck;
import net.ccbluex.liquidbounce.features.module.modules.other.hackercheck.checks.move.ScaffoldCheck;
import net.ccbluex.liquidbounce.features.module.modules.other.hackercheck.checks.rotation.RotationCheck;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

public class CheckManager {
    private final PlayerData data = new PlayerData();
    private static final Class<?>[] checksClz = {
            AutoBlockCheck.class,
            NoSlowCheck.class,
            ScaffoldCheck.class,
            RotationCheck.class,
            VelocityCheck.class
    };
    private final LinkedList<Check> checks = new LinkedList<>();
    private double totalVL = 0;
    private short addedTicks = 0;
    public CheckManager(EntityOtherPlayerMP target) {
        for (Class<?> clz : checksClz) {
            try {
                checks.add((Check) clz.getConstructor(EntityOtherPlayerMP.class).newInstance(target));
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void livingUpdate() {
        for (Check check : checks) {
            try {
                check.onLivingUpdate();
                if (check.wasFailed()) {
                    if (HackerDetector.shouldAlert()) ClientUtils.INSTANCE.displayChatMessage("§l§7[§l§9HackDetector§l§7]§F: " + check.handlePlayer.getDisplayName().getFormattedText() + "dectected for §C" + check.name);

                    totalVL += check.getPoint();
                    if (HackerDetector.catchPlayer(check.handlePlayer.getDisplayName().getFormattedText().toString(), check.reportName(), totalVL)) {
                        totalVL = -5;
                    }
                    addedTicks = 40;
                    check.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // reduce 0.1 per second
        if (--addedTicks <= 0) totalVL -= totalVL > 0 ? 0.005 : 0;
    }
    public void positionUpdate(double x, double y, double z) {
        for (Check check : checks) {
            try {
                check.positionUpdate(x, y, z);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
