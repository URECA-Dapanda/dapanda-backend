package com.dapanda.alarm.dto;

public record AlarmMessage(
    Long tradeId,
    String startTime, // "14:30:00" 이런 형식
    String endTime
) {}
