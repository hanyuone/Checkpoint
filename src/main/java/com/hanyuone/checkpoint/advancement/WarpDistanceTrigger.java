package com.hanyuone.checkpoint.advancement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class WarpDistanceTrigger implements ICriterionTrigger<WarpDistanceTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("checkpoint", "warp_distance");
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        Listeners customTrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (customTrigger$listeners == null) {
            customTrigger$listeners = new Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, customTrigger$listeners);
        }

        customTrigger$listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        Listeners customTrigger$listeners = this.listeners.get(playerAdvancementsIn);
        
        if (customTrigger$listeners != null) {
            customTrigger$listeners.remove(listener);
            
            if (customTrigger$listeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    public void trigger(ServerPlayerEntity player, int distance) {
        Listeners customTrigger$listeners = this.listeners.get(player.getAdvancements());

        if (customTrigger$listeners != null) {
            customTrigger$listeners.trigger(distance);
        }
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance();
    }

    public static class Instance extends CriterionInstance {
        public Instance() {
            super(WarpDistanceTrigger.ID);
        }

        public boolean test(int distanceWarped) {
            return distanceWarped >= 1000;
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(Listener<Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Listener<Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(int distance) {
            List<Listener<Instance>> list = null;

            for (Listener<Instance> listener : this.listeners) {
                if (listener.getCriterionInstance().test(distance)) {
                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (Listener<Instance> listener : list) {
                    listener.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }
}
