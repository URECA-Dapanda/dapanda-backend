package com.dapanda.alarm.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WifiTradeStartEvent {

	private Long tradeId;
	private Long memberId;
	private LocalDateTime startTime;
	private LocalDateTime endTime;

	public static WifiTradeStartEvent of(Long tradeId, Long memberId, LocalDateTime startTime, LocalDateTime endTime) {

		return WifiTradeStartEvent.builder()
				.tradeId(tradeId)
				.memberId(memberId)
				.startTime(startTime)
				.endTime(endTime)
				.build();
	}
}
