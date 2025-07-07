package com.dapanda.plan.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlanCategory {

    _5G("5G"),
    LTE("LTE"),
    NUGET("NUGET");

    private final String category;
}
