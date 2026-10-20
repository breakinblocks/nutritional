package com.breakinblocks.nutritional.common;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.data.codec.FoodHintDef;
import com.breakinblocks.nutritional.data.codec.NutrientDefinition;
import com.breakinblocks.nutritional.data.registry.NutritionalDatapack;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class FoodMappingAudit {

    private static final FileToIdConverter ITEM_TAG_FILES = FileToIdConverter.json(Registries.tagsDirPath(Registries.ITEM));

    private FoodMappingAudit() {}

    public static List<Item> unmappedFoods(RegistryAccess access) {
        List<Item> unmapped = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (InvertedNutrientIndex.nutrientsFor(item).isEmpty() && NutritionalLogic.isEdible(new ItemStack(item), access)) {
                unmapped.add(item);
            }
        }
        unmapped.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
        return unmapped;
    }

    public static void logUnmappedFoods(List<Item> unmapped) {
        for (Item item : unmapped) {
            Nutritional.LOGGER.warn("Food with no nutrient mapping: {}", BuiltInRegistries.ITEM.getKey(item));
        }
        Nutritional.LOGGER.info("{} foods have no nutrient mapping.", unmapped.size());
    }

    public static void logMissingTagEntries(MinecraftServer server) {
        RegistryAccess access = server.registryAccess();
        ResourceManager resources = server.getResourceManager();
        Set<Identifier> visited = new HashSet<>();
        Set<Identifier> missing = new LinkedHashSet<>();

        for (Holder.Reference<NutrientDefinition> ref : NutritionalDatapack.nutrients(access).listElements().toList()) {
            Identifier nutrientId = ref.key().identifier();
            checkTag(resources, InvertedNutrientIndex.tagFor(nutrientId, ref.value()), "nutrient " + nutrientId, visited, missing);
        }
        for (Holder.Reference<FoodHintDef> ref : NutritionalDatapack.foodHints(access).listElements().toList()) {
            String owner = "food hint " + ref.key().identifier();
            ref.value().match().tag().ifPresent(tag -> checkTag(resources, tag, owner, visited, missing));
        }
        Nutritional.LOGGER.info("{} items listed in nutrient or food hint tags are missing from installed mods.", missing.size());
    }

    private static void checkTag(ResourceManager resources, TagKey<Item> tag, String owner,
                                 Set<Identifier> visited, Set<Identifier> missing) {
        if (BuiltInRegistries.ITEM.get(tag).isEmpty()) {
            Nutritional.LOGGER.warn("Item tag #{} used by {} does not exist or failed to load.", tag.location(), owner);
        }
        checkTagEntries(resources, tag.location(), owner, visited, missing);
    }

    private static void checkTagEntries(ResourceManager resources, Identifier tagId, String owner,
                                        Set<Identifier> visited, Set<Identifier> missing) {
        if (!visited.add(tagId)) return;
        for (TagEntry entry : readTagEntries(resources, tagId)) {
            Identifier id = entry.getId();
            if (entry.isTag()) {
                checkTagEntries(resources, id, owner, visited, missing);
            } else if (ModList.get().isLoaded(id.getNamespace()) && !BuiltInRegistries.ITEM.containsKey(id)) {
                missing.add(id);
                Nutritional.LOGGER.warn("Missing food item: {} is listed in tag #{} ({}), but {} is installed and has no such item.",
                        id, tagId, owner, id.getNamespace());
            }
        }
    }

    private static List<TagEntry> readTagEntries(ResourceManager resources, Identifier tagId) {
        Identifier file = ITEM_TAG_FILES.idToFile(tagId);
        List<TagEntry> entries = new ArrayList<>();
        for (Resource resource : resources.getResourceStack(file)) {
            try (Reader reader = resource.openAsReader()) {
                TagFile tagFile = TagFile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
                if (tagFile.replace()) entries.clear();
                entries.addAll(tagFile.entries());
            } catch (IOException | RuntimeException e) {
                Nutritional.LOGGER.debug("Could not read item tag file {} from {}", file, resource.sourcePackId(), e);
            }
        }
        return entries;
    }
}
