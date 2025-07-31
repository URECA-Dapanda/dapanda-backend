package com.dapanda.alarm.event;

import java.time.LocalTime;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WifiTradeStartEvent {

	private Long tradeId;
	private Long memberId;
	private LocalTime startTime;
	private LocalTime endTime;

	public static WifiTradeStartEvent of(Long tradeId, Long memberId, LocalTime startTime,
			LocalTime endTime) {

		return WifiTradeStartEvent.builder()
				.tradeId(tradeId)
				.memberId(memberId)
				.startTime(startTime)
				.endTime(endTime)
				.build();
	}
}
