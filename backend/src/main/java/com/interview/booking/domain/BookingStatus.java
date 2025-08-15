package com.interview.booking.domain;

public enum BookingStatus {
    CREATED(true, true, false),
    PENDING(true, true, false),
    CONFIRMED(true, true, false),
    CANCELED(false, false, true),
    COMPLETED(false, false, true);

    private final boolean active;
    private final boolean blocksInventory;
    private final boolean terminal;

    BookingStatus(boolean active, boolean blocksInventory, boolean terminal) {
        this.active = active;
        this.blocksInventory = blocksInventory;
        this.terminal = terminal;
    }

    public boolean isActive() {
        return active;
    }

    public boolean blocksInventory() {
        return blocksInventory;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public static java.util.EnumSet<BookingStatus> activeSet() {
        java.util.EnumSet<BookingStatus> set = java.util.EnumSet.noneOf(BookingStatus.class);
        for (BookingStatus s : values()) if (s.isActive()) set.add(s);
        return set;
    }
}
