package org.xiyu.yee.copper_friend_backport.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.registry.ModBlocks;

/**
 * JEI插件 - 为铜箱子添加JEI支持
 */
@JeiPlugin
public class CopperFriendJEIPlugin implements IModPlugin {
    
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CopperFriendBackport.MOD_ID, "jei_plugin");
    }
    
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // 可以在这里注册自定义配方类别,目前使用原版类别
    }
    
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 注册铜箱子作为存储方块的催化剂
        // 这样点击铜箱子就能看到相关的合成配方
        
        // 所有铜箱子变体
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.COPPER_CHEST.get()),
            mezz.jei.api.constants.RecipeTypes.CRAFTING
        );
        
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.EXPOSED_COPPER_CHEST.get()),
            mezz.jei.api.constants.RecipeTypes.CRAFTING
        );
        
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.WEATHERED_COPPER_CHEST.get()),
            mezz.jei.api.constants.RecipeTypes.CRAFTING
        );
        
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.OXIDIZED_COPPER_CHEST.get()),
            mezz.jei.api.constants.RecipeTypes.CRAFTING
        );
        
        // 涂蜡版本
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.WAXED_COPPER_CHEST.get()),
            mezz.jei.api.constants.RecipeTypes.CRAFTING
        );
        
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.WAXED_EXPOSED_COPPER_CHEST.get()),
            mezz.jei.api.constants.RecipeTypes.CRAFTING
        );
        
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.WAXED_WEATHERED_COPPER_CHEST.get()),
            mezz.jei.api.constants.RecipeTypes.CRAFTING
        );
        
        registration.addRecipeCatalyst(
            new ItemStack(ModBlocks.WAXED_OXIDIZED_COPPER_CHEST.get()),
            mezz.jei.api.constants.RecipeTypes.CRAFTING
        );
        
        // 添加信息描述
        addChestInfo(registration);
    }
    
    private void addChestInfo(IRecipeCatalystRegistration registration) {
        // 为所有铜箱子添加说明信息
        Component[] copperChestInfo = new Component[] {
            Component.translatable("jei.copper_friend_backport.copper_chest.info.1"),
            Component.translatable("jei.copper_friend_backport.copper_chest.info.2"),
            Component.translatable("jei.copper_friend_backport.copper_chest.info.3")
        };
        
        // 可以通过JEI的信息面板显示额外信息
        // 注意: 这需要在registerItemSubtypes或其他地方添加
    }
}
