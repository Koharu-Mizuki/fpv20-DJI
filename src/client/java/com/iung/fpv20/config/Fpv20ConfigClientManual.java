package com.iung.fpv20.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Path;

public class Fpv20ConfigClientManual {
    private static String CONFIG_PATH = "./config/fpv20_client.json";

    private float camera_angle = 35;

    public static class Drone {
        public float mass = 0.5f;
        public float max_force = 16f;
    }

    public Drone drone = new Drone();

    public static enum DroneType {
        DefaultDrone,
        Plane
    }

    public DroneType drone_select = DroneType.DefaultDrone;

    public static class Plane {
        public float c1 = 1.0f;
    }

    public Plane plane = new Plane();

    public static class AngularVelocity_DegSec {
        public float yaw = 300;
        public float pitch = 300;
        public float roll = 300;
    }

    public AngularVelocity_DegSec angular_velocity__deg_sec = new AngularVelocity_DegSec();

    /** N/S 自稳模式参数（速度指令模型，对标 DJI Avata 2） */
    public static class Mode {
        /** 高速档水平速度 m/s（对标真机 Avata 2，R1 切换） */
        public float max_speed_h = 8f;
        /** 低速档水平速度 m/s（舒适/穿越，R1 切换） */
        public float max_speed_h_low = 2.5f;
        /** 垂直最大速度 m/s（居中=定高） */
        public float max_climb = 6f;
        /** 速度趋近目标的加速度上限 m/s²（越大越跟手/刹车越猛） */
        public float accel = 20f;
        /** 视觉最大倾角（度，云台稳定模式下保留） */
        public float max_tilt_deg = 30f;
        /** 姿态回正增益：每 1° 误差产生的纠正角速度 度/秒 */
        public float level_p = 8f;
        /** 偏航角速度 度/秒 */
        public float yaw_rate = 180f;
    }

    public Mode mode_n = new Mode();
    public Mode mode_s = new Mode();

    {
        // S 运动档：高/低速更快（真机 16/4），升降 9，加速度/偏航更猛
        mode_s.max_speed_h = 16f;
        mode_s.max_speed_h_low = 4f;
        mode_s.max_climb = 9f;
        mode_s.accel = 40f;
        mode_s.max_tilt_deg = 45f;
        mode_s.level_p = 10f;
        mode_s.yaw_rate = 240f;
    }

    public boolean free_camera_yaw = false;
    public boolean free_camera_pitch = false;

    public float slow_motion_time_rate = 0.2f;
    public String slow_motion_switch_name = "sm";


    /////////////////////////////////
    public float getCamera_angle() {
        return camera_angle;
    }

    public void setCamera_angle(float camera_angle) {
        this.camera_angle = camera_angle;
    }


    private String to_json() {
        Gson j = new GsonBuilder().setPrettyPrinting().create();

        return j.toJson(this);
    }

    private static Fpv20ConfigClientManual from_json(String json) {
        Gson j = new Gson();

        return j.fromJson(json, Fpv20ConfigClientManual.class);
    }

    public static Fpv20ConfigClientManual createAndLoad() {
        try {
            String content = Files.readString(Path.of(CONFIG_PATH));
            return from_json(content);
        } catch (Exception ignored) {
            return new Fpv20ConfigClientManual();
        }
    }

    public void save() {
        String json = this.to_json();
        try {
            Files.writeString(Path.of(CONFIG_PATH), json);
        } catch (Exception ignored) {

        }
    }
}
