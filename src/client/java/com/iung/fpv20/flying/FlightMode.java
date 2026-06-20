package com.iung.fpv20.flying;

import com.iung.fpv20.consts.Texts;
import net.minecraft.text.Text;

/**
 * 飞行模式（对标 DJI Avata 2 的 N / S / M）：
 * - NORMAL (N) 普通：自稳 + 定高 + 刹车悬停，最稳，松杆稳稳浮住
 * - SPORT  (S) 运动：同样自稳定高刹车，但更快更猛更跟手
 * - MANUAL (M) 手动：全手动率控制可翻滚（= 原版 Acro）
 */
public enum FlightMode {
    NORMAL,
    SPORT,
    MANUAL;

    /** 循环切换到下一个模式 */
    public FlightMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

    /** 是否为自稳类模式（速度指令模型） */
    public boolean isStabilized() {
        return this != MANUAL;
    }

    /** 当前模式对应的可翻译显示文本 */
    public Text text() {
        switch (this) {
            case NORMAL:
                return Texts.BTN_MODE_NORMAL;
            case SPORT:
                return Texts.BTN_MODE_SPORT;
            case MANUAL:
            default:
                return Texts.BTN_MODE_MANUAL;
        }
    }
}
