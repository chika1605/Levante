package kg.example.levantee.model.enums.product;

public enum ProductStatus {
    ACTIVE((short) 1),
    BLOCKED((short) 2);

    public final short id;

    ProductStatus(short id) {
        this.id = id;
    }

    public static ProductStatus fromId(short id) {
        for (ProductStatus status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStatus id: " + id);
    }
}
