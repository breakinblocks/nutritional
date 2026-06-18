package com.breakinblocks.nutritional.datagen.builtin;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.advancement.RewardEarnedTrigger;
import com.breakinblocks.nutritional.advancement.TierReachedTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.criterion.ConsumeItemTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.function.Consumer;

public final class DefaultAdvancements implements AdvancementSubProvider {

    private static final TagKey<Item> FRUIT_TAG     = nutrientTag("fruit");
    private static final TagKey<Item> GRAIN_TAG     = nutrientTag("grain");
    private static final TagKey<Item> PROTEIN_TAG   = nutrientTag("protein");
    private static final TagKey<Item> VEGETABLE_TAG = nutrientTag("vegetable");
    private static final TagKey<Item> DAIRY_TAG     = nutrientTag("dairy");

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        new ItemStackTemplate(Items.BREAD),
                        Component.translatable("advancement.nutritional.root"),
                        Component.translatable("advancement.nutritional.root.desc"),
                        Identifier.withDefaultNamespace("gui/advancements/backgrounds/stone"),
                        AdvancementType.TASK,
                        false, false, false)
                .addCriterion("eat_anything",
                        ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items, FRUIT_TAG)))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(saver, Nutritional.id("diet/root").toString());

        childTask(items, root, Items.COOKED_BEEF, "advancement.nutritional.eat_protein", PROTEIN_TAG, saver, "diet/eat_protein", false);
        childTask(items, root, Items.CARROT, "advancement.nutritional.eat_vegetable", VEGETABLE_TAG, saver, "diet/eat_vegetable", false);
        childTask(items, root, Items.BREAD, "advancement.nutritional.eat_grain", GRAIN_TAG, saver, "diet/eat_grain", false);
        childTask(items, root, Items.APPLE, "advancement.nutritional.eat_fruit", FRUIT_TAG, saver, "diet/eat_fruit", false);
        childTask(items, root, Items.MILK_BUCKET, "advancement.nutritional.eat_dairy", DAIRY_TAG, saver, "diet/eat_dairy", true);

        Advancement.Builder.advancement()
                .parent(root)
                .display(displaySimple(Items.GOLDEN_APPLE, "advancement.nutritional.varied_diet", AdvancementType.GOAL))
                .addCriterion("eat_protein", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items, PROTEIN_TAG)))
                .addCriterion("eat_vegetable", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items, VEGETABLE_TAG)))
                .addCriterion("eat_grain", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items, GRAIN_TAG)))
                .addCriterion("eat_fruit", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items, FRUIT_TAG)))
                .addCriterion("eat_dairy", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, DAIRY_TAG)))
                .requirements(AdvancementRequirements.Strategy.AND)
                .save(saver, Nutritional.id("diet/varied_diet").toString());

        AdvancementHolder nourished = Advancement.Builder.advancement()
                .parent(root)
                .display(displaySimple(Items.BREAD, "advancement.nutritional.reach_nourished", AdvancementType.GOAL))
                .addCriterion("reach_tier", TierReachedTrigger.tier(Nutritional.id("nourished")))
                .save(saver, Nutritional.id("diet/reach_nourished").toString());

        Advancement.Builder.advancement()
                .parent(nourished)
                .display(displaySimple(Items.ENCHANTED_GOLDEN_APPLE, "advancement.nutritional.reach_gourmand", AdvancementType.CHALLENGE))
                .rewards(AdvancementRewards.Builder.experience(200))
                .addCriterion("reach_tier", TierReachedTrigger.tier(Nutritional.id("gourmand")))
                .save(saver, Nutritional.id("diet/reach_gourmand").toString());

        Advancement.Builder.advancement()
                .parent(nourished)
                .display(displaySimple(Items.GOLDEN_APPLE, "advancement.nutritional.earn_reward", AdvancementType.CHALLENGE))
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("earn_reward", RewardEarnedTrigger.any())
                .save(saver, Nutritional.id("diet/earn_reward").toString());
    }

    private static void childTask(HolderGetter<Item> items, AdvancementHolder parent, Item icon, String key, TagKey<Item> tag,
                                  Consumer<AdvancementHolder> saver, String path, boolean useInventory) {
        Advancement.Builder.advancement()
                .parent(parent)
                .display(displaySimple(icon, key, AdvancementType.TASK))
                .addCriterion("consume", useInventory
                        ? InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, tag))
                        : ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items, tag)))
                .save(saver, Nutritional.id(path).toString());
    }

    private static DisplayInfo displaySimple(Item icon, String key, AdvancementType type) {
        return new DisplayInfo(
                new ItemStackTemplate(icon),
                Component.translatable(key),
                Component.translatable(key + ".desc"),
                Optional.empty(),
                type,
                true, true, false);
    }

    private static TagKey<Item> nutrientTag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Nutritional.MOD_ID, "nutrient/" + name));
    }
}
