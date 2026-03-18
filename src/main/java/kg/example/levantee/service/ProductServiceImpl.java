package kg.example.levantee.service;

import kg.example.levantee.dto.ProductDto.ProductRequest;
import kg.example.levantee.dto.ProductDto.ProductResponse;
import kg.example.levantee.dto.mapper.ProductMapper;
import kg.example.levantee.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(ProductRequest request) {
        return productMapper.toResponse(productRepository.save(productMapper.toEntity(request)));
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(productMapper::toResponse).toList();
    }
}
