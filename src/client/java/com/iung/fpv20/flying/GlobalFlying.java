package com.iung.fpv20.flying;

import com.iung.fpv20.Fpv20;
import com.iung.fpv20.Fpv20Client;
import com.iung.fpv20.config.Fpv20ConfigClientManual;
import com.iung.fpv20.consts.Texts;
import com.iung.fpv20.input.Controller;
import com.iung.fpv20.mixin_utils.IsFlying;
import com.iung.fpv20.network.DroneFlyPacket;
import com.iung.fpv20.physics.DefaultDrone;
import com.iung.fpv20.physics.Drone;
import com.iung.fpv20.physics.PhysicsCore;
import com.iung.fpv20.physics.Plane;
import com.iung.fpv20.sound.FlyingSound;
import com.iung.fpv20.utils.FastMath;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static com.iung.fpv20.Fpv20Client.config1;
import static com.iung.fpv20.Fpv20Client.in_slow_motion;
import static com.iung.fpv20.utils.LocalMath.DEG_TO_RAD;

public class GlobalFlying {
    public static GlobalFlying G = new GlobalFlying(0);
//    private float lastCamRoll;
//    private float camRoll;

    private Quaternionf lastDroneRotation;
    private Quaternionf droneRotation;

    private float last_update_time;
    /**
     * deg/s
     */
//    private float angular_speed;

    private boolean last_tick_flying;

    private FlightMode flight_mode = FlightMode.NORMAL;
    private boolean last_md = false;
    private boolean last_spd = false;
    private boolean low_speed = false;

    public FlightMode getFlightMode() {
        return this.flight_mode;
    }

    //    private float cam_angel_deg;
    private Drone drone;

    private Vec3d last_pos;

    private long last_tick_nano;
    private long this_tick_nano;
    private Vec3d last_tick_pos;
    private Vec3d this_tick_pos;
    private Vec3d speed;

    private void update_speed_tick(Entity player) {
        last_tick_pos = this_tick_pos;
        this_tick_pos = player.getPos();

        last_tick_nano = this_tick_nano;
        this_tick_nano = System.nanoTime();

        speed = this_tick_pos.subtract(last_tick_pos)
                .multiply((this_tick_nano - last_tick_nano) / 1000_000_000.0);
    }

    private Vec3d get_speed() {
        return this.speed;
    }


    private static float time_now_float() {
        return (float) ((double) System.nanoTime() / 1000_000_000.d);
    }

    public GlobalFlying(int camRoll) {
//        this.lastCamRoll = camRoll;
//        this.camRoll = camRoll;
//        this.angular_speed = angular_speed;
        this.last_update_time = time_now_float();
        this.last_tick_flying = false;
        switch (Fpv20Client.config1.drone_select) {
            case DefaultDrone -> this.drone = new DefaultDrone();
            case Plane -> this.drone = new Plane();
        }
        this.droneRotation = new Quaternionf();
        this.lastDroneRotation = new Quaternionf();
//        this.cam_angel_deg = 30;
        this.last_pos = new Vec3d(0, 0, 0);
        this.last_tick_pos = new Vec3d(0, 0, 0);
        this.last_tick_nano = System.nanoTime();
        this.this_tick_pos = new Vec3d(0, 0, 0);
        this.this_tick_nano = System.nanoTime() + 1;
    }

    public static void setFlying(boolean if_fly) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        IsFlying p = (IsFlying) player;
        if (p != null) {

            if (ClientPlayNetworking.canSend(DroneFlyPacket.TYPE)) {
                ClientPlayNetworking.send(new DroneFlyPacket(if_fly));
            }
            p.set_is_flying(if_fly);
            client.getSoundManager().play(new FlyingSound(player));
        }
    }

    public static boolean getFlying() {
        IsFlying p = (IsFlying) MinecraftClient.getInstance().player;
        if (p != null) {
            return p.get_is_flying();
        } else {
            return false;
        }
    }

    public void update_tick_start(Entity player) {
        this.update_speed_tick(player);
    }

    public Quaternionf cacl_cam_rotation() {
        Quaternionf new_r = new Quaternionf(this.droneRotation);
        new_r.rotateLocalX(-Fpv20Client.config1.getCamera_angle() * DEG_TO_RAD);
        return new_r;
    }

    @Deprecated
    public Quaternionf cacl_cam_rotation_last() {
        Quaternionf new_r = new Quaternionf(this.lastDroneRotation);
        new_r.rotateLocalX(-Fpv20Client.config1.getCamera_angle() * DEG_TO_RAD);
        return new_r;
    }

    public void _handle_flying(MinecraftClient client) {

        float now_time = time_now_float();

        float dt = now_time - this.last_update_time;

        this._handle_flying_inner(client, dt);

        this.last_tick_flying = getFlying();
        this.last_update_time = now_time;
    }

    private void handle_flying(MinecraftClient client, float tick_delta) {

        float now_time = time_now_float();

        float dt = (float) (tick_delta * 0.05);

        this._handle_flying_inner(client, dt);

        this.last_tick_flying = getFlying();
        this.last_update_time = now_time;
    }

//    private void set_cam_roll(float roll) {
//        this.lastCamRoll = this.camRoll;
//        this.camRoll = roll;
//    }

    private void set_drone_rotation(Quaternionf camPos) {
        this.lastDroneRotation = this.droneRotation;
        this.droneRotation = new Quaternionf(camPos);
    }


    private void _handle_flying_inner(MinecraftClient client, float dt) {

//        float now_time = time_now_float();
//
//        float dt = now_time - this.last_update_time;

        Fpv20.LOGGER.debug("handle_flying:dt {}", dt);

        if (!getFlying()) {
            return;
        }

        ClientPlayerEntity p = client.player;
        if (p == null) {
            return;
        }


//        float roll = this.camRoll;

        // start flying, init the drone
        if (this.last_tick_flying != getFlying()) {
            float yaw = p.getYaw();
            float pitch = p.getPitch();
            drone.re_init();
            drone.update_pose(PhysicsCore.from_ypr_deg(yaw, pitch, 0));
        }

        Quaternionf q = drone.get_pose();

        Controller controller = Fpv20Client.controller;
        if (controller == null) {
            return;
        }

        float input_t = controller.get_value_by_name("t");
        float input_y = controller.get_value_by_name("y");
        float input_p = controller.get_value_by_name("p");
        float input_r = controller.get_value_by_name("r");


        PhysicsCore.rotate_from_local_yaw_pitch_roll(q, input_y, input_p, input_r,
                300, 300, 300,
                dt
        );
        drone.update_pose(q);
        this.set_drone_rotation(q);


        // process hit
        Vec3d v = drone.get_speed();
        Vector3f vd = new Vector3f((float) v.x, (float) v.y, (float) v.z);
        Vec3d pos = p.getPos();
//        Vec3d v0 = pos.subtract(last_pos).multiply(1 / dt);
        Vec3d v0 = this.get_speed();
        Fpv20.LOGGER.debug("process hit:v0 {}", v0);
        last_pos = pos;
        float aaa = 0.5f;
        boolean to_set_x = false;
        boolean to_set_y = false;
        boolean to_set_z = false;


        if (Math.abs(v0.x) < 0.0001) {
            Fpv20.LOGGER.debug("process hit:x");
            vd.x = 0;
            to_set_y = true;
            to_set_z = true;
        }
        if (Math.abs(v0.y) < 0.0001) {
            Fpv20.LOGGER.debug("process hit:y");
            vd.y = 0;
            to_set_x = true;
            to_set_z = true;
        }
        if (Math.abs(v0.z) < 0.0001) {
            Fpv20.LOGGER.debug("process hit:z");
            vd.z = 0;
            to_set_y = true;
            to_set_x = true;
        }

        if (to_set_x) {
            float d = dt * aaa;
            if (vd.x < -d) {
                vd.x += d;
            } else if (vd.x > d) {
                vd.x -= d;
            } else {
                vd.x = 0;
            }
        }
        if (to_set_y) {
            float d = dt * aaa;
            if (vd.y < -d) {
                vd.y += d;
            } else if (vd.y > d) {
                vd.y -= d;
            } else {
                vd.y = 0;
            }
        }
        if (to_set_z) {
            float d = dt * aaa;
            if (vd.z < -d) {
                vd.z += d;
            } else if (vd.z > d) {
                vd.z -= d;
            } else {
                vd.z = 0;
            }
        }

        drone.set_speed(new Vec3d(vd));
        // // process hit


        drone.update_physics(input_t, dt);


//        Fpv20.LOGGER.debug("{}", dt);
        Vec3d v1 = drone.get_speed();
        p.setVelocity(v1);

//        Vector3f vd = new Vector3f((float) v.x, (float) v.y, (float) v.z);
//        Vec3d v0 = p.getVelocity();
//        if (Math.abs(v0.x) < 0.0001) {
//            vd.x = 0;
//        }
//        if (Math.abs(v0.y) < 0.0001) {
//            vd.y = 0;
//        }
//        if (Math.abs(v0.z) < 0.0001) {
//            vd.z = 0;
//        }
//        drone.set_speed(new Vec3d(vd));


//        Fpv20.LOGGER.debug("v0 {}", p.getVelocity());
//        p.setVelocity(drone.get_speed());
        Fpv20.LOGGER.debug("after update phy:v1 {}", p.getVelocity());


        Vector3f new_ypr = PhysicsCore.from_quaternion_to_ypr_deg(this.cacl_cam_rotation());
        if (false) {
            p.setYaw(new_ypr.x);
            p.setPitch(new_ypr.y);
        } else {
            p.setYaw(0);
            p.setPitch(0);
        }
//        this.set_cam_roll(new_ypr.z);


        return;
    }

    public void handle_flying_phy(ClientPlayerEntity player, float dt) {
        if (in_slow_motion) {
            dt *= config1.slow_motion_time_rate;
        }
        if (!getFlying()) {
            return;
        }

        ClientPlayerEntity p = player;
        if (p == null) {
            return;
        }


        Controller controller = Fpv20Client.controller;
        if (controller == null) {
            return;
        }

        // N/S 档：速度指令模型 —— 摇杆=目标速度，松杆刹停悬停，油门居中定高
        if (this.flight_mode.isStabilized()) {
            Fpv20ConfigClientManual.Mode mp =
                    (this.flight_mode == FlightMode.NORMAL) ? config1.mode_n : config1.mode_s;

            float in_p = controller.get_value_by_name("p"); // 俯仰杆 → 前后
            float in_r = controller.get_value_by_name("r"); // 横滚杆 → 左右
            float in_t = controller.get_value_by_name("t"); // 油门 -1~1，0=定高

            // 由机体朝向取水平前进/右向单位向量（与 DefaultDrone 同款 conjugate 约定）
            Vector3f fwd = new Vector3f(0, 0, -1).rotate(drone.get_pose().conjugate());
            fwd.y = 0;
            if (fwd.lengthSquared() < 1e-6f) {
                fwd.set(0, 0, -1);
            } else {
                fwd.normalize();
            }
            Vector3f right = new Vector3f(fwd.z, 0, -fwd.x); // fwd 绕 Y 轴 -90°

            // 低速档：水平速度砍半（穿树林用），垂直不变
            float max_speed_h = mp.max_speed_h * (this.low_speed ? 0.5f : 1.0f);

            // 目标速度（世界系）
            Vector3f target = new Vector3f();
            target.add(new Vector3f(fwd).mul(in_p * max_speed_h));
            target.add(new Vector3f(right).mul(in_r * max_speed_h));
            target.y = in_t * mp.max_climb;

            // 以加速度上限平滑趋近目标速度（给出「推杆即走、松杆刹停」的手感）
            Vector3f cur = drone.get_speed().toVector3f();
            Vector3f dv = new Vector3f(target).sub(cur);
            float max_step = mp.accel * dt;
            float dv_len = dv.length();
            if (dv_len > max_step && dv_len > 1e-6f) {
                dv.mul(max_step / dv_len);
            }
            cur.add(dv);

            drone.set_speed(new Vec3d(cur));
            if (in_slow_motion) {
                p.setVelocity(new Vec3d(cur).multiply(config1.slow_motion_time_rate));
            } else {
                p.setVelocity(new Vec3d(cur));
            }
            return;
        }

        float input_t = controller.get_value_by_name("t");

        // process hit
        Vec3d v = drone.get_speed();
        Vector3f vd = new Vector3f((float) v.x, (float) v.y, (float) v.z);
        Vec3d pos = p.getPos();

        Vec3d v0 = this.get_speed();
        Fpv20.LOGGER.debug("process hit:v0 {}", v0);
        last_pos = pos;
        float aaa = 0.5f;
        boolean hit = false;

        final float ZERO = 0.000001f;

        if (Math.abs(v0.x) < ZERO) {
//            hit = true;
            Fpv20.LOGGER.debug("process hit:x");
            vd.x = 0;
        }
        if (Math.abs(v0.y) < ZERO) {
            hit = true;
            Fpv20.LOGGER.debug("process hit:y");
            vd.y = 0;
        }
        if (Math.abs(v0.z) < ZERO) {
//            hit = true;
            Fpv20.LOGGER.debug("process hit:z");
            vd.z = 0;
        }
        if (hit) {
            if (vd.length() > aaa * dt && vd.length() > 0.0000001) {
                Vector3f vd1 = new Vector3f(vd).normalize().mul(-1f * aaa * dt);
                vd.add(vd1);
            } else {
                vd.zero();
            }
        }


        drone.set_speed(new Vec3d(vd));
        // // process hit


        // M 档油门「不回中」：自回中摇杆 -1~1 → 0~1（底部=0 推力，顶部=满），底部起算可飞
        float throttle01 = (input_t + 1f) / 2f;
        drone.update_physics(throttle01, dt);


        Vec3d v1 = drone.get_speed();


        if (in_slow_motion) {
            p.setVelocity(v1.multiply(Fpv20Client.config1.slow_motion_time_rate));
        } else {
            p.setVelocity(v1);
        }


        Fpv20.LOGGER.debug("after update phy:v1 {}", p.getVelocity());


    }

    public void handle_flying_rotate(MinecraftClient client, float dt) {
        if (in_slow_motion) {
            dt *= config1.slow_motion_time_rate;
        }


        if (!getFlying()) {
            this.last_tick_flying = false;

            return;
        }

        ClientPlayerEntity p = client.player;
        if (p == null) {
            return;
        }


        // start flying, init the drone
        if (this.last_tick_flying != getFlying()) {
            float yaw = p.getYaw();
            float pitch = p.getPitch();
            drone.re_init();
            if (Fpv20Client.config1.free_camera_yaw) {
                p.setYaw(180);
            }
            if (Fpv20Client.config1.free_camera_pitch) {
                p.setPitch(0);

            }
            Fpv20.LOGGER.info("start flying");

            drone.update_pose(PhysicsCore.from_ypr_deg(yaw, pitch, 0));
        }
        this.last_tick_flying = getFlying();

        Quaternionf q = drone.get_pose();

        Controller controller = Fpv20Client.controller;
        if (controller == null) {
            return;
        }

        float input_t = controller.get_value_by_name("t");
        float input_y = controller.get_value_by_name("y");
        float input_p = controller.get_value_by_name("p");
        float input_r = controller.get_value_by_name("r");

        // 飞行模式切换：md 按钮上升沿循环 N→S→M
        boolean md = controller.get_value_by_name("md") > 0.5f;
        if (md && !this.last_md) {
            this.flight_mode = this.flight_mode.next();
            p.sendMessage(this.flight_mode.text(), true); // true = action bar 提示
        }
        this.last_md = md;

        // 速度高/低切换：spd 按钮（R1）上升沿翻转，低速=砍半（穿越用）
        boolean spd = controller.get_value_by_name("spd") > 0.5f;
        if (spd && !this.last_spd) {
            this.low_speed = !this.low_speed;
            p.sendMessage(this.low_speed ? Texts.BTN_SPEED_LOW : Texts.BTN_SPEED_HIGH, true);
        }
        this.last_spd = spd;

        float cmd_y = input_y;
        float cmd_p = input_p;
        float cmd_r = input_r;
        float yaw_rate;
        float pitch_rate;
        float roll_rate;

        if (this.flight_mode == FlightMode.MANUAL) {
            // M 档：原版 Acro，摇杆直通角速度，不动
            yaw_rate = Fpv20Client.config1.angular_velocity__deg_sec.yaw;
            pitch_rate = Fpv20Client.config1.angular_velocity__deg_sec.pitch;
            roll_rate = Fpv20Client.config1.angular_velocity__deg_sec.roll;
        } else {
            // N/S 档：云台稳定视角——机身强制保持水平（俯仰/横滚回平），只控偏航
            // 移动完全交给速度指令模型，所以推杆时视角不会歪、松杆即停且视角居中
            Fpv20ConfigClientManual.Mode mp =
                    (this.flight_mode == FlightMode.NORMAL) ? Fpv20Client.config1.mode_n : Fpv20Client.config1.mode_s;
            final float STAB_RATE = 360f; // 回正最大角速度 deg/s（与归一化换算用，会约掉）
            yaw_rate = mp.yaw_rate;
            pitch_rate = STAB_RATE;
            roll_rate = STAB_RATE;

            Vector3f ypr = PhysicsCore.from_quaternion_to_ypr_deg(q);
            cmd_p = FastMath.clamp((0f - ypr.y) * mp.level_p / STAB_RATE, -1f, 1f);
            cmd_r = FastMath.clamp((0f - ypr.z) * mp.level_p / STAB_RATE, -1f, 1f);
            cmd_y = input_y; // 偏航保持率控制
        }

        PhysicsCore.rotate_from_local_yaw_pitch_roll(q, cmd_y, cmd_p, cmd_r,
                yaw_rate, pitch_rate, roll_rate,
                dt
        );
        drone.update_pose(q);
        this.set_drone_rotation(q);


        Vector3f new_ypr = PhysicsCore.from_quaternion_to_ypr_deg(this.cacl_cam_rotation());


        if (Fpv20.config.in_fabric()) {
            if (!Fpv20Client.config1.free_camera_yaw) {
                p.setYaw(new_ypr.x);
            }
            if (!Fpv20Client.config1.free_camera_pitch) {
                p.setPitch(new_ypr.y);
            }
        } else {
            if (!Fpv20Client.config1.free_camera_yaw) {
                p.setYaw(180);
            }
            if (!Fpv20Client.config1.free_camera_pitch) {
                p.setPitch(0);
            }
//            p.setYaw(180);
//            p.setPitch(0);
        }


    }
}
