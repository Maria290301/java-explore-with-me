package ru.practicum.category.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
}
