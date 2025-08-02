package com.dapanda.alarm.event;

import java.time.LocalTime;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WifiTradeEndEvent {

	private Long tradeId;
	private Long memberId;
	private LocalTime startTime;
	private LocalTime endTime;

	public static WifiTradeEndEvent of(Long tradeId, Long memberId, LocalTime startTime,
			LocalTime endTime) {
		return WifiTradeEndEvent.builder()
				.tradeId(tradeId)
				.memberId(memberId)
				.startTime(startTime)
				.endTime(endTime)
				.build();
	}
}
