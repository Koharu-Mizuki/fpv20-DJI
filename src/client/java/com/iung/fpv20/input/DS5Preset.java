package com.iung.fpv20.input;

import com.iung.fpv20.Fpv20Client;
import com.iung.fpv20.utils.Calibration;

/**
 * DualSense（DS5）手柄默认布局预设。
 *
 * 约定：FPV 四通道 t/y/p/r + 解锁开关 sw + 慢动作 sm + 模式切换 md。
 * 轴 / 按钮索引参考 GLFW 在 Windows/Mac 下对 DS5 的标准 HID 映射。
 */
public class DS5Preset {

    /**
     * 将当前 Controller 的通道名称和校准参数设置为 DS5 默认布局。
     */
    public static void apply(Controller controller) {
        if (controller == null) {
            return;
        }

        String[] names = controller.names;
        String[] btn_names = controller.btn_names;
        Calibration[] cal = controller.calibrations;

        // 清空所有名称
        for (int i = 0; i < names.length; i++) {
            names[i] = String.format("CH%d", i);
        }
        for (int i = 0; i < btn_names.length; i++) {
            btn_names[i] = String.format("BTN%d", i);
        }

        // 轴映射（DualSense 在 GLFW 原始轴序：0=左X 1=左Y 2=右X 3=L2 4=R2 5=右Y）
        names[0] = "y";    // 左摇杆 X → Yaw
        names[1] = "t";    // 左摇杆 Y → Throttle（需反转）
        names[2] = "r";    // 右摇杆 X → Roll
        names[3] = "aux1"; // L2 扳机
        names[4] = "aux2"; // R2 扳机
        names[5] = "p";    // 右摇杆 Y → Pitch（需反转）

        // 四个摇杆轴（0/1/2/5）：MaxMidMin，居中=0
        int[] stick_axes = {0, 1, 2, 5};
        for (int i : stick_axes) {
            cal[i].max = 1.0f;
            cal[i].min = -1.0f;
            cal[i].mid = 0.0f;
            cal[i].rate_a = 0.3f;
            cal[i].rate_b = 0.5f;
            cal[i].calibrateMethod = Calibration.CalibrateMethod.MaxMidMin;
            cal[i].reversed = false;
        }

        // 左摇杆 Y（油门）反转：DS5 上推为负值，反转后上推=正油门
        cal[1].reversed = true;
        // 右摇杆 Y（俯仰）反转：上推为负值，反转后上推=正俯仰
        cal[5].reversed = true;

        // 两个扳机轴（3/4 = L2/R2）：MaxMin，未按=-1，按满=+1，映射到 0~1
        int[] trigger_axes = {3, 4};
        for (int i : trigger_axes) {
            cal[i].max = 1.0f;
            cal[i].min = -1.0f;
            cal[i].mid = 0.0f;
            cal[i].rate_a = 0.3f;
            cal[i].rate_b = 0.5f;
            cal[i].calibrateMethod = Calibration.CalibrateMethod.MaxMin;
            cal[i].reversed = false;
        }

        // 按钮映射（DualSense GLFW 按钮序：0=× 1=○ 2=□ 3=△ 4=L1 5=R1 … 13=触控板）
        btn_names[4] = "sw";   // L1 解锁/上锁
        btn_names[5] = "spd";  // R1 速度高/低切换
        btn_names[3] = "md";   // △ 飞行模式切换
        btn_names[13] = "sm";  // 触控板 慢动作

        // 保存到配置（names/btn_names/cal 与 config 共享同一引用，回写后立即生效）
        Fpv20Client.config.stick_channel_names(names);
        Fpv20Client.config.calibrations(cal);
        Fpv20Client.config.button_channel_names(btn_names);
        Fpv20Client.config.save();
    }

    /**
     * 判断给定 GLFW joystick 名称是否为 DualSense 手柄。
     */
    public static boolean isDS5(String name) {
        return name != null
                && (name.contains("DualSense") || name.contains("Sony Interactive"));
    }
}
