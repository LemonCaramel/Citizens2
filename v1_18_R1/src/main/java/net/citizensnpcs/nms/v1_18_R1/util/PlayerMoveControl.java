package net.citizensnpcs.nms.v1_18_R1.util;

import java.util.Random;

import net.citizensnpcs.nms.v1_18_R1.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Slime;

public class PlayerMoveControl extends MoveControl {
    protected LivingEntity entity;
    private int jumpTicks;
    private int keepMovingTicks;
    protected boolean moving;
    protected double speed;
    protected double tx;
    protected double ty;
    protected double tz;

    public PlayerMoveControl(LivingEntity entityinsentient) {
        super(entityinsentient instanceof Mob ? (Mob) entityinsentient
                : new Slime(EntityType.SLIME, entityinsentient.level));
        this.entity = entityinsentient;
        this.tx = entityinsentient.getX();
        this.ty = entityinsentient.getY();
        this.tz = entityinsentient.getZ();
    }

    @Override
    public double getSpeedModifier() {
        return this.speed;
    }

    @Override
    public double getWantedX() {
        return this.tx;
    }

    @Override
    public double getWantedY() {
        return this.ty;
    }

    @Override
    public double getWantedZ() {
        return this.tz;
    }

    @Override
    public boolean hasWanted() {
        return this.moving;
    }

    protected int jumpTicks() {
        return new Random().nextInt(20) + 10;
    }

    @Override
    protected float rotlerp(float f, float f1, float f2) {
        float f3 = Mth.wrapDegrees(f1 - f);

        if (f3 > f2) {
            f3 = f2;
        }

        if (f3 < -f2) {
            f3 = -f2;
        }

        float f4 = f + f3;

        if (f4 < 0.0F)
            f4 += 360.0F;
        else if (f4 > 360.0F) {
            f4 -= 360.0F;
        }

        return f4;
    }

    @Override
    public void setWantedPosition(double d0, double d1, double d2, double d3) {
        this.tx = d0;
        this.ty = d1;
        this.tz = d2;
        this.speed = d3;
        this.moving = true;
        this.keepMovingTicks = 1;
    }

    private boolean shouldJump() {
        if (!(this.entity instanceof Slime)) {
            return false;
        }
        if (this.jumpTicks-- <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        this.entity.zza = 0;
        if (this.moving) {
            this.moving = false;
            double dX = this.tx - this.entity.getX();
            double dZ = this.tz - this.entity.getZ();
            double dY = this.ty - this.entity.getY();
            double dXZ = dX * dX + dZ * dZ;
            if (dY * dY < 1.0 && dXZ < 0.005) {
                this.entity.zza = 0.0F;
                return;
            }
            float f = (float) (Mth.atan2(dZ, dX) * 57.2957763671875D) - 90.0F;
            this.entity.setYRot(rotlerp(this.entity.getYRot(), f, 90.0F));
            NMS.setHeadYaw(entity.getBukkitEntity(), this.entity.getYRot());
            AttributeInstance speed = this.entity.getAttribute(Attributes.MOVEMENT_SPEED);
            speed.setBaseValue(0.3D * this.speed);
            float movement = (float) (this.speed * speed.getValue());
            this.entity.setSpeed(movement);
            this.entity.zza = movement;
            if (shouldJump() || (dY >= NMS.getStepHeight(entity.getBukkitEntity()) && dXZ < 1.0D)) {
                this.jumpTicks = jumpTicks();
                this.jumpTicks /= 3;
                if (this.entity instanceof EntityHumanNPC) {
                    ((EntityHumanNPC) this.entity).getControllerJump().jump();
                } else {
                    ((Mob) this.entity).getJumpControl().jump();
                }
            }
        }
    }
}