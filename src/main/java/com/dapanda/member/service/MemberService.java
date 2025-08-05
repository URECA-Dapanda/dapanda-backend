package com.dapanda.member.service;

import com.dapanda.auth.entity.OAuthProvider;
import com.dapanda.common.exception.GlobalException;
import com.dapanda.common.exception.ResultCode;
import com.dapanda.common.service.S3Service;
import com.dapanda.jwt.JwtTokenProvider;
import com.dapanda.member.dto.request.UpdateProfileImageRequest;
import com.dapanda.member.dto.response.*;
import com.dapanda.member.entity.Member;
import com.dapanda.member.entity.MemberRole;
import com.dapanda.member.repository.MemberRepository;
import com.dapanda.product.repository.ProductRepository;
import com.dapanda.product.service.ProductService;
import com.dapanda.refreshToken.service.RefreshTokenService;
import com.dapanda.trade.repository.TradeRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

	private static final String EMAIL_REGEX =
			"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	private final MemberRepository memberRepository;
	private final TradeRepository tradeRepository;
	private final ProductRepository productRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final S3Service s3Service;
	private final ProductService productService;

	public Member findById(Long memberId) {

		return memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));
	}

	// 사용자 찾기
	public Member findUserByEmailAndProvider(String email, OAuthProvider provider) {

		return memberRepository.findByEmailAndProvider(email, provider)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));
	}

	public FindCashResponse findCash(Long memberId) {

		int cash = memberRepository.findById(memberId).orElseThrow().getCash();

		return FindCashResponse.of(cash);
	}

	public FindDataResponse findBuyingData(Long memberId) {

		BigDecimal buyingData = memberRepository.findById(memberId).orElseThrow().getBuyingData();

		return FindDataResponse.of(buyingData);
	}

	public FindDataResponse findSoldData(Long memberId) {

		BigDecimal soldData = memberRepository.findById(memberId).orElseThrow().getSellingData();

		return FindDataResponse.of(soldData);
	}

	public FindDataResponse findSellingData(Long memberId) {

		BigDecimal sellingData = productRepository.sumSoldMobileDataAmountByMemberId(memberId);

		return FindDataResponse.of(sellingData);
	}

	public void updateProfileImage(UpdateProfileImageRequest request, Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		String imageUrl = request.imageUrl();

		if (imageUrl != null && !imageUrl.isEmpty()) {

			if (s3Service.isNotValidImageExtension(imageUrl)) {
				throw new GlobalException(ResultCode.INVALID_IMAGE_FORMAT);
			}

			member.updateProfileImage(imageUrl);
			memberRepository.save(member);
		}

	}

	public MemberInfoResponse getMemberInfo(Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		Long tradeCount = tradeRepository.countTradeHistoryByMemberId(memberId);

		return MemberInfoResponse.of(
				memberId,
				member.getName(),
				member.getProfileImageUrl(),
				member.getCreatedAt().toLocalDate(),
				member.getAverageRating(),
				member.getReviewCount(),
				tradeCount != null ? tradeCount.intValue() : 0
		);
	}

	@Transactional
	public void resetMemberMobileDataAmount() {

		memberRepository.resetAllDataAmounts();
	}

	@Transactional
	public void updateMemberRole(Long memberId) {

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND));

		member.updateMemberRole(MemberRole.ROLE_MEMBER);
	}

	public Long findMemberId(Long memberId) {

		return memberRepository.findById(memberId)
				.orElseThrow(() -> new GlobalException(ResultCode.MEMBER_NOT_FOUND)).getId();
	}
}
