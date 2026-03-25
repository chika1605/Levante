package kg.example.levantee.model.enums.order;

public enum OrderStatus {
    CART((short) 1),
    PENDING((short) 2),
    PAID((short) 3),
    CANCELLED((short) 4),
    SHIPPED((short) 5),
    COMPLETED((short) 6);

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
