class DesertTaskItem {
    public final String itemName;
    public final int quantity;

    public DesertTaskItem(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }
}

public enum DesertTasksEnum {
    LIZARDS("Lizards", DesertTaskItem[] { new DesertTaskItem("Waterskin(4)", 3) }, true),
    CROCODILES("Crocodiles", DesertTaskItem[] { new DesertTaskItem("Waterskin(4)", 3) }, false),
    KALPHITE("Kalphite", null, true);

    private final String taskName;
    private final DesertTaskItem[] requiresItems;
    private final boolean rangeRequiresEntrance;

    DesertTasksEnum(String taskName, DesertTaskItem[] items, boolean rangeRequiresEntrance) {
        this.taskName = taskName;
        this.items = items;
        this.rangeRequiresEntrance = rangeRequiresEntrance;
    }
}