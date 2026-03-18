package kg.example.levantee.model.enums.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductStatus {
    ACTIVE((short) 1),
    BLOCKED((short) 2);

    @JsonValue
    public final short id;

    ProductStatus(short id) {
        this.id = id;
    }

    @JsonCreator
    public static ProductStatus fromId(short id) {
        for (ProductStatus status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStatus id: " + id);
    }
}
