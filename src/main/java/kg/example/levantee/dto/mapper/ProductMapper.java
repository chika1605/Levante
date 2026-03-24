package kg.example.levantee.dto.mapper;

import kg.example.levantee.model.entity.product.ProductProjection;
import kg.example.levantee.dto.productDto.ProductRequest;
import kg.example.levantee.dto.productDto.ProductResponse;
import kg.example.levantee.model.entity.product.Product;
import kg.example.levantee.model.enums.product.ProductStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .stock(request.getStock())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setCode(product.getCode());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setWeight(product.getWeight());
        response.setLength(product.getLength());
        response.setWidth(product.getWidth());
        response.setHeight(product.getHeight());
        response.setStock(product.getStock());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    public ProductResponse toResponse(ProductProjection p) {
        ProductResponse response = new ProductResponse();
        response.setId(p.getId());
        response.setCode(p.getCode());
        response.setName(p.getName());
        response.setDescription(p.getDescription());
        response.setPrice(p.getPrice());
        response.setWeight(p.getWeight());
        response.setLength(p.getLength());
        response.setWidth(p.getWidth());
        response.setHeight(p.getHeight());
        response.setStock(p.getStock());
        response.setStatus(ProductStatus.fromId(p.getStatus()));
        response.setCreatedAt(p.getCreatedAt());
        response.setUpdatedAt(p.getUpdatedAt());
        return response;
    }
}
