package com.easymap.easymap.controller;

import com.easymap.easymap.dto.request.review.ReviewUpdateRequestDTO;
import com.easymap.easymap.dto.request.user.LoadUserStatusRequestDto;
import com.easymap.easymap.dto.request.user.UserNicknameDuplicateRequestDTO;

import com.easymap.easymap.dto.request.user.UserRequiredInfoRequestDto;
import com.easymap.easymap.dto.response.ResponseDto;
import com.easymap.easymap.dto.response.review.ReviewResponseDTO;
import com.easymap.easymap.dto.response.user.LoadUserStatusResponseDto;
import com.easymap.easymap.dto.response.user.MyReviewGetResponseDTO;
import com.easymap.easymap.dto.response.user.UserAdditionalInfoResponseDto;
import com.easymap.easymap.dto.response.user.UserNicknameDuplicateResponseDTO;
import com.easymap.easymap.entity.user.User;
import com.easymap.easymap.handler.exception.AuthenticationException;
import com.easymap.easymap.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RequestMapping("/api/v1/user")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/nickname-check")
    public ResponseEntity<? super UserNicknameDuplicateResponseDTO> userNicknameDuplicateCheck(@RequestBody @Valid UserNicknameDuplicateRequestDTO userNicknameDuplicateDTO){

        boolean rst = userService.userNicknameDuplicateCheck(userNicknameDuplicateDTO);

        return UserNicknameDuplicateResponseDTO.success(rst);

    }

    @PostMapping("/info")
    public ResponseEntity<? super LoadUserStatusResponseDto> loadUserStatus(@RequestBody LoadUserStatusRequestDto loadUserStatusRequestDto, @AuthenticationPrincipal UserDetails userDetails){

        Long userId = loadUserStatusRequestDto.getUserId();
        User user = userService.loadUserStatus(userId, userDetails);
        if(user.getDeactivationDate() != null){
            return LoadUserStatusResponseDto.userDeactivated();
        }

        return LoadUserStatusResponseDto.success(user);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/withdraw")
    public ResponseEntity<? super ResponseDto> userWithdraw(@AuthenticationPrincipal UserDetails userDetails){
        if(userDetails == null){
            throw new AuthenticationException("not authenticated");
        }

        userService.userWithdraw(userDetails);


        return ResponseDto.success();
    }

    /**
     * 필수로 입력해야 하는데 누락된 유저 정보를 리스트로 반환
     */
    @GetMapping("additional-info-check")
    public ResponseEntity<? super UserAdditionalInfoResponseDto> userAdditionalInfoCheck(){

        List<String> rst = userService.userAdditionalInfoCheck();

        return UserAdditionalInfoResponseDto.success(rst);
    }

    /**
     * 회원 가입 미기입 정보(성별, 생년월일, 닉네임, 프로필 이미지 s3 key) 입력
     * 기존 정보 변경시에도 이 api 사용
     */
    @PatchMapping("/required-info")
    public ResponseEntity<ResponseDto> patchUserRequiredInfo(
            @RequestBody UserRequiredInfoRequestDto userInfo) {
        boolean isChanged = userService.patchUserRequiredInfo(userInfo);
        if (!isChanged) {
            return ResponseDto.notModified();
        }
        return ResponseDto.success();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/review")
    public ResponseEntity<? super MyReviewGetResponseDTO> getMyReviews(@AuthenticationPrincipal UserDetails userDetails){
        List<ReviewResponseDTO> reviewResponseDTOList = userService.getMyReviews(userDetails.getUsername());

        return MyReviewGetResponseDTO.success(reviewResponseDTOList);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/review/{reviewId}")
    public ResponseEntity<? super ResponseDto> updateReview(@PathVariable(value = "reviewId")Long reviewId, @RequestBody ReviewUpdateRequestDTO reviewUpdateRequestDTO, @AuthenticationPrincipal UserDetails userDetails){
        userService.updateMyReview(reviewId, reviewUpdateRequestDTO, userDetails.getUsername());
        return ResponseDto.success();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/review/{reviewId}")
    public ResponseEntity<? super ResponseDto> deleteReview(@PathVariable(value = "reviewId")Long reviewId, @AuthenticationPrincipal UserDetails userDetails){
        userService.deleteMyReview(reviewId, userDetails.getUsername());
        return ResponseDto.success();
    }


}
