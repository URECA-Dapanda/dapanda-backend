package com.dapanda.product.service;

import static com.dapanda.TestConstants.Member.*;
import static com.dapanda.TestConstants.MobileData.SELLING_DATA;
import static com.dapanda.TestConstants.MobileData.*;
import static com.dapanda.TestConstants.Pagination.DEFAULT_CURSOR_ID;
import static com.dapanda.TestConstants.Pagination.DEFAULT_SIZE_2;
import static com.dapanda.TestConstants.Product.*;
import static com.dapanda.TestConstants.Review.AVERAGE_RATE;
import static com.dapanda.TestConstants.Review.REVIEW_COUNT;
import static com.dapanda.TestConstants.Wifi.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.dapanda.common.dto.response.CountCursorPageResponse;
import com.dapanda.common.dto.response.CursorPageResponse;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.common.service.S3Service;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberFixture;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.dto.MobileDataSummary;
import com.dapanda.product.dto.WifiSummary;
import com.dapanda.product.dto.request.*;
import com.dapanda.product.dto.response.*;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 서비스 테스트")
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductImageRepository productImageRepository;

	@Mock
	private MobileDataRepository mobileDataRepository;

	@Mock
	private WifiRepository wifiRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private ProductService productService;

	@Mock
	private S3Service s3Service;

	@Nested
	@DisplayName("모바일 데이터 상품 등록")
	class CreateMobileData {

		@Test
		@DisplayName("성공: 정상 등록")
		void createMobileDataSuccess() {

			// given
			Long memberId = 1L;
			Member member = MemberFixture.createMember1WithId(memberId);
			CreateMobileDataRequest request = new CreateMobileDataRequest(12000,
					BigDecimal.valueOf(1.0), false);

			given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
			given(productRepository.sumSoldMobileDataAmountByMemberId(memberId)).willReturn(
					BigDecimal.valueOf(1.0));

			MobileData mobileData = MobileData.singleOf(new BigDecimal("1.0"), 12000, false);
			given(mobileDataRepository.save(any())).willReturn(mobileData);

			Product product = Product.of(ProductState.ACTIVE, 12000, 1L, ItemType.MOBILE_DATA,
					member);
			given(productRepository.save(any())).willReturn(product);

			// when/then (예외 없음 = 성공)
			assertThatCode(() -> productService.createMobileData(request,
					memberId)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 회원")
		void failCreateMobileDataIfNoMember() {

			// given
			Long memberId = 1234L;
			CreateMobileDataRequest request = new CreateMobileDataRequest(12000,
					new BigDecimal("1.0"), false);

			given(memberRepository.findById(memberId)).willReturn(Optional.empty());

			// when/then
			assertThatThrownBy(() -> productService.createMobileData(request, memberId))
					.isInstanceOf(GlobalException.class)
					.hasMessage(ResultCode.MEMBER_NOT_FOUND.getMessage());
		}

		@Test
		@DisplayName("실패: 판매 데이터 2GB 초과")
		void failCreateMobileDataIfOverLimit() {

			// given
			Long memberId = 1L;
			Member member = MemberFixture.createMember1WithId(memberId);
			CreateMobileDataRequest request = new CreateMobileDataRequest(12000,
					new BigDecimal("2000.0"),
					false); // 2GB 추가

			given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
			given(productRepository.sumSoldMobileDataAmountByMemberId(memberId)).willReturn(
					BigDecimal.valueOf(2000));

			// when/then
			assertThatThrownBy(() -> productService.createMobileData(request, memberId))
					.isInstanceOf(GlobalException.class)
					.hasMessage(ResultCode.EXCEEDED_TRANSFER_LIMIT.getMessage());
		}
	}

	@Nested
	@DisplayName("와이파이 상품 등록")
	class CreateWifi {

		@Test
		@DisplayName("성공: 정상 등록")
		void createWifiSuccess() {

			// given
			Long memberId = 1L;
			Member member = MemberFixture.createMember1WithId(memberId);
			CreateWifiRequest request = new CreateWifiRequest(15000, "와이파이", "설명", 37.5, 127.0,
					ADDRESS, LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusHours(5),
					Collections.singletonList("ImageUrl.jpg"));
			given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
			Wifi wifi = Wifi.of("와이파이", "설명", 37.5, 127.0, ADDRESS, request.getStartTime(),
					request.getEndTime());
			given(wifiRepository.save(any())).willReturn(wifi);

			Product product = Product.of(ProductState.ACTIVE, 15000, 1L, ItemType.WIFI, member);
			given(productRepository.save(any())).willReturn(product);

			// when/then
			assertThatCode(
					() -> productService.createWifi(request, memberId)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("실패: 존재하지 않는 회원")
		void failCreateWifiIfNoMember() {

			// given
			Long memberId = 999L;
			CreateWifiRequest request = new CreateWifiRequest(15000, "와이파이", "설명", 37.5, 127.0,
					ADDRESS, LocalDateTime.now(), LocalDateTime.now().plusHours(5),
					Collections.singletonList("ImageUrl"));
			given(memberRepository.findById(memberId)).willReturn(Optional.empty());

			// when/then
			assertThatThrownBy(() -> productService.createWifi(request, memberId))
					.isInstanceOf(GlobalException.class)
					.hasMessage(ResultCode.MEMBER_NOT_FOUND.getMessage());
		}
	}

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
									"회원" + i, new BigDecimal("5.0"), 200, false, UPDATED_AT));
				}
				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3));

				given(productRepository.findMobileDataByCursor(null, 3, ProductSortOption.RECENT,
						null)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						null, 3, "RECENT", null);

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
									"회원" + i, BigDecimal.valueOf(5), 200, false, UPDATED_AT));
				}
				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3));

				given(productRepository.findMobileDataByCursor(null, 3, ProductSortOption.PRICE_ASC,
						null)).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						null, 3, "PRICE_ASC", null);

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
							new BigDecimal(String.valueOf(2.0 + i)),
							100 + 100 * i,
							i % 2 == 0,
							UPDATED_AT
					));
				}

				CursorPageResponse<MobileDataSummary> response = CursorPageResponse.of(
						summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3)
				);

				given(productRepository.findMobileDataByCursor(null, 2,
						ProductSortOption.AMOUNT_ASC, new BigDecimal("2.0"))).willReturn(response);

				// when
				CursorPageResponse<MobileDataSummary> result = productService.findMobileDataByCursor(
						null, 2, "AMOUNT_ASC", new BigDecimal("2.0"));

				// then
				assertThat(result.getData()).hasSize(3);
				assertThat(result.getData().get(0).getRemainAmount()).isEqualByComparingTo(
						BigDecimal.valueOf(3));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("정렬 옵션이 유효하지 않으면 예외를 던진다")
			void failWhenInvalidSizeTest() {

				// given & when
				GlobalException exception = assertThrows(GlobalException.class, () -> {
					productService.findMobileDataByCursor(null, 1, "RECENT123",
							null);
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
									"회원" + i, "상품제목" + i, 37.0 + i, 127.0 + i, ADDRESS, 3F, i,
									true, UPDATED_AT));
				}
				CursorPageResponse<WifiSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3));

				given(productRepository.findWifiByCursor(null, 3, ProductSortOption.PRICE_ASC,
						true, 37.0, 127.0)).willReturn(response);

				// when
				CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(null, 3,
						"PRICE_ASC", true, 37.0, 127.0);

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
							ADDRESS,
							3F,
							i % 2 == 0 ? 2 : 1,
							true,
							UPDATED_AT
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
				CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(null, 2,
						"DISTANCE_ASC", true, 37.0, 127.0);

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
									"회원" + i, "상품제목" + idx, 37.0 + idx, 127.0 + idx, ADDRESS,
									1F + i, idx, true, UPDATED_AT));
				}
				CursorPageResponse<WifiSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(3L, false, 3));

				given(productRepository.findWifiByCursor(null, 3,
						ProductSortOption.AVERAGE_RATE_DESC, true, 37.0, 127.0)).willReturn(
						response);

				// when
				CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(null, 3,
						"AVERAGE_RATE_DESC", true, 37.0, 127.0);

				// then
				assertThat(result.getData()).hasSize(3);
				assertThat(result.getData().get(0).getAverageRate()).isEqualTo(4.0F);
			}

			@Test
			@DisplayName("distanceKm 기준으로 필터링된 와이파이 상품 목록 조회를 성공한다")
			void findWifiFilteredByDistanceTest() {

				// given
				List<WifiSummary> summaries = new ArrayList<>();
				summaries.add(
						new WifiSummary(1L, 1000, 1L, "회원1", "image.jpg",
								"상품제목1", "imageUrl", 37.0, 127.0,
								ADDRESS, 5, 5, true, UPDATED_AT));
				summaries.add(
						new WifiSummary(2L, 2000, 2L, "회원2", "image.jpg",
								"상품제목2", "imageUrl", 37.1, 127.1,
								ADDRESS, 10, 10, true, UPDATED_AT));
				CursorPageResponse<WifiSummary> response = CursorPageResponse.of(summaries,
						CursorPageResponse.PageInfo.of(2L, false, 2));

				given(productRepository.findWifiByCursor(null, 3,
						ProductSortOption.DISTANCE_ASC, true, 37.0, 127.0)).willReturn(
						response);

				// when
				CursorPageResponse<WifiSummary> result = productService.findWifiByCursor(null, 3,
						"DISTANCE_ASC", true, 37.0, 127.0);

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

				// given & when
				GlobalException exception = assertThrows(GlobalException.class, () -> {
					productService.findWifiByCursor(null, 1, "RECENT123",
							true, 37.0, 127.0);
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
				MobileData mobileData = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				MobileDataInfoResponse expectedResponse = new MobileDataInfoResponse(PRODUCT_ID,
						mobileData.getId(), PRICE_3000, member.getId(), member.getName(),
						PROFILE_IMAGE_URL, REMAIN_AMOUNT_1, PRICE_PER_100MB_300, AVERAGE_RATE,
						REVIEW_COUNT, true, false, UPDATED_AT);

				given(productRepository.existsById(PRODUCT_ID))
						.willReturn(true);
				given(productRepository.findMobileDataInfo(PRODUCT_ID, MEMBER_ID)).willReturn(
						expectedResponse);

				// when
				MobileDataInfoResponse actualResponse = productService.findMobileDataInfo(
						PRODUCT_ID, MEMBER_ID);

				// then
				assertThat(actualResponse.getItemId()).isEqualTo(mobileData.getId());
				assertThat(actualResponse.getRemainAmount()).isEqualTo(REMAIN_AMOUNT_1);
				assertThat(actualResponse.getPricePer100MB()).isEqualTo(PRICE_PER_100MB_300);
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
					assertThatThrownBy(
							() -> productService.findMobileDataInfo(PRODUCT_ID, MEMBER_ID))
							.isInstanceOf(GlobalException.class)
							.hasMessage(ResultCode.PRODUCT_NOT_FOUND.getMessage());
				}

				@Test
				@DisplayName("데이터 상품 상세 조회 시 상품이 유효하지 않으면 예외를 던진다")
				void throwsExceptionWhenProductInvalid() {

					// given
					given(productRepository.existsById(PRODUCT_ID)).willReturn(true);
					given(productRepository.findMobileDataInfo(PRODUCT_ID, MEMBER_ID)).willReturn(
							null);

					// when & then
					assertThatThrownBy(
							() -> productService.findMobileDataInfo(PRODUCT_ID, MEMBER_ID))
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
						ADDRESS, START_TIME, END_TIME);
				WifiInfoResponse expectedResponse = new WifiInfoResponse(PRODUCT_ID,
						wifi.getId(), PRICE_3000, member.getId(), member.getName(),
						PROFILE_IMAGE_URL, TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
						AVERAGE_RATE, REVIEW_COUNT, false, null, START_TIME,
						END_TIME, true, UPDATED_AT);

				given(productRepository.existsById(PRODUCT_ID))
						.willReturn(true);
				given(productRepository.findWifiInfo(PRODUCT_ID, MEMBER_ID)).willReturn(
						expectedResponse);

				// when
				WifiInfoResponse actualResponse = productService.findWifiInfo(PRODUCT_ID,
						MEMBER_ID);

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
				assertThatThrownBy(() -> productService.findWifiInfo(PRODUCT_ID, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.PRODUCT_NOT_FOUND.getMessage());
			}

			@Test
			@DisplayName("데이터 상품 상세 조회 시 상품이 유효하지 않으면 예외를 던진다")
			void throwsExceptionWhenProductInvalid() {

				// given
				given(productRepository.existsById(PRODUCT_ID)).willReturn(true);
				given(productRepository.findWifiInfo(PRODUCT_ID, MEMBER_ID)).willReturn(null);

				// when & then
				assertThatThrownBy(() -> productService.findWifiInfo(PRODUCT_ID, MEMBER_ID))
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
				UpdateMobileDataRequest request = new UpdateMobileDataRequest(PRODUCT_ID,
						NEW_PRICE_9000,
						CHANGED_AMOUNT, SPLIT_TYPE);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT,
						BEFORE_REMAIN_AMOUNT, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE_3000, member);

				given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(mobileDataRepository.findById(mobileData.getId())).willReturn(
						Optional.of(mobileData));

				// when
				UpdateMobileDataResponse response = productService.updateMobileData(request,
						MEMBER_ID);

				// then
				assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
				assertThat(product.getPrice()).isEqualTo(NEW_PRICE_9000);
				assertThat(mobileData.getDataAmount()).isEqualTo(
						BEFORE_DATA_AMOUNT.add(CHANGED_AMOUNT));
				assertThat(mobileData.getRemainAmount()).isEqualTo(
						BEFORE_REMAIN_AMOUNT.add(CHANGED_AMOUNT));
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("상품 등록자가 아닌 회원이 상품을 수정하면 예외를 던진다")
			public void failUpdateMobileDataIfMemberIsWrongTest() throws Exception {

				// given
				UpdateMobileDataRequest request = new UpdateMobileDataRequest(PRODUCT_ID,
						NEW_PRICE_9000,
						CHANGED_AMOUNT, SPLIT_TYPE);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT,
						BEFORE_REMAIN_AMOUNT, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE_3000, member);

				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(mobileDataRepository.findById(mobileData.getId())).willReturn(
						Optional.of(mobileData));

				// when & then
				assertThatThrownBy(() -> productService.updateMobileData(request, OTHER_MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.OTHER_PRODUCT.getMessage());
			}

			@Test
			@DisplayName("데이터 전송량과 판매한 데이터의 합이 데이터 전송 정책을 초과하면 예외를 던진다")
			public void failUpdateMobileDataIfDataTransferPolicyTest() throws Exception {

				// given
				UpdateMobileDataRequest request = new UpdateMobileDataRequest(PRODUCT_ID,
						NEW_PRICE_9000,
						EXCEED_CHANGED_AMOUNT, SPLIT_TYPE);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT,
						BEFORE_REMAIN_AMOUNT, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE_3000, member);

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
				UpdateMobileDataRequest request = new UpdateMobileDataRequest(PRODUCT_ID,
						NEW_PRICE_9000, CHANGED_AMOUNT, SPLIT_TYPE);

				Member member = MemberFixture.createMemberWithSellingDataWithId(MEMBER_ID,
						SELLING_DATA);
				MobileData mobileData = MobileDataFixture.createMobileData(BEFORE_DATA_AMOUNT,
						BEFORE_REMAIN_AMOUNT, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE_3000, member);

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
				UpdateWifiRequest request = new UpdateWifiRequest(PRODUCT_ID, NEW_PRICE_9000,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						ADDRESS, START_TIME, END_TIME);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
						START_TIME, END_TIME);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						wifi.getId(), PRICE_3000, member);

				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
				given(wifiRepository.findById(wifi.getId())).willReturn(
						Optional.of(wifi));

				// when
				UpdateWifiResponse response = productService.updateWifi(request,
						MEMBER_ID);

				// then
				assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
				assertThat(product.getPrice()).isEqualTo(NEW_PRICE_9000);
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
				UpdateWifiRequest request = new UpdateWifiRequest(PRODUCT_ID, NEW_PRICE_9000,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						ADDRESS, START_TIME, END_TIME);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
						START_TIME, END_TIME);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						wifi.getId(), PRICE_3000, member);

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
				UpdateWifiRequest request = new UpdateWifiRequest(PRODUCT_ID, NEW_PRICE_9000,
						CHANGED_TITLE, CHANGED_CONTENT, CHANGED_LATITUDE, CHANGED_LONGITUDE,
						ADDRESS, WRONG_START_TIME, WRONG_END_TIME);

				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				Wifi wifi = WifiFixture.createWifi(TITLE, CONTENT, LATITUDE, LONGITUDE, ADDRESS,
						START_TIME, END_TIME);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						wifi.getId(), PRICE_3000, member);

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

	@Nested
	@DisplayName("상품 삭제")
	class DeleteProduct {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("상품 삭제를 성공한다")
			void deleteProductTest() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE_3000, member);

				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

				// when
				productService.deleteProduct(PRODUCT_ID, MEMBER_ID);

				// then
				assertThat(product.getState()).isEqualTo(ProductState.DELETED);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("존재하지 않는 상품이면 예외를 던진다")
			void deleteProductFailWhenNotFoundProductTest() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithId(PRODUCT_ID,
						mobileData.getId(), PRICE_3000, member);

				// when & then
				assertThatThrownBy(() -> productService.deleteProduct(PRODUCT_ID, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.PRODUCT_NOT_FOUND.getMessage());
			}

			@Test
			@DisplayName("이미 삭제된 상품이면 예외를 던진다")
			void deleteProductFailWhenAlreadyDeletedTest() {

				// given
				Member member = MemberFixture.createMember1WithId(MEMBER_ID);
				MobileData mobileData = MobileDataFixture.createMobileData(DATA_AMOUNT_1,
						REMAIN_AMOUNT_1, PRICE_PER_100MB_300);
				Product product = ProductFixture.createMobileDataProductWithIdWithState(PRODUCT_ID,
						mobileData.getId(), ProductState.DELETED, member);

				given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

				// when & then
				assertThatThrownBy(() -> productService.deleteProduct(PRODUCT_ID, MEMBER_ID))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.ALREADY_DELETED_PRODUCT.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("판매 상태에 따른 회원의 상품 목록 조회")
	class ReadSellingProduct {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("페이징조건이 없으면 기본 페이징 값으로 조회된다")
			public void readDefaultPagingSellingProduct() {

				//given
				ReadSellingProductRequest request = new ReadSellingProductRequest(
						DEFAULT_CURSOR_ID,
						DEFAULT_SIZE_2,
						USER_DETAILS_MEMBER_ID,
						ProductState.ACTIVE
				);

				List<ReadSellingProductResponse> queryResponse = ProductFixture.createReadSellingProductResponse();

				given(memberRepository.existsById(USER_DETAILS_MEMBER_ID)).willReturn(true);
				given(productRepository.findSellingProduct(request)).willReturn(queryResponse);

				//when
				CountCursorPageResponse<ReadSellingProductResponse> response = productService.readSellingProduct(
						request);

				//then
				assertThat(response.getData()).hasSize(DEFAULT_SIZE_2);

				assertThat(response.getData()).isEqualTo(queryResponse);

				assertThat(response.getPageInfo().isHasNext()).isFalse(); // 다음 페이지가 없다고 가정
				assertThat(response.getPageInfo().getNextCursorId()).isNull(); // 다음 커서 ID가 없다고 가정
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class Fail {

			@Test
			@DisplayName("회원을 찾을 수 없으면 예외가 발생한다")
			public void memberNotFoundTest() {

				//given
				ReadSellingProductRequest request = new ReadSellingProductRequest(
						DEFAULT_CURSOR_ID,
						DEFAULT_SIZE_2,
						USER_DETAILS_MEMBER_ID,
						ProductState.ACTIVE
				);

				//when & then
				assertThatThrownBy(() -> productService.readSellingProduct(request))
						.isInstanceOf(GlobalException.class)
						.hasMessage(ResultCode.MEMBER_NOT_FOUND.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("판매 시세 조회")
	class FindMarketPrice {

		@Nested
		@DisplayName("성공 케이스")
		class Success {

			@Test
			@DisplayName("판매 시세(최근거래가, 평균거래가)를 조회한다")
			public void findMarketPrice() {

				// given
				String itemTypeString = "MOBILE_DATA";
				ItemType itemType = ItemType.MOBILE_DATA;
				int expectedRecentPrice = 200;
				int expectedAveragePrice = 3000;

				FindMarketPriceResponse response = FindMarketPriceResponse.of(expectedRecentPrice,
						expectedAveragePrice);

				given(productRepository.findMarketPrice(itemType)).willReturn(response);

				// when
				FindMarketPriceResponse result = productService.findMarketPrice(itemTypeString);

				// then
				assertThat(result.getRecentPrice()).isEqualTo(expectedRecentPrice);
				assertThat(result.getAveragePrice()).isEqualTo(expectedAveragePrice);
			}
		}
	}
}
