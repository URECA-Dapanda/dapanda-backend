package com.dapanda.review.dto.request;

import com.dapanda.product.entity.ProductType;
import jakarta.validation.constraints.*;

public record SaveReviewRequest(

        @NotNull(message = "리뷰 대상 회원의 아이디는 필수입니다.")
        Long revieweeId,

        @NotNull(message = "리뷰 대상 상품의 아이디는 필수입니다.")
        Long productId,

        @NotNull(message = "상품 타입은 필수입니다.")
        ProductType type,

        @NotNull(message = "평점은 필수입니다.")
        @DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
        Float rating,

        @NotNull(message = "리뷰 코멘트가 Null 입니다.")
        @Size(max = 50, message = "코멘트는 최대 50자 이하여야 합니다.")
        String comment) {
}
