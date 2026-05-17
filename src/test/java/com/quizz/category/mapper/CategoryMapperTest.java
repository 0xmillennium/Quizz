package com.quizz.category.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.quizz.category.dto.CategoryOptionResponse;
import com.quizz.category.dto.CategoryResponse;
import com.quizz.category.dto.CategoryUpdateRequest;
import com.quizz.category.entity.Category;
import com.quizz.common.entity.BaseEntity;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CategoryMapperTest {

    private CategoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CategoryMapper();
    }

    @Test
    void toResponseMapsIdNameDescriptionAndActive() {
        Category category = category(1L, "Science", "Questions about science");

        CategoryResponse response = mapper.toResponse(category);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Science");
        assertThat(response.description()).isEqualTo("Questions about science");
        assertThat(response.active()).isTrue();
    }

    @Test
    void toOptionResponseMapsIdAndName() {
        Category category = category(1L, "Science", null);

        CategoryOptionResponse response = mapper.toOptionResponse(category);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Science");
    }

    @Test
    void toResponseListMapsList() {
        List<CategoryResponse> responses = mapper.toResponseList(List.of(
                category(1L, "Science", null),
                category(2L, "History", null)
        ));

        assertThat(responses).extracting(CategoryResponse::name)
                .containsExactly("Science", "History");
    }

    @Test
    void toOptionResponseListMapsList() {
        List<CategoryOptionResponse> responses = mapper.toOptionResponseList(List.of(
                category(1L, "Science", null),
                category(2L, "History", null)
        ));

        assertThat(responses).extracting(CategoryOptionResponse::name)
                .containsExactly("Science", "History");
    }

    @Test
    void toUpdateRequestMapsNameAndDescription() {
        Category category = category(1L, "Science", "Questions about science");

        CategoryUpdateRequest request = mapper.toUpdateRequest(category);

        assertThat(request.getName()).isEqualTo("Science");
        assertThat(request.getDescription()).isEqualTo("Questions about science");
    }

    private Category category(Long id, String name, String description) {
        Category category = Category.create(name, description);
        setId(category, id);
        return category;
    }

    private void setId(Category category, Long id) {
        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(category, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not set category id for test.", exception);
        }
    }
}
