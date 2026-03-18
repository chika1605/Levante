package kg.example.levantee.dto.mapper;

import kg.example.levantee.dto.productDto.ProductRequest;
import kg.example.levantee.dto.productDto.ProductResponse;
import kg.example.levantee.model.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
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
        response.setStock(product.getStock());
        response.setStatus(product.getStatus());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
