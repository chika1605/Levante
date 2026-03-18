package kg.example.levantee.service;

import kg.example.levantee.dto.productDto.ProductRequest;
import kg.example.levantee.dto.productDto.ProductResponse;
import kg.example.levantee.dto.mapper.ProductMapper;
import kg.example.levantee.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(ProductRequest request) {
        return productMapper.toResponse(productRepository.save(productMapper.toEntity(request)));
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(productMapper::toResponse).toList();
    }
}
