package com.example.rest.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookPayloadDTO {

    @NotBlank
    @Schema(description = "책 제목", example = "Java의 신", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    @NotBlank
    private int price;

    @NotBlank
    private String author;

    @NotBlank
    private int page;
}
