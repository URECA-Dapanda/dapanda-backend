package com.dapanda.report.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {

    SPAM("스팸/광고"),
    FRAUD("사기"),
    ABUSIVE_COMMENT("욕설"),
    EXPLICIT_CONTENT("음란물/선정적 콘텐츠"),
    PRICE_MANIPULATION("가격 조작"),
    STALKING("스토킹"),
    OTHER("기타");

    private final String category;

}
