package com.github.saturnvolv.witl;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.stream.Stream;

public interface InventoryStream {
    static Stream<ItemStack> buildStream(Inventory inventory) {
        Stream.Builder<ItemStack> builder = Stream.<ItemStack>builder();
        for (int index = 0; index < inventory.size(); index++) {
            builder.add(inventory.getStack(index));
        }
        return builder.build();
    }

    static Stream<ItemStack> getBundledStacks(ItemStack stack) throws IllegalArgumentException {
        if (!stack.isOf(Items.BUNDLE)) throw new IllegalArgumentException("You must only provide a stack instance of a Bundle");
        if (stack.hasNbt()) {
            NbtList nbtList = stack.getNbt().getList("Items", 10);
            Stream<NbtElement> output = nbtList.stream();
            return output.map(NbtCompound.class::cast).map(ItemStack::fromNbt);
        }
        return Stream.<ItemStack>builder().build();
    }
}
