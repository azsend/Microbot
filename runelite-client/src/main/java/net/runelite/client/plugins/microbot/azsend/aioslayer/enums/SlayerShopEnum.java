package net.runelite.client.plugins.microbot.azsend.aioslayer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlayerShopEnum {
    ENCHANTED_GEM("Enchanted gem", 1),
    MIRROR_SHIELD("Mirror shield", 5000),
    LEAF_BLADED_SPEAR("Leaf-bladed spear", 31000),
    BROAD_ARROWS("Broad arrows", 60),
    BAG_OF_SALT("Bag of salt", 10),
    ROCK_HAMMER("Rock hammer", 500),
    FACEMASK("Facemask", 200),
    EARMUFFS("Earmuffs", 200),
    NOSE_PEG("Nose peg", 200),
    SLAYERS_STAFF("Slayer's staff", 21000),
    SPINY_HELMET("Spiny helmet", 650),
    FISHING_EXPLOSIVES("Fishing explosive", 60),
    ICE_COOLER("Ice cooler", 1),
    SLAYER_GLOVES("Slayer gloves", 200),
    UNLIT_BUG_LANTERN("Unlit bug lantern", 130),
    INSULATED_BOOTS("Insulated boots", 200),
    FUNGICIDE_SPRAY("Fungicide spray 10", 300),
    FUNGICIDE("Fungicide", 10),
    WITCHWOOD_ICON("Witchwood icon", 900),
    SLAYER_BELL("Slayer bell", 150),
    BROAD_ARROWHEADS("Broad arrowheads", 55),
    BROAD_ARROWHEAD_PACK("Broad arrowhead pack", 5500),
    UNFINISHED_BROAD_BOLTS("Unfinished broad bolts", 55),
    UNFINISHED_BROAD_BOLT_PACK("Unfinished broad bolt pack", 5500),
    ROCK_THROWNHAMMER("Rock thrownhammer", 200),
    BOOT_OF_STONE("Boot of stone", 200);

    private final String itemName;
    private final int price;
}
