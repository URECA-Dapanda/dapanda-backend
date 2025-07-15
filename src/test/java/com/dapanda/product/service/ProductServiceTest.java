package com.dapanda.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.MobileDataCursorRequest;
import com.dapanda.product.dto.request.UpdateMobileDataRequest;
import com.dapanda.product.dto.request.UpdateWifiRequest;
import com.dapanda.product.dto.request.WifiCursorRequest;
import com.dapanda.product.dto.response.MobileDataInfoResponse;
import com.dapanda.product.dto.response.UpdateMobileDataResponse;
import com.dapanda.product.dto.response.UpdateWifiResponse;
import com.dapanda.product.dto.response.WifiInfoResponse;
import com.dapanda.product.entity.MobileData;
import com.dapanda.product.entity.MobileDataFixture;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.ProductFixture;
import com.dapanda.product.entity.ProductSortOption;
import com.dapanda.product.entity.Wifi;
import com.dapanda.product.entity.WifiFixture;
import com.dapanda.product.repository.MobileDataRepository;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.product.repository.WifiRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

	private static final Long MEMBER_ID = 1L;
	private static final Long OTHER_MEMBER_ID = 2L;
	private static final Long PRODUCT_ID = 1L;
	private static final int NEW_PRICE = 9000;
	private static final float BEFORE_DATA_AMOUNT = 1.0F;
	private static final float BEFORE_REMAIN_AMOUNT = 1.0F;
	private static final float CHANGED_AMOUNT = 1.0F;
	private static final float EXCEED_CHANGED_AMOUNT = 3.0F;
	private static final float SELLING_DATA = 1.5F;
	private static final boolean SPLIT_TYPE = true;
	private static final float DATA_AMOUNT = 2.0F;
	private static final float REMAIN_AMOUNT = 1.0F;
	private static final int PRICE_PER_100MB = 300;
	private static final int PRICE = 3000;
	private static final String TITLE = "와이파이 팔아요";
	private static final String CHANGED_TITLE = "와이파이 팝니당";
	private static final String CONTENT = "서울시 강남구 할리스입니다";
	private static final String CHANGED_CONTENT = "서울시 강남구 할리스입니다람쥐";
	private static final double LATITUDE = 30F;
	private static final double CHANGED_LATITUDE = 35F;
	private static final double LONGITUDE = 126F;
	private static final double CHANGED_LONGITUDE = 150;
	private static final double AVERAGE_RATE = 3.5;
	private static final int REVIEW_COUNT = 3;
	private static final LocalDateTime START_TIME = LocalDateTime.of(2025, 3, 4, 10, 0);
	private static final LocalDateTime WRONG_START_TIME = LocalDateTime.of(2025, 3, 4, 10, 0);
	private static final LocalDateTime END_TIME = LocalDateTime.of(2025, 3, 4, 21, 0);
	private static final LocalDateTime WRONG_END_TIME = LocalDateTime.of(2024, 3, 4, 21, 0);
	private static final LocalDateTime UPDATED_AT = LocalDateTime.of(2025, 3, 3, 21, 0);

	@Mock
	private ProductRepository productRepository;

	@Mock
	private MobileDataRepository mobileDataRepository;

	@Mock
	private WifiRepository wifiRepository;

	@Mock
	private MemberRepository memberRepository;

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
			void findMobileDataSortedByRecentTest() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				for (int i = 1; i <= 3; i++) {
					summaries.add(
							MobileDataFixture.createMobileDataSummary((long) i, 1000, (long) i,
									"회원" + i, 5, 200, false));
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
			void findMobileDataSortedByPriceAscTest() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				for (int i = 1; i <= 3; i++) {
					summaries.add(
							MobileDataFixture.createMobileDataSummary((long) i, 100 + i, (long) i,
									"회원" + i, 5, 200, false));
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
			void findMobileDataSortedByAmountAscTest() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				for (int i = 1; i <= 3; i++) {
					summaries.add(MobileDataFixture.createMobileDataSummary(
							(long) i,
							(100 + 100 * i) * 10 * i,
							(long) i,
							"회원" + i,
							1 + i,
							100 + 100 * i,
							i % 2 == 0
					));
				}

				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(
						summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3)
				);

				given(productRepository.findMobileDataByCursor(null, 2,
						ProductSortOption.AMOUNT_ASC, 2.0F)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						new com.dapanda.product.dto.request.MobileDataCursorRequest(null, 2,
								"AMOUNT_ASC", 2.0F)
				);

				// then
				assertThat(result.getData()).hasSize(3);
				assertThat(result.getData().get(0).getRemainAmount()).isEqualTo(2);
			}

			@Test
			@DisplayName("AMOUNT_DESC 정렬로 데이터 상품 목록 조회를 성공한다")
			void findMobileDataSortedByAmountDescTest() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				for (int i = 3; i >= 1; i--) {
					summaries.add(
							MobileDataFixture.createMobileDataSummary((long) i, 1000, (long) i,
									"회원" + i, i * 10, 200, false));
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
			void findMobileDataFilteredByAmountTest() {

				// given
				List<MobileDataSummary> summaries = new ArrayList<>();
				summaries.add(new MobileDataSummary(1L, 1000, 1L, "회원1", 5.0F, 200,
						false));
				summaries.add(
						new MobileDataSummary(2L, 2000, 2L, "회원2", 10, 200,
								false));
				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(2L, false, 2));

				given(productRepository.findMobileDataByCursor(null, 3,
						ProductSortOption.AMOUNT_ASC, 5.0F)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						new com.dapanda.product.dto.request.MobileDataCursorRequest(null, 3,
								"AMOUNT_ASC", 5.0F));

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
			void failWhenInvalidSizeTest() {

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
	}

	@Nested
	@DisplayName("와이파이 상품 목록 조회")
	class FindWifi {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("PRICE_ASC 정렬로 와이파이 상품 목록 조회를 성공한다")
			void findWifiSortedByPriceAscTest() {

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
			void findWifiSortedByDistanceAscTest() {

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
			void findWifiSortedByAverageRateDescTest() {

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
			void findWifiFilteredByDistanceTest() {

				// given
				List<WifiSummary> summaries = new ArrayList<>();
				summaries.add(
						new WifiSummary(1L, 1000, 1L, "회원1", "상품제목1", "imageUrl", 37.0, 127.0,
								5, 5));
				summaries.add(
						new WifiSummary(2L, 2000, 2L, "회원2", "상품제목2", "imageUrl", 37.1, 127.1,
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
			void failWhenInvalidSortOptionTest() {

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

	@Nested
	@DisplayName("데이터 상품 상세 조회")
	class FindMobileDataInfo {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 상품 상세 조회를 성공한다")
			void findMobileDataInfoTest() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(DATA_AMOUNT,
						REMAIN_AMOUNT, PRICE_PER_100MB);
				MobileDataInfoResponse expectedResponse = new MobileDataInfoResponse(PRODUCT_ID,
						mobileData.getId(), PRICE, member.getId(), member.getName(), REMAIN_AMOUNT,
						PRICE_PER_100MB, AVERAGE_RATE, REVIEW_COUNT, UPDATED_AT);

				given(productRepository.existsById(PRODUCT_ID))
						.willReturn(true);
				given(productRepository.findMobileDataInfo(PRODUCT_ID)).willReturn(
						expectedResponse);

				// when
				MobileDataInfoResponse actualResponse = productService.findMobileDataInfo(
						PRODUCT_ID);

				// then
				assertThat(actualResponse.getItemId()).isEqualTo(mobileData.getId());
				assertThat(actualResponse.getRemainAmount()).isEqualTo(REMAIN_AMOUNT);
				assertThat(actualResponse.getPricePer100MB()).isEqualTo(PRICE_PER_100MB);
			}

			@Nested
			@DisplayName("실패 케이스")
			class Fail {

				@Test
				@DisplayName("데이터 상품 상세 조회 시 상품 아이디가 존재하지 않으면 예외를 던진다")
				void throwsExceptionWhenProductIdNotExist() {

					// given
					given(productRepository.existsById(PRODUCT_ID)).willReturn(false);

					// when & then
					assertThatThrownBy(() -> productService.findMobileDataInfo(PRODUCT_ID))
							.isInstanceOf(GlobalException.class)
							.hasMessage(ResultCode.PRODUCT_NOT_FOUND.getMessage());
				}

				@Test
				@DisplayName("데이터 상품 상세 조회 시 상품이 유효하지 않으면 예외를 던진다")
				void throwsExceptionWhenProductInvalid() {

					// given
					given(productRepository.existsById(PRODUCT_ID)).willReturn(true);
					given(productRepository.findMobileDataInfo(PRODUCT_ID)).willReturn(null);

					// when & then
					assertThatThrownBy(() -> productService.findMobileDataInfo(PRODUCT_ID))
							.isInstanceOf(GlobalException.class)
							.hasMessage(ResultCode.INVALID_PRODUCT.getMessage());
				}
			}
		}
	}

	@Nested
	@DisplayName("와이파이 상품 상세 조회")
	class FindWifiInfo {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("와이파이 상품 상세 조회를 성공한다")
			void findWifiInfoTest() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE,
						START_TIME, END_TIME);
				WifiInfoResponse expectedResponse = new WifiInfoResponse(PRODUCT_ID,
						wifi.getId(), PRICE, member.getId(), member.getName(), TITLE, CONTENT,
						LATITUDE, LONGITUDE, AVERAGE_RATE, REVIEW_COUNT, null, START_TIME, END_TIME,
						UPDATED_AT);

				given(productRepository.existsById(PRODUCT_ID))
						.willReturn(true);
				given(productRepository.findWifiInfo(PRODUCT_ID)).willReturn(
						expectedResponse);

				// when
				WifiInfoResponse actualResponse = productService.findWifiInfo(PRODUCT_ID);

				// then
				assertThat(actualResponse.getItemId()).isEqualTo(wifi.getId());
				assertThat(actualResponse.getTitle()).isEqualTo(TITLE);
				assertThat(actualResponse.getContent()).isEqualTo(CONTENT);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("와이파이 상품 상세 조회 시 상품 아이디가 존재하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductIdNotExist() {

				// given
				given(productRepository.existsById(PRODUCT_ID)).willReturn(false);

				// when & then
				assertThatThrownBy(() -> productService.findWifiInfo(PRODUCT_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.PRODUCT_NOT_FOUND.getMessage());
			}

			@Test
			@DisplayName("데이터 상품 상세 조회 시 상품이 유효하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductInvalid() {

				// given
				given(productRepository.existsById(PRODUCT_ID)).willReturn(true);
				given(productRepository.findWifiInfo(PRODUCT_ID)).willReturn(null);

				// when & then
				assertThatThrownBy(() -> productService.findWifiInfo(PRODUCT_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INVALID_PRODUCT.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("데이터 상품 수정")
	class UpdateMobileData {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("데이터 상품 수정이 성공하면 상품 아이디를 반환한다")
			public void updateMobileDataTest() {

				// given
				UpdateMobileDataRequest request = new UpdateMobileDataRequest(PRODUCT_ID, NEW_PRICE,
						CHANGED_AMOUNT, SPLIT_TYPE);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT,
						BEFORE_REMAIN_AMOUNT, PRICE_PER_100MB);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE, member);

				given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(mobileDataRepository.findById(mobileData.getId())).willReturn(
						Optional.of(mobileData));

				// when
				UpdateMobileDataResponse response = productService.updateMobileData(request,
						MEMBER_ID);

				// then
				assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
				assertThat(product.getPrice()).isEqualTo(NEW_PRICE);
				assertThat(mobileData.getDataAmount()).isEqualTo(
						BEFORE_DATA_AMOUNT + CHANGED_AMOUNT);
				assertThat(mobileData.getRemainAmount()).isEqualTo(
						BEFORE_REMAIN_AMOUNT + CHANGED_AMOUNT);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("상품 등록자가 아닌 회원이 상품을 수정하면 예외를 던진다")
			public void failUpdateMobileDataIfMemberIsWrongTest() throws Exception {

				// given
				UpdateMobileDataRequest request = new UpdateMobileDataRequest(PRODUCT_ID, NEW_PRICE,
						CHANGED_AMOUNT, SPLIT_TYPE);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT,
						BEFORE_REMAIN_AMOUNT, PRICE_PER_100MB);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE, member);

				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(mobileDataRepository.findById(mobileData.getId())).willReturn(
						Optional.of(mobileData));

				// when & then
				assertThatThrownBy(() -> productService.updateMobileData(request, OTHER_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_PRODUCT.getMessage());
			}

			@Test
			@DisplayName("데이터 전송량과 판매한 데이터의 합이 데이터 전송 정책을 초과할 때 예외를 던진다")
			public void failUpdateMobileDataIfDataTransferPolicyTest() throws Exception {

				// given
				UpdateMobileDataRequest request = new UpdateMobileDataRequest(PRODUCT_ID, NEW_PRICE,
						EXCEED_CHANGED_AMOUNT, SPLIT_TYPE);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT,
						BEFORE_REMAIN_AMOUNT, PRICE_PER_100MB);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE, member);

				given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(mobileDataRepository.findById(mobileData.getId())).willReturn(
						Optional.of(mobileData));

				// when & then
				assertThatThrownBy(() -> productService.updateMobileData(request, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INVALID_DATA_TRANSFER_AMOUNT.getMessage());
			}

			@Test
			@DisplayName("데이터 전송량이 유효하지 않을 때 예외를 던진다")
			public void failUpdateMobileDataIfDataInvalidTest() throws Exception {

				// given
				UpdateMobileDataRequest request = new UpdateMobileDataRequest(PRODUCT_ID, NEW_PRICE,
						CHANGED_AMOUNT, SPLIT_TYPE);

				Member member = MemberFixture.createMemberWithSellingData(MEMBER_ID, SELLING_DATA);
				MobileData mobileData = MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT,
						BEFORE_REMAIN_AMOUNT, PRICE_PER_100MB);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE, member);

				given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(mobileDataRepository.findById(mobileData.getId())).willReturn(
						Optional.of(mobileData));

				// when & then
				assertThatThrownBy(() -> productService.updateMobileData(request, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.EXCEEDED_TRANSFER_LIMIT.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("와이파이 상품 수정")
	class UpdateWifi {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("와이파이 상품 수정이 성공하면 상품 아이디를 반환한다")
			public void updateWifiTest() {

				// given
				UpdateWifiRequest request = new UpdateWifiRequest(PRODUCT_ID, NEW_PRICE,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						START_TIME, END_TIME);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, START_TIME,
						END_TIME);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						wifi.getId(), PRICE, member);

				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(wifiRepository.findById(wifi.getId())).willReturn(
						Optional.of(wifi));

				// when
				UpdateWifiResponse response = productService.updateWifi(request,
						MEMBER_ID);

				// then
				assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
				assertThat(product.getPrice()).isEqualTo(NEW_PRICE);
				assertThat(wifi.getTitle()).isEqualTo(CHANGED_TITLE);
				assertThat(wifi.getContent()).isEqualTo(CHANGED_CONTENT);
				assertThat(wifi.getLatitude()).isEqualTo(CHANGED_LATITUDE);
				assertThat(wifi.getLongitude()).isEqualTo(CHANGED_LONGITUDE);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("상품 등록자가 아닌 회원이 상품을 수정하면 예외를 던진다")
			public void failUpdateWifiIfMemberIsWrongTest() throws Exception {

				// given
				UpdateWifiRequest request = new UpdateWifiRequest(PRODUCT_ID, NEW_PRICE,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						START_TIME, END_TIME);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, START_TIME,
						END_TIME);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						wifi.getId(), PRICE, member);

				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(wifiRepository.findById(wifi.getId())).willReturn(
						Optional.of(wifi));

				// when & then
				assertThatThrownBy(() -> productService.updateWifi(request, OTHER_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_PRODUCT.getMessage());
			}

			@Test
			@DisplayName("종료 시간이 시작 시간보다 늦으면 예외를 던진다")
			public void failUpdateWifiIfTimeIsInvalidTest() throws Exception {

				// given
				UpdateWifiRequest request = new UpdateWifiRequest(PRODUCT_ID, NEW_PRICE,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						WRONG_START_TIME, WRONG_END_TIME);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, START_TIME,
						END_TIME);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						wifi.getId(), PRICE, member);

				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(wifiRepository.findById(wifi.getId())).willReturn(
						Optional.of(wifi));

				// when & then
				assertThatThrownBy(() -> productService.updateWifi(request, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.INVALID_TIME.getMessage());
			}
		}
	}
}