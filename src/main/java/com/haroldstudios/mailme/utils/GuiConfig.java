package com.haroldstudios.mailme.utils;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.gui.Expandable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class GuiConfig {

    private final YamlConfiguration guiConfig;

    public GuiConfig(final MailMe plugin) {
        if (!new File(MailMe.getInstance().getDataFolder(), "gui.yml").exists()) {
            plugin.saveResource("gui.yml", false);
        }
        guiConfig = plugin.getLocale().applyMissingValues("gui.yml");

    }

    public GContainer getItemGContainer(String id, int rows) {
        ConfigurationSection section = guiConfig.getConfigurationSection(id);
        if (section == null) {
            MailMe.debug(GuiConfig.class, "Could not find configuration section: " + id + " in gui config.");
            return new GContainer(false,1,1);
        }

        boolean enabled = section.getBoolean("enabled", true);
        int row = section.getInt("row");
        int col = section.getInt("col");

        if (!isValidCol(col) || !isValidRow(row, rows))  {
            MailMe.debug(GuiConfig.class, "Invalid col or row number with id: " + id);
            return new GContainer(false,1,1);
        }

        if (section.contains("expandedRow") && section.contains("expandedCol")) {
            int expandedRow = section.getInt("expandedRow");
            int expandedCol = section.getInt("expandedCol");

            if (!isValidCol(col) || !isValidRow(row, rows))  {
                MailMe.debug(GuiConfig.class, "Invalid expandedcol or expandedrow number with id: " + id);
                return new GContainer(false,1,1);
            }
            return new GContainer(enabled, row, col, expandedRow, expandedCol);
        } else {
            return new GContainer(enabled, row, col);
        }
    }

    public Expandable.GuiType getGuiTypeFor(String path) {
        try {
            String type = guiConfig.getString(path + ".default-setting");
            if (type == null) {
                MailMe.debug(GuiConfig.class, "Default setting was not found!");
                return Expandable.GuiType.COMPACT;
            }
            return Expandable.GuiType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            MailMe.debug(GuiConfig.class, "Default setting for: " + path + " was not a valid gui type. Valid types are COMPACT AND EXPANDED");
        }
        return Expandable.GuiType.COMPACT;
    }

    public GContainer getItemGContainer(String id) {
        return getItemGContainer(id, 6);
    }

    private boolean isValidRow(int row, int maxRow) {
        return row >= 1 && row <= maxRow;
    }

    private boolean isValidCol(int col) {
        return col >= 1 && col <= 9;
    }

    public static class GContainer {
        private final int col;
        private final int row;
        private int expandedCol;
        private int expandedRow;
        private final boolean enabled;

        public GContainer(boolean enabled, int row, int col) {
            this.col = col;
            this.row = row;
            this.enabled = enabled;
        }

        public GContainer(boolean enabled, int compactRow, int compactCol, int expandedRow, int expandedCol) {
            this(enabled, compactRow, compactCol);
            this.expandedCol = expandedCol;
            this.expandedRow = expandedRow;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getCol(Expandable.GuiType type) {
            if (type == Expandable.GuiType.COMPACT) {
                return col;
            } else {
                return expandedCol;
            }
        }

        public int getCol() {
            return col;
        }

        public int getRow(Expandable.GuiType type) {
            if (type == Expandable.GuiType.COMPACT) {
                return row;
            } else {
                return expandedRow;
            }
        }

        public int getRow() {
            return row;
        }
    }

}