package com.breakinblocks.nutritional.datagen.lang;

import com.breakinblocks.nutritional.Nutritional;
import com.breakinblocks.nutritional.attribute.NutritionalAttributes;
import com.breakinblocks.nutritional.config.HudAnchor;
import com.breakinblocks.nutritional.effect.NutritionalMobEffects;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class EnUsLangProvider extends LanguageProvider {

    public EnUsLangProvider(PackOutput output) {
        super(output, Nutritional.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("mod.nutritional.name", "Nutritional");

        add("nutrient.nutritional.fruit", "Fruit");
        add("nutrient.nutritional.grain", "Grain");
        add("nutrient.nutritional.protein", "Protein");
        add("nutrient.nutritional.vegetable", "Vegetable");
        add("nutrient.nutritional.dairy", "Dairy");

        add("tier.nutritional.starving", "Starving");
        add("tier.nutritional.starving.enter", "You feel yourself wasting away.");
        add("tier.nutritional.starving.exit", "The hollow ache subsides.");
        add("tier.nutritional.malnourished", "Malnourished");
        add("tier.nutritional.malnourished.enter", "Your diet has become unbalanced.");
        add("tier.nutritional.malnourished.exit", "Your body is recovering.");
        add("tier.nutritional.surviving", "Surviving");
        add("tier.nutritional.nourished", "Nourished");
        add("tier.nutritional.nourished.enter", "You feel well-fed and strong.");
        add("tier.nutritional.nourished.exit", "The glow of good nutrition fades.");
        add("tier.nutritional.gourmand", "Gourmand");
        add("tier.nutritional.gourmand.enter", "Your palate is satisfied across every group.");
        add("tier.nutritional.gourmand.exit", "Your masterful diet has slipped.");

        add("reward.nutritional.healthy_lifestyle", "Healthy Lifestyle");

        add(NutritionalAttributes.NUTRIENT_ABSORPTION.get().getDescriptionId(), "Nutrient Absorption");
        add(NutritionalAttributes.NUTRIENT_DECAY_RATE.get().getDescriptionId(), "Nutrient Decay Rate");

        add(NutritionalMobEffects.NOURISHED.get().getDescriptionId(), "Nourished");
        add(NutritionalMobEffects.MALNOURISHED.get().getDescriptionId(), "Malnourished");
        add(NutritionalMobEffects.TOUGHNESS.get().getDescriptionId(), "Toughness");

        add("commands.nutritional.set", "%s.%s = %s");
        add("tooltip.nutritional.nutrients", "Nutrients: %s");

        add("screen.nutritional.title", "Nutrition");
        add("screen.nutritional.close", "Close");
        add("screen.nutritional.hud.on", "HUD: On");
        add("screen.nutritional.hud.off", "HUD: Off");
        add("screen.nutritional.hud.move", "Move HUD");
        add("screen.nutritional.hud.place.title", "Position the HUD widget");
        add("screen.nutritional.hud.place.hint.drag", "Drag the widget to move it.");
        add("screen.nutritional.hud.place.hint.keys", "Arrow keys nudge, Shift for 10px, Shift+drag ignores snapping.");
        add("screen.nutritional.hud.place.readout", "%s — offset %s, %s");
        add("screen.nutritional.hud.place.sample", "Nutrition");
        add("screen.nutritional.hud.place.reset", "Reset");
        add("screen.nutritional.hud.place.done", "Done");

        add(HudAnchor.TOP_LEFT.translationKey(), "Top left");
        add(HudAnchor.TOP_CENTER.translationKey(), "Top center");
        add(HudAnchor.TOP_RIGHT.translationKey(), "Top right");
        add(HudAnchor.MIDDLE_LEFT.translationKey(), "Middle left");
        add(HudAnchor.MIDDLE_CENTER.translationKey(), "Middle center");
        add(HudAnchor.MIDDLE_RIGHT.translationKey(), "Middle right");
        add(HudAnchor.BOTTOM_LEFT.translationKey(), "Bottom left");
        add(HudAnchor.BOTTOM_CENTER.translationKey(), "Bottom center");
        add(HudAnchor.BOTTOM_RIGHT.translationKey(), "Bottom right");

        add("key.nutritional.open_nutrition", "Open Nutrition Screen");
        add("key.categories.nutritional", "Nutritional");

        add("jei.nutritional.info.header", "Provides nutrients:");

        add("advancement.nutritional.root", "Nutritional");
        add("advancement.nutritional.root.desc", "Eat balanced meals to thrive.");
        add("advancement.nutritional.eat_protein", "Protein-Packed");
        add("advancement.nutritional.eat_protein.desc", "Eat any protein-rich food.");
        add("advancement.nutritional.eat_vegetable", "Eat Your Veggies");
        add("advancement.nutritional.eat_vegetable.desc", "Eat any vegetable.");
        add("advancement.nutritional.eat_grain", "Daily Bread");
        add("advancement.nutritional.eat_grain.desc", "Eat any grain.");
        add("advancement.nutritional.eat_fruit", "A Fruity Snack");
        add("advancement.nutritional.eat_fruit.desc", "Eat any fruit.");
        add("advancement.nutritional.eat_dairy", "Got Milk?");
        add("advancement.nutritional.eat_dairy.desc", "Drink milk or pick up dairy.");
        add("advancement.nutritional.varied_diet", "Varied Diet");
        add("advancement.nutritional.varied_diet.desc", "Eat something from every nutrient group.");
        add("advancement.nutritional.reach_nourished", "Well Fed");
        add("advancement.nutritional.reach_nourished.desc", "Reach the Nourished tier.");
        add("advancement.nutritional.reach_gourmand", "Gourmand");
        add("advancement.nutritional.reach_gourmand.desc", "Reach the Gourmand tier.");
        add("advancement.nutritional.earn_reward", "Healthy Lifestyle");
        add("advancement.nutritional.earn_reward.desc", "Earn a sustained nutritional reward.");
    }
}
