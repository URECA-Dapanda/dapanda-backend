package com.dapanda.common.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CountCursorPageResponse<T> {

	private List<T> data;
	private PageInfo pageInfo;
	private long count;

	public static <T> CountCursorPageResponse<T> of(List<T> data, PageInfo pageInfo, long count) {

		return CountCursorPageResponse.<T>builder()
				.data(data)
				.pageInfo(pageInfo)
				.count(count)
				.build();
	}

	@Getter
	@Builder(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class PageInfo {

		private final Long nextCursorId;
		private final boolean hasNext;
		private final int size;

		public static PageInfo of(Long nextCursorId, boolean hasNext, int size) {

			return PageInfo.builder()
					.nextCursorId(nextCursorId)
					.hasNext(hasNext)
					.size(size)
					.build();
		}
	}
}
