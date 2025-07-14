package com.dapanda.common.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
