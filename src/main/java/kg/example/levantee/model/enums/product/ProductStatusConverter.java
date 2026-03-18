package kg.example.levantee.model.enums.product;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProductStatusConverter implements AttributeConverter<ProductStatus, Short> {

    @Override
    public Short convertToDatabaseColumn(ProductStatus status) {
        if (status == null) return null;
        return status.id;
    }

    @Override
    public ProductStatus convertToEntityAttribute(Short dbValue) {
        if (dbValue == null) return null;
        return ProductStatus.fromId(dbValue);
    }
}
