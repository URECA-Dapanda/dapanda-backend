package com.dapanda.member.controller;

import com.dapanda.auth.entity.CustomUserDetails;
import com.dapanda.common.exception.CommonResponse;
import com.dapanda.member.dto.request.UpdateProfileImageRequest;
import com.dapanda.member.dto.response.*;
import com.dapanda.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/members/role")
	public ResponseEntity<CommonResponse<Void>> updateMemberRole(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		memberService.updateMemberRole(userDetails.getId());

		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

	@GetMapping("/members/cash")
	public CommonResponse<FindCashResponse> getCash(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(memberService.findCash(userDetails.getId()));
	}

	@GetMapping("/members/buying-data")
	public CommonResponse<FindDataResponse> getBuyingData(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(memberService.findBuyingData(userDetails.getId()));
	}

	@GetMapping("/members/selling-data/sold")
	public CommonResponse<FindDataResponse> getSoldData(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(memberService.findSoldData(userDetails.getId()));
	}

	@GetMapping("/members/selling-data")
	public CommonResponse<FindDataResponse> getSellingData(
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		return CommonResponse.success(memberService.findSellingData(userDetails.getId()));
	}

	@GetMapping("/members/info")
	public CommonResponse<MemberInfoResponse> getMemberInfo(
			@RequestParam(required = false) Long memberId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		Long targetMemberId = memberId != null ? memberId : userDetails.getId();

		return CommonResponse.success(memberService.getMemberInfo(targetMemberId));
	}

	@PostMapping("/members/profile-image")
	public CommonResponse<Void> updateProfileImage(
			@RequestBody @Valid UpdateProfileImageRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {

		memberService.updateProfileImage(request, userDetails.getId());

		return CommonResponse.success(null);
	}

}
