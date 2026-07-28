package com.yuncheng.system.login.profile.controller;

import com.yuncheng.framework.file.dto.FileRecord;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.login.auth.support.RefreshCookieManager;
import com.yuncheng.system.login.profile.dto.ProfileEmailChangeRequest;
import com.yuncheng.system.login.profile.dto.ProfileEmailCodeRequest;
import com.yuncheng.system.login.profile.dto.ProfileInfoResponse;
import com.yuncheng.system.login.profile.dto.ProfilePasswordChangeRequest;
import com.yuncheng.system.login.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 当前登录用户个人中心接口。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/user/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final RefreshCookieManager cookieManager;

    public ProfileController(
            ProfileService profileService,
            RefreshCookieManager cookieManager
    ) {
        this.profileService = profileService;
        this.cookieManager = cookieManager;
    }

    @GetMapping
    public ApiResponse<ProfileInfoResponse> profile() {
        return ApiResponse.success(profileService.getProfile());
    }

    @PostMapping("/email-code")
    @OperationLog("发送邮箱变更验证码")
    public ApiResponse<Void> sendEmailCode(
            @Valid @RequestBody ProfileEmailCodeRequest request
    ) {
        profileService.sendEmailCode(request);
        return ApiResponse.success(null);
    }

    @PutMapping("/email")
    @OperationLog("修改个人邮箱")
    public ApiResponse<Void> changeEmail(
            @Valid @RequestBody ProfileEmailChangeRequest request
    ) {
        profileService.changeEmail(request);
        return ApiResponse.success(null);
    }

    @PutMapping("/password")
    @OperationLog("修改个人密码")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ProfilePasswordChangeRequest request,
            HttpServletResponse response
    ) {
        profileService.changePassword(request);
        cookieManager.clear(response);
        return ApiResponse.success(null);
    }

    @PostMapping("/avatar")
    @OperationLog("更换个人头像")
    public ApiResponse<FileRecord> changeAvatar(
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(profileService.changeAvatar(file));
    }

    @DeleteMapping("/avatar")
    @OperationLog("删除个人头像")
    public ApiResponse<Void> deleteAvatar() {
        profileService.deleteAvatar();
        return ApiResponse.success(null);
    }
}
