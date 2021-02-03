package com.qzx.herostory.model;

/**
 * @author: qzx
 * @date: 2021/2/4 - 02 - 04 - 16:16
 * @description: 用户的移动状态
 * @version: 1.0
 */
public class MoveState {
    /**
     * 移动起始位置X
     */
    private float fromPosX;

    /**
     * 移动起始位置Y
     */
    private float fromPosY;

    /**
     * 移动目标位置X
     */
    private float toPosX;

    /**
     * 移动目标位置Y
     */
    private float toPosY;

    /**
     * 移动起始时间
     */
    private long startTime;

    public float getFromPosX() {
        return fromPosX;
    }

    public void setFromPosX(float fromPosX) {
        this.fromPosX = fromPosX;
    }

    public float getFromPosY() {
        return fromPosY;
    }

    public void setFromPosY(float fromPosY) {
        this.fromPosY = fromPosY;
    }

    public float getToPosX() {
        return toPosX;
    }

    public void setToPosX(float toPosX) {
        this.toPosX = toPosX;
    }

    public float getToPosY() {
        return toPosY;
    }

    public void setToPosY(float toPosY) {
        this.toPosY = toPosY;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "MoveState{" +
                "fromPosX=" + fromPosX +
                ", fromPosY=" + fromPosY +
                ", toPosX=" + toPosX +
                ", toPosY=" + toPosY +
                ", startTime=" + startTime +
                '}';
    }
}
