package com.haroldstudios.mailme.gui;

public interface Expandable {

    void expand();

    enum GuiType {
        COMPACT(7), EXPANDED(28);

        private int pageSize;

        GuiType(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageSize() {
            return pageSize;
        }
    }
}
