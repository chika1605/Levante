package kg.example.levantee.dto.mapper;

import kg.example.levantee.dto.productDto.ProductProjection;
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

    public ProductResponse toResponse(ProductProjection p) {
        ProductResponse response = new ProductResponse();
        response.setId(p.getId());
        response.setCode(p.getCode());
        response.setName(p.getName());
        response.setDescription(p.getDescription());
        response.setPrice(p.getPrice());
        response.setStock(p.getStock());
        response.setStatus(p.getStatus());
        response.setCreatedAt(p.getCreatedAt());
        response.setUpdatedAt(p.getUpdatedAt());
        return response;
    }
}
