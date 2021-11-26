package net.citizensnpcs.nms.v1_18_R1.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_18_R1.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_18_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnderDragonController extends MobEntityController {
    public EnderDragonController() {
        super(EntityEnderDragonNPC.class);
    }

    @Override
    public org.bukkit.entity.EnderDragon getBukkitEntity() {
        return (org.bukkit.entity.EnderDragon) super.getBukkitEntity();
    }

    public static class EnderDragonNPC extends CraftEnderDragon implements ForwardingNPCHolder {
        public EnderDragonNPC(EntityEnderDragonNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityEnderDragonNPC extends EnderDragon implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEnderDragonNPC(EntityType<? extends EnderDragon> types, Level level) {
            this(types, level, null);
        }

        public EntityEnderDragonNPC(EntityType<? extends EnderDragon> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMSImpl.clearGoals(npc, goalSelector, targetSelector);
            }
        }

        @Override
        public void aiStep() {
            if (npc != null) {
                npc.update();
                NMSImpl.updateMinecraftAIState(npc, this);
            }
            if (npc != null && !npc.useMinecraftAI()) {
                Vec3 mot = getDeltaMovement();
                if (mot.x != 0 || mot.y != 0 || mot.z != 0) {
                    mot = mot.multiply(0.98, 0.98, 0.98);
                    setYRot(getCorrectYaw(getX() + mot.x, getZ() + mot.z));
                    setPos(getX() + mot.x, getY() + mot.y, getZ() + mot.z);
                    setDeltaMovement(mot);
                }
            } else {
                super.aiStep();
            }
        }

        @Override
        protected boolean canRide(Entity entity) {
            if (npc != null && (entity instanceof Boat || entity instanceof AbstractMinecart)) {
                return !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            }
            return super.canRide(entity);
        }

        @Override
        public void checkDespawn() {
            if (npc == null) {
                super.checkDespawn();
            }
        }

        @Override
        public void dismountTo(double d0, double d1, double d2) {
            if (npc == null) {
                super.dismountTo(d0, d1, d2);
                return;
            }
            NPCEnderTeleportEvent event = new NPCEnderTeleportEvent(npc);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                super.dismountTo(d0, d1, d2);
            }
        }

        @Override
        protected SoundEvent getAmbientSound() {
            return NMSImpl.getSoundEffect(npc, super.getAmbientSound(), NPC.AMBIENT_SOUND_METADATA);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new EnderDragonNPC(this));
            }
            return super.getBukkitEntity();
        }

        private float getCorrectYaw(double tX, double tZ) {
            if (getZ() > tZ)
                return (float) (-Math.toDegrees(Math.atan((getX() - tX) / (getZ() - tZ))));
            if (getZ() < tZ) {
                return (float) (-Math.toDegrees(Math.atan((getX() - tX) / (getX() - tZ)))) + 180.0F;
            }
            return getYRot();
        }

        @Override
        protected SoundEvent getDeathSound() {
            return NMSImpl.getSoundEffect(npc, super.getDeathSound(), NPC.DEATH_SOUND_METADATA);
        }

        @Override
        protected SoundEvent getHurtSound(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.getHurtSound(damagesource), NPC.HURT_SOUND_METADATA);
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public boolean isLeashed() {
            if (npc == null)
                return super.isLeashed();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.isLeashed();
            if (super.isLeashed()) {
                dropLeash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void push(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.push(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public void push(Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.push(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }
    }
}
