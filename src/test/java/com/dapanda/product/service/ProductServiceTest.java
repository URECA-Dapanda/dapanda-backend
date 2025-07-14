package com.dapanda.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.MobileDataCursorRequest;
import com.dapanda.product.dto.request.WifiCursorRequest;
import com.dapanda.product.entity.DataSellingUnit;
import com.dapanda.product.entity.MobileDataFixture;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.product.entity.WifiFixture;
import com.dapanda.product.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 서비스 테스트")
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Nested
	@DisplayName("데이터 상품 목록 조회")
	class FindMobileData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("RECENT 정렬로 데이터 상품 목록 조회를 성공한다")
			void findMobileDataSortedByRecent() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				for (int i = 1; i <= 3; i++) {
					summaries.add(
							MobileDataFixture.createMobileDataSummary((long) i, 1000, (long) i,
									"회원" + i, 5,
									DataSellingUnit.GB, 200, false));
				}
				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3));

				given(productRepository.findMobileDataByCursor(null, 3, ProductSortOption.RECENT,
						null)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						new com.dapanda.product.dto.request.MobileDataCursorRequest(null, 3,
								"RECENT", null));

				// then
				assertThat(result.getData()).hasSize(3);
			}

			@Test
			@DisplayName("PRICE_ASC 정렬로 데이터 상품 목록 조회를 성공한다")
			void findMobileDataSortedByPriceAsc() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				for (int i = 1; i <= 3; i++) {
					summaries.add(
							MobileDataFixture.createMobileDataSummary((long) i, 100 + i, (long) i,
									"회원" + i, 5,
									DataSellingUnit.GB, 200, false));
				}
				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3));

				given(productRepository.findMobileDataByCursor(null, 3, ProductSortOption.PRICE_ASC,
						null)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						new com.dapanda.product.dto.request.MobileDataCursorRequest(null, 3,
								"PRICE_ASC", null));

				// then
				assertThat(result.getData()).hasSize(3);
				assertThat(result.getData().get(0).getPrice()).isEqualTo(101);
			}


			@Test
			@DisplayName("AMOUNT_ASC 정렬로 데이터 상품 목록 조회를 성공한다")
			void findMobileDataSortedByAmountAsc() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				for (int i = 1; i <= 3; i++) {
					summaries.add(MobileDataFixture.createMobileDataSummary(
							(long) i,
							(100 + 100 * i) * 10 * i,
							(long) i,
							"회원" + i,
							1 + i,
							DataSellingUnit.GB,
							100 + 100 * i,
							i % 2 == 0
					));
				}

				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(
						summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3)
				);

				given(productRepository.findMobileDataByCursor(null, 2,
						ProductSortOption.AMOUNT_ASC, 2)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						new com.dapanda.product.dto.request.MobileDataCursorRequest(null, 2,
								"AMOUNT_ASC", 2)
				);

				// then
				assertThat(result.getData()).hasSize(3);
				assertThat(result.getData().get(0).getRemainAmount()).isEqualTo(2);
			}

			@Test
			@DisplayName("AMOUNT_DESC 정렬로 데이터 상품 목록 조회를 성공한다")
			void findMobileDataSortedByAmountDesc() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				for (int i = 3; i >= 1; i--) {
					summaries.add(
							MobileDataFixture.createMobileDataSummary((long) i, 1000, (long) i,
									"회원" + i, i * 10,
									DataSellingUnit.GB, 200, false));
				}
				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3));

				given(productRepository.findMobileDataByCursor(null, 3,
						ProductSortOption.AMOUNT_DESC, null)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						new com.dapanda.product.dto.request.MobileDataCursorRequest(null, 3,
								"AMOUNT_DESC", null));

				// then
				assertThat(result.getData()).hasSize(3);
				assertThat(result.getData().get(0).getRemainAmount()).isEqualTo(30);
			}

			@Test
			@DisplayName("dataAmount 기준으로 필터링된 데이터 상품 목록 조회를 성공한다")
			void findMobileDataFilteredByAmount() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				summaries.add(new MobileDataSummary(1L, 1000, 1L, "회원1", 5, DataSellingUnit.GB, 200,
						false));
				summaries.add(
						new MobileDataSummary(2L, 2000, 2L, "회원2", 10, DataSellingUnit.GB, 200,
								false));
				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(2L, false, 2));

				given(productRepository.findMobileDataByCursor(null, 3,
						ProductSortOption.AMOUNT_ASC, 5)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						new com.dapanda.product.dto.request.MobileDataCursorRequest(null, 3,
								"AMOUNT_ASC", 5));

				// then
				assertThat(result.getData()).hasSize(2);
				assertThat(result.getData().get(0).getRemainAmount()).isGreaterThanOrEqualTo(5);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("정렬 옵션이 유효하지 않으면 예외를 던진다")
			void failWhenInvalidSize() {

				// given
				MobileDataCursorRequest request = new MobileDataCursorRequest(null, 1, "RECENT123",
						null);

				// when
				GlobalException exception = assertThrows(GlobalException.class, () -> {
					productService.findMobileDataByCursor(request);
				});

				// then
				assertEquals(ResultCode.INVALID_PRODUCT_SORT_OPTION, exception.getResultCode());
			}
		}

		@Nested
		@DisplayName("와이파이 상품 목록 조회")
		class FindWifi {

			@Nested
			@DisplayName("성공 케이스")
			class Success {

				@Test
				@DisplayName("RECENT 정렬로 와이파이 상품 목록 조회를 성공한다")
				void findWifiSortedByRecent() {

					// given
					List<WifiSummary> summaries = new ArrayList<>();
					for (int i = 1; i <= 3; i++) {
						summaries.add(
								WifiFixture.createWifiSummary((long) i, 1000, (long) i,
										"회원" + i, "상품제목" + i, 37.0 + i, 127.0 + i, i * 10.0, i));
					}
					CursorPageResponse<WifiSummary> response = CursorPageResponse.of(summaries,
							CursorPageResponse.PageInfo.of(3L, false, 3));

					given(productRepository.findWifiByCursor(null, 3, ProductSortOption.RECENT,
							true, 37.0, 127.0)).willReturn(response);

					// when
					CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(
							new WifiCursorRequest(null, 3, "RECENT", true, 37.0, 127.0));

					// then
					assertThat(result.getData()).hasSize(3);
				}

				@Test
				@DisplayName("PRICE_ASC 정렬로 와이파이 상품 목록 조회를 성공한다")
				void findWifiSortedByPriceAsc() {

					// given
					List<WifiSummary> summaries = new ArrayList<>();
					for (int i = 1; i <= 3; i++) {
						summaries.add(
								WifiFixture.createWifiSummary((long) i, 100 + i, (long) i,
										"회원" + i, "상품제목" + i, 37.0 + i, 127.0 + i, i * 10.0, i));
					}
					CursorPageResponse<WifiSummary> response = CursorPageResponse.of(summaries,
							CursorPageResponse.PageInfo.of(3L, false, 3));

					given(productRepository.findWifiByCursor(null, 3, ProductSortOption.PRICE_ASC,
							true, 37.0, 127.0)).willReturn(response);

					// when
					CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(
							new WifiCursorRequest(null, 3, "PRICE_ASC", true, 37.0, 127.0));

					// then
					assertThat(result.getData()).hasSize(3);
					assertThat(result.getData().get(0).getPrice()).isEqualTo(101);
				}


				@Test
				@DisplayName("DISTANCE_ASC 정렬로 와이파이 상품 목록 조회를 성공한다")
				void findWifiSortedByDistanceAsc() {

					// given
					List<WifiSummary> summaries = new ArrayList<>();
					for (int i = 1; i <= 3; i++) {
						summaries.add(WifiFixture.createWifiSummary(
								(long) i,
								(100 + 100 * i) * 10 * i,
								(long) i,
								"회원" + i,
								"상품제목" + i,
								37.0 + i,
								127.0 + i,
								i * 10.0,
								i % 2 == 0 ? 2 : 1
						));
					}

					CursorPageResponse<WifiSummary> response = CursorPageResponse.of(
							summaries,
							CursorPageResponse.PageInfo.of(3L, false, 3)
					);

					given(productRepository.findWifiByCursor(null, 2,
							ProductSortOption.DISTANCE_ASC, true, 37.0, 127.0)).willReturn(
							response);

					// when
					CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(
							new WifiCursorRequest(null, 2, "DISTANCE_ASC", true, 37.0, 127.0));

					// then
					assertThat(result.getData()).hasSize(3);
					assertThat(result.getData().get(0).getDistanceKm()).isEqualTo(1.0);
				}

				@Test
				@DisplayName("AVERAGE_RATE_DESC 정렬로 와이파이 상품 목록 조회를 성공한다")
				void findWifiSortedByAverageRateDesc() {

					// given
					List<WifiSummary> summaries = new ArrayList<>();
					for (int i = 3; i >= 1; i--) {
						int idx = 4 - i;
						summaries.add(
								WifiFixture.createWifiSummary((long) i, 1000, (long) i,
										"회원" + i, "상품제목" + idx, 37.0 + idx, 127.0 + idx, 5.0 - idx,
										idx));
					}
					CursorPageResponse<WifiSummary> response = CursorPageResponse.of(summaries,
							CursorPageResponse.PageInfo.of(3L, false, 3));

					given(productRepository.findWifiByCursor(null, 3,
							ProductSortOption.AVERAGE_RATE_DESC, true, 37.0, 127.0)).willReturn(
							response);

					// when
					CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(
							new WifiCursorRequest(null, 3, "AVERAGE_RATE_DESC", true, 37.0, 127.0));

					// then
					assertThat(result.getData()).hasSize(3);
					assertThat(result.getData().get(0).getAverageRate()).isEqualTo(4.0);
				}

				@Test
				@DisplayName("distanceKm 기준으로 필터링된 와이파이 상품 목록 조회를 성공한다")
				void findWifiFilteredByDistance() {

					// given
					List<WifiSummary> summaries = new ArrayList<>();
					summaries.add(
							new WifiSummary(1L, 1000, 1L, "회원1", "상품제목1", 37.0, 127.0, "imageUrl",
									5, 5));
					summaries.add(
							new WifiSummary(2L, 2000, 2L, "회원2", "상품제목2", 37.1, 127.1, "imageUrl",
									10, 10));
					CursorPageResponse<WifiSummary> response = CursorPageResponse.of(summaries,
							CursorPageResponse.PageInfo.of(2L, false, 2));

					given(productRepository.findWifiByCursor(null, 3,
							ProductSortOption.DISTANCE_ASC, true, 37.0, 127.0)).willReturn(
							response);

					// when
					CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(
							new WifiCursorRequest(null, 3, "DISTANCE_ASC", true, 37.0, 127.0));

					// then
					assertThat(result.getData()).hasSize(2);
					assertThat(result.getData().get(0).getDistanceKm()).isGreaterThanOrEqualTo(5);
				}
			}

			@Nested
			@DisplayName("실패 케이스")
			class Fail {

				@Test
				@DisplayName("정렬 옵션이 유효하지 않으면 예외를 던진다")
				void failWhenInvalidSortOption() {

					// given
					WifiCursorRequest request = new WifiCursorRequest(null, 1, "RECENT123",
							true, 37.0, 127.0);

					// when
					GlobalException exception = assertThrows(GlobalException.class, () -> {
						productService.findWifiByCursor(request);
					});

					// then
					assertEquals(ResultCode.INVALID_PRODUCT_SORT_OPTION, exception.getResultCode());
				}
			}
		}
	}
}