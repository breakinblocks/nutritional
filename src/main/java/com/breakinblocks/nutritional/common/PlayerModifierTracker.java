package com.breakinblocks.nutritional.common;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class PlayerModifierTracker {

    private final Map<UUID, Map<Identifier, Holder<Attribute>>> applied = new WeakHashMap<>();
    private final boolean permanent;

    public PlayerModifierTracker(boolean permanent) {
        this.permanent = permanent;
    }

    public void apply(ServerPlayer player, Map<Identifier, ModifierSpec> desired) {
        UUID pid = player.getUUID();
        Map<Identifier, Holder<Attribute>> current = applied.computeIfAbsent(pid, k -> new HashMap<>());

        Set<Identifier> toRemove = new HashSet<>(current.keySet());
        toRemove.removeAll(desired.keySet());
        for (Identifier id : toRemove) {
            Holder<Attribute> attrHolder = current.remove(id);
            AttributeInstance inst = player.getAttribute(attrHolder);
            if (inst != null) inst.removeModifier(id);
        }

        for (Map.Entry<Identifier, ModifierSpec> entry : desired.entrySet()) {
            Identifier id = entry.getKey();
            ModifierSpec spec = entry.getValue();
            AttributeInstance inst = player.getAttribute(spec.attribute);
            if (inst == null) continue;
            AttributeModifier existing = inst.getModifier(id);
            if (existing != null && existing.amount() == spec.amount && existing.operation() == spec.operation) {
                current.put(id, spec.attribute);
                continue;
            }
            if (existing != null) inst.removeModifier(id);
            AttributeModifier next = new AttributeModifier(id, spec.amount, spec.operation);
            if (permanent) inst.addPermanentModifier(next);
            else inst.addTransientModifier(next);
            current.put(id, spec.attribute);
        }
    }

    public void clear(ServerPlayer player) {
        UUID pid = player.getUUID();
        Map<Identifier, Holder<Attribute>> current = applied.remove(pid);
        if (current == null) return;
        for (Map.Entry<Identifier, Holder<Attribute>> entry : current.entrySet()) {
            AttributeInstance inst = player.getAttribute(entry.getValue());
            if (inst != null) inst.removeModifier(entry.getKey());
        }
    }

    public record ModifierSpec(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {}
}
