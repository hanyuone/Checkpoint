package com.hanyuone.checkpoint.item;

import com.hanyuone.checkpoint.Checkpoint;
import net.minecraft.item.Item;

public class PairerItem extends Item {
    public PairerItem() {
        super(new Item.Properties()
                .group(Checkpoint.TAB)
                .maxStackSize(1)
                .defaultMaxDamage(16));
    }
}
