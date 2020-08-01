package com.hanyuone.checkpoint.util;

import com.hanyuone.checkpoint.advancement.WarpDistanceTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Advancements {
    public static WarpDistanceTrigger WARP_DISTANCE;

    public static <T extends ICriterionInstance> ICriterionTrigger<T> register(ICriterionTrigger<T> trigger) {
        Method method = ObfuscationReflectionHelper.findMethod(CriteriaTriggers.class, "register", ICriterionTrigger.class);
        method.setAccessible(true);

        try {
            trigger = (ICriterionTrigger<T>) method.invoke(null, trigger);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            System.out.println("Failed to register trigger " + trigger.getId() + "!");
            e.printStackTrace();
        }

        return trigger;
    }
}
