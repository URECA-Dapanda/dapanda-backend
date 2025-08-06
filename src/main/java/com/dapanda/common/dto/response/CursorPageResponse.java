package com.dapanda.common.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CursorPageResponse<T> {

	private List<T> data;
	private PageInfo pageInfo;

	public static <T> CursorPageResponse<T> of(List<T> data, PageInfo pageInfo) {

		return CursorPageResponse.<T>builder()
				.data(data)
				.pageInfo(pageInfo)
				.build();
	}

	@Getter
	@Builder(access = AccessLevel.PRIVATE)
	public static class PageInfo {

		private final Long nextCursorId;
		private final boolean hasNext;
		private final int size;

		@JsonCreator
		public PageInfo(
				@JsonProperty("nextCursorId") Long nextCursorId,
				@JsonProperty("hasNext") boolean hasNext,
				@JsonProperty("size") int size) {
			this.nextCursorId = nextCursorId;
			this.hasNext = hasNext;
			this.size = size;
		}

		public static PageInfo of(Long nextCursorId, boolean hasNext, int size) {
			return PageInfo.builder()
					.nextCursorId(nextCursorId)
					.hasNext(hasNext)
					.size(size)
					.build();
		}
	}
}
