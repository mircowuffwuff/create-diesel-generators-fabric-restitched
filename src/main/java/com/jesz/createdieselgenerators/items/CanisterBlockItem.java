package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.armor.CapacityEnchantment;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingBehaviorItem;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.item.FluidHandlerItemStack;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.FluidTextUtil;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CanisterBlockItem extends BlockItem implements CapacityEnchantment.ICapacityEnchantable, CustomEnchantingBehaviorItem, FluidStorageItem {
    public CanisterBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }
    public void appendHoverText(ItemStack stack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        if(stack.getTag() != null) {
            CompoundTag primaryTankCompound;
                primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getCompound("TankContent");

            FluidStack fStack = FluidStack.loadFluidStackFromNBT(primaryTankCompound);
            if(fStack.isEmpty()){
                components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
                this.getBlock().appendHoverText(stack, level, components, tooltipFlag);
                return;
            }
            var v = EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.CAPACITY.get(), stack);
            components.add(CreateLang.fluidName(fStack).component()
                    .withStyle(ChatFormatting.GRAY)
                    .append(" ")
                    .append(CreateLang.text(FluidTextUtil.getUnicodeMillibuckets(fStack.getAmount(), AllConfigs.client().fluidUnitType.get(), AllConfigs.client().simplifyFluidUnit.get()))
                            .style(ChatFormatting.GOLD).component())
                    .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" / "))
                    .append(CreateLang.number(ConfigRegistry.CANISTER_CAPACITY.get() + ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get() * v)
                            .style(ChatFormatting.GRAY).component())
                    .append(Component.translatable("create.generic.unit.millibuckets").withStyle(ChatFormatting.GRAY)));
            this.getBlock().appendHoverText(stack, level, components, tooltipFlag);
            return;
        }
        components.add(Component.translatable("createdieselgenerators.tooltip.empty").withStyle(ChatFormatting.GRAY));
        this.getBlock().appendHoverText(stack, level, components, tooltipFlag);

    }

    @Override
    public InteractionResult useOn(UseOnContext p_40581_) {
        return super.useOn(p_40581_);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }


    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(enchantment == AllEnchantments.CAPACITY.get())
            return true;
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEFEFEF;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if(stack.getTag() != null) {
                CompoundTag primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getCompound("TankContent");

            FluidStack fStack = FluidStack.loadFluidStackFromNBT(primaryTankCompound);
            return !fStack.isEmpty();
        }
        return false;
    }
    @Override
    public int getBarWidth(ItemStack stack) {
        if(stack.getTag() == null)
            return 0;
        CompoundTag primaryTankCompound = stack.getTag().getCompound("BlockEntityTag").getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getCompound("TankContent");
        var v = EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.CAPACITY.get(), stack);
        return Math.round(13 * Mth.clamp(FluidStack.loadFluidStackFromNBT(primaryTankCompound).getAmount()/(float)(ConfigRegistry.CANISTER_CAPACITY.get()+ ConfigRegistry.CANISTER_CAPACITY_ENCHANTMENT.get() * v), 0, 1));
    }


    @Override
    public Storage<FluidVariant> getFluidStorage(ItemStack stack, ContainerItemContext context) {
        return new CanisterFluidHandlerItemStack(context, ConfigRegistry.CANISTER_CAPACITY.get());
    }

    static class CanisterFluidHandlerItemStack extends FluidHandlerItemStack {

        /**
         * @param capacity  The maximum capacity of this fluid tank.
         */
        public CanisterFluidHandlerItemStack(@NotNull ContainerItemContext container, long capacity) {
            super(container, capacity);
        }

        @Override
        public FluidStack getFluid() {
            CompoundTag tagCompound = container.getItemVariant().getNbt();
            if (tagCompound == null || !tagCompound.getCompound("BlockEntityTag").contains("Tanks")) {
                return FluidStack.EMPTY;
            }
            if(tagCompound.getCompound("BlockEntityTag").getList("Tanks", CompoundTag.TAG_COMPOUND).isEmpty())
                return FluidStack.EMPTY;
            return FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("BlockEntityTag").getList("Tanks", CompoundTag.TAG_COMPOUND).getCompound(0).getCompound("TankContent"));
        }

        @Override
        protected boolean setFluid(FluidStack fluid, TransactionContext tx) {
            if (!container.getItemVariant().hasNbt()) {
                container.getItemVariant().copyOrCreateNbt();
            }

            CompoundTag fluidTag = new CompoundTag();
            fluidTag.put("TankContent", new CompoundTag());
            fluid.writeToNBT(fluidTag.getCompound("TankContent"));
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            list.add(fluidTag);
            tag.put("Tanks", list);
            if (container.getItemVariant().getNbt() != null) {
                container.getItemVariant().getNbt().put("BlockEntityTag", tag);
            }

            return true;
        }



        @Override
        protected boolean setContainerToEmpty(TransactionContext tx) {
            if(container.getItemVariant().getNbt() != null) {
                container.getItemVariant().getNbt().getCompound("BlockEntityTag").remove("Tanks");
                return true;

            }

            return false;
        }
    }
}
