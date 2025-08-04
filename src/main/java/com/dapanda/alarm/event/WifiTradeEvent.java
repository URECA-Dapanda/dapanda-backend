package com.dapanda.alarm.event;

import java.time.LocalTime;

public record WifiTradeEvent(
		Long tradeId,
		EventState eventState,
		Long memberId,
		LocalTime startTime,
		LocalTime endTime) {

	public static WifiTradeEvent createStartEvent(
			Long tradeId,
			Long memberId,
			LocalTime startTime,
			LocalTime endTime) {

		return new WifiTradeEvent(tradeId, EventState.START, memberId, startTime, endTime);
	}

	public static WifiTradeEvent createEndEvent(
			Long tradeId,
			Long memberId,
			LocalTime startTime,
			LocalTime endTime) {

		return new WifiTradeEvent(tradeId, EventState.END, memberId, startTime, endTime);
	}
}
