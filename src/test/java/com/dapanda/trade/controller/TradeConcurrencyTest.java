package com.dapanda.trade.controller;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.base.BaseIntegrationTest;
import com.dapanda.member.entity.*;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.Plan;
import com.dapanda.plan.entity.PlanFixture;
import com.dapanda.plan.repository.PlanRepository;
import com.dapanda.product.entity.*;
import com.dapanda.product.repository.MobileDataRepository;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.trade.dto.request.DefaultPurchaseMobileDataRequest;
import com.dapanda.trade.dto.request.ScrapPurchaseMobileDataRequest;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.entity.TradeFixture;
import com.dapanda.trade.repository.TradeRepository;
import com.dapanda.trade.service.TradeService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.dapanda.TestConstants.Member.CASH_5000;
import static com.dapanda.TestConstants.MobileData.*;
import static com.dapanda.TestConstants.Product.PRICE_3000;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("performance")
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // 순서 보장
//@Execution(ExecutionMode.SAME_THREAD)  // 병렬 실행 막기
@DisplayName("상품 거래 동시성 테스트")
public class TradeConcurrencyTest extends BaseIntegrationTest {

	private static final int THREAD_COUNT = 1000;
	private static final int THREAD_POOL_SIZE = 32;
	private static final CountDownLatch setupLatch = new CountDownLatch(1);

	@Autowired
	private EntityManager entityManager;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private TradeService tradeService;
	@Autowired
	private TradeRepository tradeRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private MobileDataRepository mobileDataRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private PlanRepository planRepository;

	private Product product;
	private MobileData mobileData;
	private List<Long> memberIds;

	@BeforeEach
	void setup() {

		cleanupDatabase();

		// 상품 1개 생성
		Member seller = MemberFixture.createMember1();
		ReflectionTestUtils.setField(seller, "sellingData", BigDecimal.ZERO);
		memberRepository.saveAndFlush(seller);

		Plan sellerPlan = planRepository.saveAndFlush(
				PlanFixture.createPlan(seller, BigDecimal.valueOf(5.0)));
		this.mobileData = mobileDataRepository.save(
				MobileDataFixture.createMobileDataSplitType(
						DATA_AMOUNT_2, REMAIN_AMOUNT_2, PRICE_PER_100MB_150));
		this.product = productRepository.saveAndFlush(
				ProductFixture.createMobileDataProduct(PRICE_3000, mobileData.getId(), seller));

		// 멤버 1000명 생성
		this.memberIds = IntStream.range(0, THREAD_COUNT)
				.mapToObj(i -> {
					Member member = Member.ofLocalMember("user" + i + "@test.com", "user" + i,
							"pw1234", OAuthProvider.LOCAL, MemberRole.ROLE_MEMBER);

					ReflectionTestUtils.setField(member, "buyingData", BigDecimal.ZERO);
					ReflectionTestUtils.setField(member, "cash", CASH_5000);
					memberRepository.saveAndFlush(member);

					Plan memberPlan = PlanFixture.createPlan(member, BigDecimal.valueOf(10.0));
					planRepository.saveAndFlush(memberPlan);

					return member.getId();
				})
				.collect(Collectors.toList());

		setupLatch.countDown();
		System.out.println("setup 종료");
	}

	private void cleanupDatabase() {

		entityManager.clear();

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

		jdbcTemplate.execute("TRUNCATE TABLE member");
		jdbcTemplate.execute("TRUNCATE TABLE mobile_data");
		jdbcTemplate.execute("TRUNCATE TABLE product");
		jdbcTemplate.execute("TRUNCATE TABLE trade");
		jdbcTemplate.execute("TRUNCATE TABLE trade_details");
		jdbcTemplate.execute("TRUNCATE TABLE plan");

		jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
	}

	@Test
	@DisplayName("동시에 1000명이 통합 데이터 상품 일반 구매를 시도하면 Trade는 두 개만 생성된다")
	void mobileDataFullPurchaseConcurrencyTest() throws Exception {

		// given
		setupLatch.await();

		ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

		DefaultPurchaseMobileDataRequest request = new DefaultPurchaseMobileDataRequest(
				product.getId(),
				mobileData.getId(), DATA_AMOUNT_2);

		// when
		for (int i = 0; i < THREAD_COUNT; i++) {
			final int index = i;
			executor.submit(() -> {
				try {
					tradeService.defaultPurchaseMobileData(memberIds.get(index), request);  // 구매 시도
					System.out.println("상품 구매 성공");
				} catch (Exception e) {
					System.out.println("e = " + e);
					// 예외 무시: 동시에 실패하는 요청 있을 수 있음
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		// then
		List<Trade> trades = tradeRepository.findAll();
		assertThat(trades).hasSize(2);
	}

	@Test
	@DisplayName("동시에 1000명이 분할 데이터 상품 일반 구매를 시도하면 Trade는 네 개만 생성된다")
	void mobileDataPartialPurchaseConcurrencyTest() throws Exception {

		// given
		setupLatch.await();

		ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

		DefaultPurchaseMobileDataRequest request = new DefaultPurchaseMobileDataRequest(
				product.getId(),
				mobileData.getId(), DATA_AMOUNT_1);

		// when
		for (int i = 0; i < THREAD_COUNT; i++) {
			final int index = i;
			executor.submit(() -> {
				try {
					tradeService.defaultPurchaseMobileData(memberIds.get(index), request);  // 구매 시도
					System.out.println("상품 구매 성공");
				} catch (Exception e) {
					System.out.println("e = " + e);
					// 예외 무시: 동시에 실패하는 요청 있을 수 있음
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		// then
		List<Trade> trades = tradeRepository.findAll();
		assertThat(trades).hasSize(4);
	}

	@Test
	@DisplayName("동시에 1000명이 데이터 상품 자투리 구매를 시도하면 Trade는 두 개만 생성된다")
	void mobileDataScrapPurchaseConcurrencyTest()
			throws Exception {

		// given
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

		ScrapPurchaseMobileDataRequest request = new ScrapPurchaseMobileDataRequest(DATA_AMOUNT_2,
				PRICE_3000,
				List.of(TradeFixture.createMobileDataScrap(product, mobileData, PRICE_3000,
						DATA_AMOUNT_2)));

		// when
		for (int i = 0; i < THREAD_COUNT; i++) {
			final int index = i;
			executor.submit(() -> {
				try {
					tradeService.scrapPurchaseMobileData(memberIds.get(index), request);
					System.out.println("상품 구매 성공");
				} catch (Exception e) {
					System.out.println("e = " + e);
					// 예외 무시: 동시에 실패하는 요청 있을 수 있음
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		// then
		List<Trade> trades = tradeRepository.findAll();
		Product updateProduct = productRepository.findById(product.getId()).orElseThrow();
		MobileData updateMobileData = mobileDataRepository.findById(mobileData.getId())
				.orElseThrow();

		assertThat(trades).hasSize(2);
		assertThat(updateProduct.getState()).isEqualTo(ProductState.SOLD_OUT);
		assertThat(updateMobileData.getRemainAmount().compareTo(BigDecimal.ZERO)).isEqualTo(0);
	}
}
