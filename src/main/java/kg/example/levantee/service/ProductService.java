package kg.example.levantee.service;

import kg.example.levantee.dto.productDto.ProductRequest;
import kg.example.levantee.dto.productDto.ProductResponse;
import kg.example.levantee.dto.mapper.ProductMapper;
import kg.example.levantee.repository.ProductRepository;
import kg.example.levantee.utils.exception.AlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByCode(request.getCode())) {
            throw new AlreadyExistsException("Продукт с таким кодом уже существует");
        }
        return productMapper.toResponse(productRepository.save(productMapper.toEntity(request)));
    }

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAllProducts(pageable)
                .map(productMapper::toResponse);
    }
}
