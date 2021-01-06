package com.haroldstudios.mailme.utils;

public enum PermissionConstants {

    NUM_OF_MAILBOXES("mailbox.limit."),
    USE_MAILBOX("mailbox.use"),
    USE_POSTOFFICE("postoffice.use");

    private String perm;

    PermissionConstants(String string) {
        this.perm = string;
    }

    public String getPerm() {
        return perm;
    }
}
