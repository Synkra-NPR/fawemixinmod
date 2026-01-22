package com.synkra.fawemixinmod.tacz.client.gui.overlay;

import net.minecraft.world.item.ItemStack;

public class KillFeedEntry {
    private final String killerName;
    private final String victimName;
    private final ItemStack weapon;
    private final long timestamp;
    private final boolean isKillerPlayer;
    private long lifeTime = 0;
    private long yAnimationStartTime = 0;
    private double xOffset = 0;
    private double yOffset = 0;

    public KillFeedEntry(String killerName, String victimName, ItemStack weapon, boolean isKillerPlayer) {
        this.killerName = killerName;
        this.victimName = victimName;
        this.weapon = weapon.copy();
        this.timestamp = System.currentTimeMillis();
        this.isKillerPlayer = isKillerPlayer;
    }

    public String getKillerName() {
        return killerName;
    }

    public String getVictimName() {
        return victimName;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isKillerPlayer() {
        return isKillerPlayer;
    }

    public long getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(long lifeTime) {
        this.lifeTime = lifeTime;
    }

    public long getYAnimationStartTime() {
        return yAnimationStartTime;
    }

    public void setYAnimationStartTime(long yAnimationStartTime) {
        this.yAnimationStartTime = yAnimationStartTime;
    }

    public double getXOffset() {
        return xOffset;
    }

    public void setXOffset(double xOffset) {
        this.xOffset = xOffset;
    }

    public double getYOffset() {
        return yOffset;
    }

    public void setYOffset(double yOffset) {
        this.yOffset = yOffset;
    }
}