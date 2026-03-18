package kg.example.levantee.model.enums.order;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(OrderStatus status) {
        if (status == null) return null;
        return status.id;
    }

    @Override
    public OrderStatus convertToEntityAttribute(Integer dbValue) {
        if (dbValue == null) return null;
        return OrderStatus.fromId(dbValue);
    }
}
