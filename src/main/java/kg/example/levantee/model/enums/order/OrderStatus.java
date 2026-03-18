package kg.example.levantee.model.enums.order;

public enum OrderStatus {
    PENDING((short) 1),
    PAID((short) 2),
    CANCELLED((short) 3);

    public final short id;

    OrderStatus(short id) {
        this.id = id;
    }

    public static OrderStatus fromId(int id) {
        for (OrderStatus status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus id: " + id);
    }
}
