package kg.example.levantee.model.enums.order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    PENDING(1),
    CONFIRMED(2),
    CANCELLED(3);

    @JsonValue
    public final int id;

    OrderStatus(int id) {
        this.id = id;
    }

    @JsonCreator
    public static OrderStatus fromId(int id) {
        for (OrderStatus status : values()) {
            if (status.id == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus id: " + id);
    }
}
