package com.breakinblocks.nutritional.userpack;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.config.NutritionalConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.Optional;

@EventBusSubscriber(modid = Nutritional.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class UserPackSource {

    private UserPackSource() {}

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        if (!NutritionalConfig.SERVER.userpackEnabled.get()) return;

        UserPackBootstrap.ensureDirectoryStructure();

        Path packPath = UserPackPaths.root();
        PackLocationInfo location = new PackLocationInfo(
                UserPackPaths.PACK_ID,
                Component.literal("Nutritional Config"),
                PackSource.BUILT_IN,
                Optional.empty()
        );
        Pack.ResourcesSupplier supplier = new PathPackResources.PathResourcesSupplier(packPath);
        Pack pack = Pack.readMetaAndCreate(location, supplier, PackType.SERVER_DATA,
                new PackSelectionConfig(true, Pack.Position.TOP, false));

        if (pack == null) {
            Nutritional.LOGGER.warn("Nutritional config pack at {} could not be loaded (missing or invalid pack.mcmeta).", packPath);
            return;
        }
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }
}
