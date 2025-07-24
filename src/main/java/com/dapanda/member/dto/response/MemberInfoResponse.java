package com.dapanda.member.dto.response;

import java.time.LocalDate;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfoResponse {

	private String name;
	private String profileImageUrl;
	private LocalDate joinedAt;
	private float averageRating;
	private int reviewCount;
	private int tradeCount;

	public static MemberInfoResponse of(
			String name,
			String profileImageUrl,
			LocalDate joinedAt,
			float averageRating,
			int reviewCount,
			int tradeCount
	) {
		return MemberInfoResponse.builder()
				.name(name)
				.profileImageUrl(profileImageUrl)
				.joinedAt(joinedAt)
				.averageRating(averageRating)
				.reviewCount(reviewCount)
				.tradeCount(tradeCount)
				.build();
	}
}
