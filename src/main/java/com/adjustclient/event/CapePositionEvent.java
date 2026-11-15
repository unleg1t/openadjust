package com.adjustclient.event;

public final class CapePositionEvent extends Event {
    public net.minecraft.client.entity.AbstractClientPlayer player;
    public double prevChasingPosX;
    public double prevChasingPosY;
    public double prevChasingPosZ;
    public double chasingPosX;
    public double chasingPosY;
    public double chasingPosZ;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public double posX;
    public double posY;
    public double posZ;
    public float prevRenderYawOffset;
    public float renderYawOffset;

    public CapePositionEvent(net.minecraft.client.entity.AbstractClientPlayer player,
            double prevChasingPosX, double prevChasingPosY, double prevChasingPosZ,
            double chasingPosX, double chasingPosY, double chasingPosZ,
            double prevPosX, double prevPosY, double prevPosZ, double posX, double posY, double posZ,
            float prevRenderYawOffset, float renderYawOffset) {
        this.player = player;
        this.prevChasingPosX = prevChasingPosX;
        this.prevChasingPosY = prevChasingPosY;
        this.prevChasingPosZ = prevChasingPosZ;
        this.chasingPosX = chasingPosX;
        this.chasingPosY = chasingPosY;
        this.chasingPosZ = chasingPosZ;
        this.prevPosX = prevPosX;
        this.prevPosY = prevPosY;
        this.prevPosZ = prevPosZ;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.prevRenderYawOffset = prevRenderYawOffset;
        this.renderYawOffset = renderYawOffset;
    }
}
