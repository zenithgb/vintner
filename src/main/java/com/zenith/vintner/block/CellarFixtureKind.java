package com.zenith.vintner.block;

public enum CellarFixtureKind {
    LABELLED_SHELF(8, true, "labelled_cellar_shelf"),
    TASTING_CABINET(8, false, "tasting_cabinet");

    private final int capacity;
    private final boolean singleBatch;
    private final String messagePrefix;

    CellarFixtureKind(
            int capacity,
            boolean singleBatch,
            String messagePrefix
    ) {
        this.capacity = capacity;
        this.singleBatch = singleBatch;
        this.messagePrefix = messagePrefix;
    }

    public int capacity() {
        return capacity;
    }

    public boolean singleBatch() {
        return singleBatch;
    }

    public String messagePrefix() {
        return messagePrefix;
    }
}
