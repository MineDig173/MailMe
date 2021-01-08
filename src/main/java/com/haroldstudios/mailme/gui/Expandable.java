package com.haroldstudios.mailme.gui;

public interface Expandable {

    void expand();
    void collapse();

    enum GuiType {
        COMPACT(7), EXPANDED(18);

        private final int pageSize;

        GuiType(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageSize() {
            return pageSize;
        }
    }
}
