package kg.example.levantee.model.enums.order;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(OrderStatus status) {
        if (status == null) return null;
        return status.id;
    }

    @Override
    public OrderStatus convertToEntityAttribute(Short dbValue) {
        if (dbValue == null) return null;
        return OrderStatus.fromId(dbValue);
    }
}
