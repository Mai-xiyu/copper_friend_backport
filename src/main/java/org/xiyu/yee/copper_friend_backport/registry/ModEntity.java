package org.xiyu.yee.copper_friend_backport.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.http.client.entity.EntityBuilder;
import org.xiyu.yee.copper_friend_backport.CopperFriendBackport;
import org.xiyu.yee.copper_friend_backport.coppergolem.CopperGolem;

import java.util.function.Supplier;

public class ModEntity {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
            ForgeRegistries.ENTITY_TYPES, CopperFriendBackport.MOD_ID);

    public static final RegistryObject<EntityType<CopperGolem>> COPPER_GOLEM = register(
            "copper_golem",()-> EntityType.Builder.of(CopperGolem::new, MobCategory.MISC).sized(0.49F, 0.98F)
                    .clientTrackingRange(10).build("copper_golem")
    );

    public static<T extends Entity> RegistryObject<EntityType<T>> register(String id,Supplier<EntityType<T>> supplier){
        return ENTITIES.register(id, supplier);
    }
}
