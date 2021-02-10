package com.haroldstudios.mailme.gui;

import com.haroldstudios.mailme.mail.Mail;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;

public class GuiOptions {

    private Expandable.GuiType guiType = Expandable.GuiType.COMPACT;
    private @Nullable Runnable runnable;
    private LinkedList<Mail> mail = null;
    private Integer rows = 6;
    private String name = "Menu";
    private Player player;
    private @Nullable AbstractMailGui previousMenu;
    private boolean readOnly = false;

    public @Nullable GuiOptions setPreviousMenu(final AbstractMailGui previousMenu) {
        this.previousMenu = previousMenu;
        return this;
    }

    public GuiOptions setForWhom(final Player player) {
        this.player = player;
        return this;
    }

    public Player getForWhom() {
        return player;
    }

    public Expandable.GuiType getGuiType() {
        return guiType;
    }

    public Integer getRows() {
        return rows;
    }

    public LinkedList<Mail> getMailList() {
        return mail;
    }

    public @Nullable Runnable getRunnable() {
        return runnable;
    }

    public String getTitle() {
        return name;
    }

    public @Nullable AbstractMailGui getPreviousMenu() {
        return previousMenu;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public GuiOptions withRunnable(final Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public GuiOptions withGuiType(final Expandable.GuiType guiType) {
        this.guiType = guiType;
        return this;
    }

    public GuiOptions withMail(final Mail[] mail) {
        this.mail = new LinkedList<>(Arrays.asList(mail));
        return this;
    }

    public GuiOptions withRows(final int rows) {
        this.rows = rows;
        return this;
    }

    public GuiOptions withTitle(final String title) {
        this.name = title;
        return this;
    }

    public GuiOptions withPreviousMenu(final AbstractMailGui previousGui) {
        this.previousMenu = previousGui;
        return this;
    }

    public GuiOptions withReadOnlyMode(final boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cl) {
            return this;
        }
    }

    @Override
    public String toString() {
        return String.format("GuiOptions.GuiOptions: " +
                        "Runnable present: %s, " +
                        "GuiType: %s, " +
                        "Mail Size: %s, " +
                        "Rows: %s, " +
                        "Title: %s " +
                        "Player: %s " +
                        "Previous Menu?: %s " +
                        "Read Only: %s ",
                runnable != null,
                guiType.toString(),
                mail == null ? null : mail.size(),
                rows,
                name,
                player == null ? null : player.getName(),
                previousMenu != null,
                readOnly);
    }
}