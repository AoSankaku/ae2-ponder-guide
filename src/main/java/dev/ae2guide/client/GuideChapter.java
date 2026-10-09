package dev.ae2guide.client;

import net.minecraft.network.chat.Component;

public enum GuideChapter {
    START("start", "Getting started", "Materials, crystals and processors", "ae2:certus_quartz_crystal"),
    NETWORK("network", "ME networks", "Power, channels and wiring", "ae2:controller"),
    STORAGE("storage", "Storage", "Cells, filtering and external inventories", "ae2:drive"),
    AUTOMATION("automation", "Automation", "Transport and request-based crafting", "ae2:pattern_provider"),
    ADVANCED("advanced", "Advanced systems", "Subnetworks, P2P, quantum and spatial storage", "ae2:quantum_ring"),
    TROUBLESHOOTING("troubleshooting", "Troubleshooting", "Diagnose power, channel, storage and crafting problems", "ae2:network_tool");

    private final String id;
    private final String en;
    private final String description;
    private final String icon;
    GuideChapter(String id, String en, String description, String icon) {
        this.id = id; this.en = en; this.description = description; this.icon = icon;
    }
    public String id() { return id; }
    public String en() { return en; }
    public String description() { return description; }
    public String icon() { return icon; }
    public Component label() { return Component.translatable("ae2ponder.chapter." + id); }
}
