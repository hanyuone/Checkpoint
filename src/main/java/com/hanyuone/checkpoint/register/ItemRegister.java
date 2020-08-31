package com.hanyuone.checkpoint.register;

import com.hanyuone.checkpoint.Checkpoint;
import com.hanyuone.checkpoint.item.CheckpointItem;
import com.hanyuone.checkpoint.item.PairerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegister {
    public static final DeferredRegister<Item> REGISTER = new DeferredRegister<>(ForgeRegistries.ITEMS, Checkpoint.MOD_ID);

    public static final RegistryObject<Item> CHECKPOINT_ITEM = REGISTER.register("checkpoint", CheckpointItem::new);
    public static final RegistryObject<Item> PAIRER_ITEM = REGISTER.register("pairer", PairerItem::new);
}
