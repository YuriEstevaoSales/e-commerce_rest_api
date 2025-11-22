package com.yuri.store.mappers;

import com.yuri.store.dtos.CategoryDto;
import com.yuri.store.dtos.ProductDto;
import com.yuri.store.entities.Category;
import com.yuri.store.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "category", source = "category") 
    ProductDto toDto(Product product);
    
    Product toEntity(ProductDto productDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true) 
    void update(ProductDto productDto, @MappingTarget Product product);
    
    
    default CategoryDto categoryToCategoryDto(Category category) {
        if (category == null) {
            return null;
        }
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}
