package me.libraryaddict.disguise;

import java.lang.reflect.Field;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.PacketsManager;
import me.libraryaddict.disguise.utilities.ReflectionManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class DisguiseAPI {
    private static boolean hearSelfDisguise;

    private static boolean hidingArmor;
    private static boolean hidingHeldItem;
    private static boolean sendVelocity;
    
    @Deprecated
    public static boolean canHearSelfDisguise() {
        return hearSelfDisguise;
    }

    /**
     * Disguise the next entity to spawn with this disguise. This may not work however if the entity doesn't actually spawn.
     */
    public static void disguiseNextEntity(Disguise disguise) {
        if (disguise == null)
            return;
        if (disguise.getEntity() != null || DisguiseUtilities.getDisguises().containsValue(disguise)) {
            disguise = disguise.clone();
        }
        try {
            Field field = ReflectionManager.getNmsClass("Entity").getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            DisguiseUtilities.getDisguises().put(id, disguise);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Disguise this entity with this disguise
     */
    public static void disguiseToAll(Entity entity, Disguise disguise) {
        // If they are trying to disguise a null entity or use a null disguise
        // Just return.
        if (entity == null || disguise == null)
            return;
        // Fire a disguise event
        DisguiseEvent event = new DisguiseEvent(entity, disguise);
        Bukkit.getPluginManager().callEvent(event);
        // If they cancelled this disguise event. No idea why.
        // Just return.
        if (event.isCancelled())
            return;
        // The event wasn't cancelled.
        // If the disguise entity isn't the same as the one we are disguising
        if (disguise.getEntity() != entity) {
            // If the disguise entity actually exists
            if (disguise.getEntity() != null) {
                // Clone the disguise
                disguise = disguise.clone();
            }
            // Set the disguise's entity
            disguise.setEntity(entity);
        } // If there was a old disguise
        Disguise oldDisguise = getDisguise(entity);
        // Stick the disguise in the disguises bin
        DisguiseUtilities.getDisguises().put(entity.getEntityId(), disguise);
        // Resend the disguised entity's packet
        DisguiseUtilities.refreshTrackers(entity);
        // If he is a player, then self disguise himself
        DisguiseUtilities.setupFakeDisguise(disguise);
        // Discard the disguise
        if (oldDisguise != null)
            oldDisguise.removeDisguise();
    }

    /**
     * Get the disguise of a entity
     */
    public static Disguise getDisguise(Entity disguised) {
        if (disguised == null)
            return null;
        if (DisguiseUtilities.getDisguises().containsKey(disguised.getEntityId()))
            return DisguiseUtilities.getDisguises().get(disguised.getEntityId());
        return null;
    }

    /**
     * Get the ID of a fake disguise for a entityplayer
     */
    public static int getFakeDisguise(int entityId) {
        if (DisguiseUtilities.getSelfDisguisesIds().containsKey(entityId))
            return DisguiseUtilities.getSelfDisguisesIds().get(entityId);
        return -1;
    }

    /**
     * Is this entity disguised
     */
    public static boolean isDisguised(Entity disguised) {
        return getDisguise(disguised) != null;
    }

    /**
     * Is the plugin modifying the inventory packets so that players when self disguised, do not see their armor floating around
     */
    public static boolean isHidingArmorFromSelf() {
        return hidingArmor;
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing self disguise
     */
    public static boolean isHidingHeldItemFromSelf() {
        return hidingHeldItem;
    }

    public static boolean isInventoryListenerEnabled() {
        return PacketsManager.isInventoryListenerEnabled();
    }

    public static boolean isSelfDisguisesSoundsReplaced() {
        return hearSelfDisguise;
    }

    /**
     * Is the sound packets caught and modified
     */
    public static boolean isSoundEnabled() {
        return PacketsManager.isHearDisguisesEnabled();
    }

    /**
     * Is the velocity packets sent
     */
    public static boolean isVelocitySent() {
        return sendVelocity;
    }

    /**
     * The default value if a player views his own disguise
     */
    public static boolean isViewDisguises() {
        return PacketsManager.isViewDisguisesListenerEnabled();
    }

    /**
     * Can players hear their own disguises
     */
    public static void setHearSelfDisguise(boolean replaceSound) {
        if (hearSelfDisguise != replaceSound) {
            hearSelfDisguise = replaceSound;
        }
    }

    /**
     * Set the plugin to hide self disguises armor from theirselves
     */
    public static void setHideArmorFromSelf(boolean hideArmor) {
        if (hidingArmor != hideArmor) {
            hidingArmor = hideArmor;
        }
    }

    /**
     * Does the plugin appear to remove the item they are holding, to prevent a floating sword when they are viewing self disguise
     */
    public static void setHideHeldItemFromSelf(boolean hideHelditem) {
        if (hidingHeldItem != hideHelditem) {
            hidingHeldItem = hideHelditem;
        }
    }

    public static void setInventoryListenerEnabled(boolean inventoryListenerEnabled) {
        if (PacketsManager.isInventoryListenerEnabled() != inventoryListenerEnabled) {
            PacketsManager.setInventoryListenerEnabled(inventoryListenerEnabled);
        }
    }

    /**
     * Set if the disguises play sounds when hurt
     */
    public static void setSoundsEnabled(boolean isSoundsEnabled) {
        PacketsManager.setHearDisguisesListener(isSoundsEnabled);
    }

    /**
     * Disable velocity packets being sent for w/e reason. Maybe you want every ounce of performance you can get?
     */
    public static void setVelocitySent(boolean sendVelocityPackets) {
        sendVelocity = sendVelocityPackets;
    }

    public static void setViewDisguises(boolean seeOwnDisguise) {
        PacketsManager.setViewDisguisesListener(seeOwnDisguise);
    }

    /**
     * Undisguise the entity. This doesn't let you cancel the UndisguiseEvent if the entity is no longer valid. Aka removed from
     * the world.
     */
    public static void undisguiseToAll(Entity entity) {
        Disguise disguise = getDisguise(entity);
        if (disguise == null)
            return;
        UndisguiseEvent event = new UndisguiseEvent(entity, disguise);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        disguise.removeDisguise();
    }

    private DisguiseAPI() {
    }
}