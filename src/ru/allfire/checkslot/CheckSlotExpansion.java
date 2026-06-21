package ru.allfire.checkslot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Multimap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class CheckSlotExpansion extends PlaceholderExpansion {
    
    @Override
    public String getAuthor() {
        return "AllFiRE";
    }
    
    @Override
    public String getIdentifier() {
        return "checkslot";
    }
    
    @Override
    public String getVersion() {
        return "1.6.0";
    }
    
    @Override
    public List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();
        
        // Без ника
        placeholders.add("%checkslot_name_<slot>_<fallback>%");
        placeholders.add("%checkslot_rawname_<slot>_<fallback>%");
        placeholders.add("%checkslot_displayname_<slot>_<fallback>%");
        placeholders.add("%checkslot_rawdisplayname_<slot>_<fallback>%");
        placeholders.add("%checkslot_data_<slot>_<fallback>%");
        placeholders.add("%checkslot_lore_<slot>_<fallback>%");
        placeholders.add("%checkslot_lore_1,2-3\n4_<slot>_<fallback>%");
        placeholders.add("%checkslot_lore-1_<slot>_<fallback>%");
        placeholders.add("%checkslot_lore-2_<slot>_<fallback>%");
        placeholders.add("%checkslot_enchants_<slot>_<fallback>%");
        placeholders.add("%checkslot_rawenchants_<slot>_<fallback>%");
        placeholders.add("%checkslot_displayenchants_<slot>_<fallback>%");
        placeholders.add("%checkslot_rawdisplayenchants_<slot>_<fallback>%");
        placeholders.add("%checkslot_potion_<slot>_<fallback>%");
        placeholders.add("%checkslot_rawpotion_<slot>_<fallback>%");
        placeholders.add("%checkslot_displaypotion_<slot>_<fallback>%");
        placeholders.add("%checkslot_rawdisplaypotion_<slot>_<fallback>%");
        placeholders.add("%checkslot_attribute_<slot>_<fallback>%");
        placeholders.add("%checkslot_rawattribute_<slot>_<fallback>%");
        placeholders.add("%checkslot_durability_<slot>_<fallback>%");
        placeholders.add("%checkslot_maxdurability_<slot>_<fallback>%");
        
        // С ником
        placeholders.add("%checkslot_<player>_<type>_<slot>_<fallback>%");
        
        return placeholders;
    }
    
    @Override
    public String onRequest(OfflinePlayer p, String params) {
        if (p == null || !p.isOnline()) {
            return "";
        }
        
        Player viewer = p.getPlayer();
        if (viewer == null) return "";
        
        // Определяем, есть ли ник игрока в начале
        String firstPart = params.contains("_") ? params.substring(0, params.indexOf('_')) : params;
        boolean hasPlayerArg = false;
        String playerArg = null;
        String typeSlotFallback;
        
        List<String> knownTypes = List.of("name", "rawname", "displayname", "rawdisplayname",
            "data", "lore", "enchants", "rawenchants", "displayenchants", "rawdisplayenchants",
            "potion", "rawpotion", "displaypotion", "rawdisplaypotion",
            "attribute", "rawattribute", "durability", "maxdurability");
        
        boolean firstIsType = knownTypes.contains(firstPart) || firstPart.startsWith("lore-") || firstPart.startsWith("lore");
        
        if (!firstIsType) {
            hasPlayerArg = true;
            if (firstPart.startsWith("{")) {
                int closeBracket = findClosingBracket(params, 0);
                if (closeBracket == -1) return "";
                
                String placeholderInBrackets = params.substring(1, closeBracket);
                playerArg = "%" + placeholderInBrackets + "%";
                typeSlotFallback = params.substring(closeBracket + 2);
            } else {
                int firstUnderscore = params.indexOf('_');
                playerArg = firstPart;
                typeSlotFallback = params.substring(firstUnderscore + 1);
            }
        } else {
            typeSlotFallback = params;
        }
        
        // Определяем тип
        String type;
        String loreParams = null;
        String slotAndFallback;
        
        if (typeSlotFallback.startsWith("lore-")) {
            int firstUnderscore = typeSlotFallback.indexOf('_');
            if (firstUnderscore == -1) return "";
            type = typeSlotFallback.substring(0, firstUnderscore);
            slotAndFallback = typeSlotFallback.substring(firstUnderscore + 1);
        } else if (typeSlotFallback.startsWith("lore_")) {
            String afterLore = typeSlotFallback.substring(5);
            
            if (afterLore.matches("^[\\d,\\-|\\n\\\\]+_.*")) {
                int secondUnderscore = afterLore.indexOf('_');
                loreParams = afterLore.substring(0, secondUnderscore);
                slotAndFallback = afterLore.substring(secondUnderscore + 1);
                type = "lore_custom";
            } else {
                type = "lore";
                slotAndFallback = afterLore;
            }
        } else {
            int firstUnderscore = typeSlotFallback.indexOf('_');
            if (firstUnderscore == -1) return "";
            type = typeSlotFallback.substring(0, firstUnderscore);
            slotAndFallback = typeSlotFallback.substring(firstUnderscore + 1);
        }
        
        // Парсим slot_fallback
        String[] parts = slotAndFallback.split("_", 2);
        if (parts.length < 1) return "";
        
        String slotStr = parts[0];
        String fallback = parts.length > 1 ? parts[1].replace('&', '§') : "";
        
        // Определяем целевого игрока
        Player target;
        if (hasPlayerArg) {
            target = resolvePlayer(viewer, playerArg);
        } else {
            target = viewer;
        }
        
        if (target == null || !target.isOnline()) {
            return parseInlinePlaceholders(viewer, fallback);
        }
        
        fallback = parseInlinePlaceholders(target, fallback);
        
        ItemStack item = getItemInSlot(target, slotStr);
        if (item == null || item.getType() == Material.AIR) {
            return fallback;
        }
        
        try {
            if (type.startsWith("lore-")) {
                return getLoreLine(item, type, fallback);
            }
            
            switch (type) {
                case "name":
                    return getItemName(item, fallback);
                case "rawname":
                    return getRawItemName(item, fallback);
                case "displayname":
                    return getDisplayName(item, fallback);
                case "rawdisplayname":
                    return getRawDisplayName(item, fallback);
                case "data":
                    return getItemData(item, fallback);
                case "lore":
                    return getItemLore(item, fallback);
                case "lore_custom":
                    return getCustomLore(item, loreParams, fallback);
                case "enchants":
                    return getEnchantments(item, false, false, fallback);
                case "rawenchants":
                    return getEnchantments(item, true, false, fallback);
                case "displayenchants":
                    return getEnchantments(item, false, true, fallback);
                case "rawdisplayenchants":
                    return getEnchantments(item, true, true, fallback);
                case "potion":
                    return getPotionEffects(item, false, false, fallback);
                case "rawpotion":
                    return getPotionEffects(item, true, false, fallback);
                case "displaypotion":
                    return getPotionEffects(item, false, true, fallback);
                case "rawdisplaypotion":
                    return getPotionEffects(item, true, true, fallback);
                case "attribute":
                    return getAttributes(item, false, fallback);
                case "rawattribute":
                    return getAttributes(item, true, fallback);
                case "durability":
                    return getDurability(item, false, fallback);
                case "maxdurability":
                    return getDurability(item, true, fallback);
                default:
                    return fallback;
            }
        } catch (Exception e) {
            return fallback;
        }
    }
    
    private int findClosingBracket(String str, int openPos) {
        int depth = 1;
        for (int i = openPos + 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }
    
    private Player resolvePlayer(Player viewer, String playerArg) {
        if (playerArg.contains("%")) {
            String parsed = PlaceholderAPI.setPlaceholders(viewer, playerArg);
            if (parsed == null || parsed.isEmpty()) return null;
            return Bukkit.getPlayer(parsed);
        }
        
        return Bukkit.getPlayer(playerArg);
    }
    
    private String parseInlinePlaceholders(Player player, String text) {
        if (text == null || text.isEmpty()) return text;
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        
        while (i < text.length()) {
            char c = text.charAt(i);
            
            if (c == '{') {
                int closeBracket = text.indexOf('}', i);
                if (closeBracket != -1) {
                    String placeholder = "%" + text.substring(i + 1, closeBracket) + "%";
                    String parsed = PlaceholderAPI.setPlaceholders(player, placeholder);
                    result.append(parsed);
                    i = closeBracket + 1;
                    continue;
                }
            }
            
            result.append(c);
            i++;
        }
        
        return result.toString();
    }
    
    private ItemStack getItemInSlot(Player player, String slot) {
        return switch (slot.toLowerCase()) {
            case "helmet" -> player.getInventory().getHelmet();
            case "chestplate" -> player.getInventory().getChestplate();
            case "leggings" -> player.getInventory().getLeggings();
            case "boots" -> player.getInventory().getBoots();
            case "offhand" -> player.getInventory().getItemInOffHand();
            default -> {
                try {
                    int slotNum = Integer.parseInt(slot);
                    if (slotNum < 0 || slotNum > 40) {
                        yield null;
                    }
                    yield player.getInventory().getItem(slotNum);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }
    
    private String getItemName(ItemStack item, String fallback) {
        return formatMaterialName(item.getType());
    }
    
    private String getRawItemName(ItemStack item, String fallback) {
        return item.getType().name().toLowerCase();
    }
    
    private String getDisplayName(ItemStack item, String fallback) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        try {
            Object i18nName = item.getClass().getMethod("getI18NDisplayName").invoke(item);
            if (i18nName instanceof String s) {
                return s;
            }
            Component component = (Component) i18nName;
            return LegacyComponentSerializer.legacySection().serialize(component);
        } catch (Exception e) {
            return formatMaterialName(item.getType());
        }
    }
    
    private String getRawDisplayName(ItemStack item, String fallback) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return stripColor(meta.getDisplayName());
        }
        try {
            Object i18nName = item.getClass().getMethod("getI18NDisplayName").invoke(item);
            String name;
            if (i18nName instanceof String s) {
                name = s;
            } else {
                Component component = (Component) i18nName;
                name = LegacyComponentSerializer.legacySection().serialize(component);
            }
            return stripColor(name);
        } catch (Exception e) {
            return formatMaterialName(item.getType()).toLowerCase();
        }
    }
    
    private String getItemData(ItemStack item, String fallback) {
        StringBuilder data = new StringBuilder();
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            data.append("Type: ").append(item.getType().name());
            data.append(", Amount: ").append(item.getAmount());
            
            if (meta.hasCustomModelData()) {
                data.append(", CustomModelData: ").append(meta.getCustomModelData());
            }
            
            if (meta.isUnbreakable()) {
                data.append(", Unbreakable");
            }
            
            if (meta.hasEnchants()) {
                data.append(", Enchanted");
            }
        }
        
        return !data.isEmpty() ? data.toString() : fallback;
    }
    
    private String getItemLore(ItemStack item, String fallback) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            return String.join("\n", lore);
        }
        return fallback;
    }
    
    private String getLoreLine(ItemStack item, String type, String fallback) {
        try {
            int lineNum = Integer.parseInt(type.substring(5)) - 1;
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lineNum >= 0 && lineNum < lore.size()) {
                    return lore.get(lineNum);
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
        }
        return fallback;
    }
    
    private String getCustomLore(ItemStack item, String loreParams, String fallback) {
        if (loreParams == null || loreParams.isEmpty()) return fallback;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return fallback;
        
        List<String> lore = meta.getLore();
        if (lore.isEmpty()) return fallback;
        
        String delimiter = detectDelimiter(loreParams);
        List<Integer> lineNumbers = parseLineNumbers(loreParams, delimiter);
        if (lineNumbers.isEmpty()) return fallback;
        
        List<String> selectedLines = new ArrayList<>();
        for (int num : lineNumbers) {
            if (num >= 1 && num <= lore.size()) {
                selectedLines.add(lore.get(num - 1));
            }
        }
        
        if (selectedLines.isEmpty()) return fallback;
        
        if ("\n".equals(delimiter)) {
            return String.join("\n", selectedLines);
        }
        
        return String.join(delimiter, selectedLines);
    }
    
    private String detectDelimiter(String params) {
        if (params.contains("\n")) return "\n";
        
        for (char c : params.toCharArray()) {
            if (!Character.isDigit(c) && c != '-') {
                return String.valueOf(c);
            }
        }
        
        return ",";
    }
    
    private List<Integer> parseLineNumbers(String params, String delimiter) {
        List<Integer> numbers = new ArrayList<>();
        
        String[] tokens;
        if ("\n".equals(delimiter)) {
            tokens = params.split("\n");
        } else {
            tokens = params.split(Pattern.quote(delimiter));
        }
        
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;
            
            if (token.contains("-")) {
                String[] range = token.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0].trim());
                        int end = Integer.parseInt(range[1].trim());
                        for (int i = Math.min(start, end); i <= Math.max(start, end); i++) {
                            if (!numbers.contains(i)) {
                                numbers.add(i);
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                try {
                    int num = Integer.parseInt(token);
                    if (!numbers.contains(num)) {
                        numbers.add(num);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        return numbers;
    }
    
    private String getEnchantments(ItemStack item, boolean raw, boolean i18n, String fallback) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasEnchants()) {
            Map<Enchantment, Integer> enchants = meta.getEnchants();
            if (enchants.isEmpty()) {
                return fallback;
            }
            
            List<String> enchantList = new ArrayList<>();
            enchants.forEach((enchant, level) -> {
                String enchantName;
                if (i18n) {
                    enchantName = getI18nEnchantmentName(enchant);
                    if (raw) enchantName = stripColor(enchantName);
                } else {
                    enchantName = raw ? enchant.getKey().getKey().toLowerCase() : 
                        formatEnchantmentName(enchant);
                }
                String levelStr = raw ? String.valueOf(level) : " " + toRoman(level);
                enchantList.add(enchantName + levelStr);
            });
            
            return raw ? String.join(",", enchantList) : String.join(", ", enchantList);
        }
        return fallback;
    }
    
    private String getPotionEffects(ItemStack item, boolean raw, boolean i18n, String fallback) {
        if (item.getItemMeta() instanceof PotionMeta potionMeta) {
            List<PotionEffect> effects = potionMeta.getCustomEffects();
            if (effects.isEmpty()) {
                return fallback;
            }
            
            List<String> effectList = new ArrayList<>();
            for (PotionEffect effect : effects) {
                String effectName;
                if (i18n) {
                    effectName = getI18nPotionEffectName(effect.getType());
                    if (raw) effectName = stripColor(effectName);
                } else {
                    effectName = raw ? effect.getType().getKey().getKey().toLowerCase() :
                        formatPotionEffectName(effect.getType());
                }
                int amplifier = effect.getAmplifier() + 1;
                String levelStr = raw ? String.valueOf(amplifier) : " " + toRoman(amplifier);
                effectList.add(effectName + levelStr);
            }
            
            return raw ? String.join(",", effectList) : String.join(", ", effectList);
        }
        return fallback;
    }
    
    private String getI18nEnchantmentName(Enchantment enchant) {
        try {
            Object displayName = enchant.getClass().getMethod("displayName", int.class).invoke(enchant, 1);
            if (displayName instanceof String s) {
                return s;
            }
            Component component = (Component) displayName;
            return LegacyComponentSerializer.legacySection().serialize(component);
        } catch (Exception e) {
            return formatEnchantmentName(enchant);
        }
    }
    
    private String getI18nPotionEffectName(PotionEffectType type) {
        try {
            Object displayName = type.getClass().getMethod("displayName").invoke(type);
            if (displayName instanceof String s) {
                return s;
            }
            Component component = (Component) displayName;
            return LegacyComponentSerializer.legacySection().serialize(component);
        } catch (Exception e) {
            return formatPotionEffectName(type);
        }
    }
    
    private String getAttributes(ItemStack item, boolean raw, String fallback) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasAttributeModifiers()) {
            Multimap<Attribute, AttributeModifier> attributes = meta.getAttributeModifiers();
            if (attributes != null && !attributes.isEmpty()) {
                List<String> attrList = new ArrayList<>();
                
                attributes.forEach((attribute, modifier) -> {
                    String attrName = raw ? attribute.getKey().getKey().toLowerCase() :
                        formatAttributeName(attribute);
                    double amount = modifier.getAmount();
                    String operation = formatOperation(modifier.getOperation(), raw);
                    
                    if (raw) {
                        attrList.add(attrName + ":" + amount + ":" + operation);
                    } else {
                        attrList.add(attrName + " " + (amount > 0 ? "+" : "") + 
                                   String.format("%.1f", amount) + " " + operation);
                    }
                });
                
                return raw ? String.join(",", attrList) : String.join(", ", attrList);
            }
        }
        return fallback;
    }
    
    private String getDurability(ItemStack item, boolean max, String fallback) {
        if (item.getType().getMaxDurability() > 0) {
            if (max) {
                return String.valueOf(item.getType().getMaxDurability());
            } else {
                return String.valueOf(item.getType().getMaxDurability() - item.getDurability());
            }
        }
        return fallback;
    }
    
    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace('_', ' ');
        StringBuilder formatted = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                       .append(word.substring(1)).append(" ");
            }
        }
        return formatted.toString().trim();
    }
    
    private String formatEnchantmentName(Enchantment enchant) {
        String name = enchant.getKey().getKey();
        name = name.replace('_', ' ');
        StringBuilder formatted = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                       .append(word.substring(1)).append(" ");
            }
        }
        return formatted.toString().trim();
    }
    
    private String formatPotionEffectName(PotionEffectType type) {
        String name = type.getKey().getKey();
        name = name.replace('_', ' ');
        StringBuilder formatted = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                       .append(word.substring(1)).append(" ");
            }
        }
        return formatted.toString().trim();
    }
    
    private String formatAttributeName(Attribute attribute) {
        String name = attribute.getKey().getKey();
        name = name.replace('_', ' ');
        String[] words = name.split("\\.");
        String lastWord = words[words.length - 1];
        
        StringBuilder formatted = new StringBuilder();
        for (String word : lastWord.split(" ")) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                       .append(word.substring(1)).append(" ");
            }
        }
        return formatted.toString().trim();
    }
    
    private String formatOperation(AttributeModifier.Operation op, boolean raw) {
        if (raw) {
            return op.name().toLowerCase();
        }
        return switch (op) {
            case ADD_NUMBER -> "add";
            case ADD_SCALAR -> "multiply_base";
            case MULTIPLY_SCALAR_1 -> "multiply_total";
        };
    }
    
    private String toRoman(int number) {
        if (number <= 0) return String.valueOf(number);
        
        String[] romanNumerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                result.append(romanNumerals[i]);
                number -= values[i];
            }
        }
        return result.toString();
    }
    
    private String stripColor(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "");
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public boolean canRegister() {
        return true;
    }
}