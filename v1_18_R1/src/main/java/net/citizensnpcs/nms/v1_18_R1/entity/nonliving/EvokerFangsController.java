package net.citizensnpcs.nms.v1_18_R1.entity.nonliving;

import net.citizensnpcs.nms.v1_18_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_18_R1.util.ForwardingNPCHolder;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEvokerFangs;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_18_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EvokerFangsController extends MobEntityController {
    public EvokerFangsController() {
        super(EntityEvokerFangsNPC.class);
    }

    @Override
    public org.bukkit.entity.EvokerFangs getBukkitEntity() {
        return (org.bukkit.entity.EvokerFangs) super.getBukkitEntity();
    }

    public static class EntityEvokerFangsNPC extends EvokerFangs implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEvokerFangsNPC(EntityType<? extends EvokerFangs> types, Level level) {
            this(types, level, null);
        }

        public EntityEvokerFangsNPC(EntityType<? extends EvokerFangs> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new EvokerFangsNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
            if (npc == null) {
                return super.interactAt(entityhuman, vec3d, enumhand);
            }
            PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(
                    (org.bukkit.entity.Player) entityhuman.getBukkitEntity(), getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            return event.isCancelled() ? InteractionResult.FAIL : InteractionResult.SUCCESS;
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
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean save(CompoundTag save) {
            return npc == null ? super.save(save) : false;
        }

        @Override
        public void tick() {
            super.tick();
            if (npc != null) {
                npc.update();
            }
        }
    }

    public static class EvokerFangsNPC extends CraftEvokerFangs implements ForwardingNPCHolder {
        public EvokerFangsNPC(EntityEvokerFangsNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }
}
