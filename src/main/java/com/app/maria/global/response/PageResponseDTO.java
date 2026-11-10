package com.app.maria.global.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class PageResponseDTO<T> {

    private List<T> content;
    private long totalCount;
    private int page;
    private int size;

    public static <T> PageResponseDTO<T> of(List<T> content, long totalCount, int page, int size) {
        return PageResponseDTO.<T>builder()
                .content(content)
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .build();
    }
}
