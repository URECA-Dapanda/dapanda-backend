package com.dapanda.trade.service;

import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.member.entity.Member;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.plan.entity.Plan;
import com.dapanda.plan.repository.PlanRepository;
import com.dapanda.product.entity.MobileData;
import com.dapanda.product.entity.Product;
import com.dapanda.product.entity.ProductState;
import com.dapanda.product.repository.MobileDataRepository;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.trade.dto.request.TradeMobileDataDefaultRequest;
import com.dapanda.trade.dto.response.TradeMobileDataDefaultResponse;
import com.dapanda.trade.entity.Trade;
import com.dapanda.trade.entity.TradeDetails;
import com.dapanda.trade.repository.TradeDetailsRepository;
import com.dapanda.trade.repository.TradeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeService {

	private final TradeRepository tradeRepository;
	private final ProductRepository productRepository;
	private final MobileDataRepository mobileDataRepository;
	private final MemberRepository memberRepository;
	private final TradeDetailsRepository tradeDetailsRepository;
	private final PlanRepository planRepository;

	/**
	 * 1. 데이터 일반 상품 구매 요청
	 * 2. 해당 상품 재고 조회
	 * 3. 재고 유효하면 Lock 걸기, 재고 유효하지 않으면 Exception
	 * 4. 캐시 결제 -> 캐시에 Lock, 결제 완료되면 Lock 해제 // 캐시 잔고 부족하면 예외
	 * 5. 해당 상품 SOLD_OUT 처리
	 * 6. Trade, TradeDetails 순서대로 생성
	 * 7. 구매자: 데이터 추가 / 판매자: 데이터 차감 / 공통 필드 갱신
	 * 8. 알림, 로그 -> EventListener(트랜잭션 이후)
	 * 9. Response: 구매 데이터양, 내 총 데이터양
	 */
	@Transactional
	public TradeMobileDataDefaultResponse mobileDataDefault(
			Long buyerId, TradeMobileDataDefaultRequest request) {

		Product product = productRepository.findByIdForUpdate(request.productId())
				.orElseThrow();

		if (product.getState().equals(ProductState.SOLD_OUT)) {
			throw new GlobalException(ResultCode.ALREADY_SOLD_OUT);
		}

		if (product.getMember().getId().equals(buyerId)) {
			throw new GlobalException(ResultCode.CANNOT_PURCHASE_OWN_PRODUCT);
		}

		MobileData mobileData = mobileDataRepository.findById(product.getItemId()).orElseThrow();

		if (mobileData.isSplitType()) {

			return handlePartialPurchaseProduct();
		}

		return handleFullPurchaseProduct(product, mobileData, buyerId);
	}

	private TradeMobileDataDefaultResponse handleFullPurchaseProduct(Product product,
			MobileData mobileData, Long buyerId) {

		// Lock 건 상태로 구매자, 판매자 조회
		Member buyer = memberRepository.findByIdForUpdate(buyerId).orElseThrow();
		Member seller = memberRepository.findByIdForUpdate(product.getMember().getId())
				.orElseThrow();

		deductBuyerCashAndUpdateState(buyer, product, mobileData, product.getPrice(),
				mobileData.getDataAmount());

		Trade trade = createTradeAndTradeDetails(product, mobileData, buyer, product.getPrice());

		updateBuyerAndSellerData(buyer, seller, mobileData.getDataAmount());

		return TradeMobileDataDefaultResponse.of(trade.getId());
	}

	private void deductBuyerCashAndUpdateState(Member buyer, Product product, MobileData mobileData,
			int price, float dataAmount) {

		if (buyer.getCash() < price) {
			throw new GlobalException(ResultCode.INSUFFICIENT_CASH);
		}

		buyer.deductCash(price);
		buyer.addBuyingData(dataAmount);
		product.changeState(ProductState.SOLD_OUT);
		mobileData.deductRemainAmount(dataAmount);
	}

	private Trade createTradeAndTradeDetails(Product product, MobileData mobileData, Member buyer,
			int price) {

		Trade trade = Trade.of(mobileData.getDataAmount(), null, price, buyer);
		tradeRepository.save(trade);
		TradeDetails tradeDetails = TradeDetails.of(product, trade);
		tradeDetailsRepository.save(tradeDetails);

		return trade;
	}

	private void updateBuyerAndSellerData(Member buyer, Member seller, float dataAmount) {

		seller.addSellingData(dataAmount);

		Plan sellerPlan = planRepository.findByMember(seller).orElseThrow();
		Plan buyerPlan = planRepository.findByMember(buyer).orElseThrow();

		buyerPlan.addMobileData(dataAmount);
		sellerPlan.deductMobileData(dataAmount);
	}

	private TradeMobileDataDefaultResponse handlePartialPurchaseProduct() {

		return null;
	}
}
