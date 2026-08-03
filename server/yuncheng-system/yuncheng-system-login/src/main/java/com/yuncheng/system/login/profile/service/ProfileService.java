package com.yuncheng.system.login.profile.service;

import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.common.util.DataMaskingUtils;
import com.yuncheng.framework.captcha.CaptchaScene;
import com.yuncheng.framework.file.constant.FilePolicyCodes;
import com.yuncheng.framework.file.dto.FileRecord;
import com.yuncheng.framework.file.service.FileService;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.login.email.enums.EmailVerificationScene;
import com.yuncheng.system.login.email.service.EmailVerificationService;
import com.yuncheng.system.login.profile.dto.ProfileEmailChangeRequest;
import com.yuncheng.system.login.profile.dto.ProfileEmailCodeRequest;
import com.yuncheng.system.login.profile.dto.ProfileInfoResponse;
import com.yuncheng.system.login.profile.dto.ProfileOrgResponse;
import com.yuncheng.system.login.profile.dto.ProfilePasswordChangeRequest;
import com.yuncheng.system.login.security.service.LoginSecurityService;
import com.yuncheng.system.security.service.SecurityPolicyService;
import com.yuncheng.system.user.dto.UserProfileData;
import com.yuncheng.system.user.service.UserAccountService;
import com.yuncheng.system.user.service.UserInputService;
import com.yuncheng.system.user.service.UserQueryService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 查询并维护当前登录用户个人资料。 */
@Service
public class ProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);
    private static final String AVATAR_BUSINESS_TYPE = "system-user";
    private static final String AVATAR_BUSINESS_POSITION = "avatar";

    private final CurrentUserContext currentUserContext;
    private final UserQueryService userQueryService;
    private final UserAccountService userAccountService;
    private final UserInputService inputService;
    private final EmailVerificationService verificationService;
    private final LoginSecurityService loginSecurityService;
    private final SecurityPolicyService securityPolicyService;
    private final FileService fileService;

    public ProfileService(
            CurrentUserContext currentUserContext,
            UserQueryService userQueryService,
            UserAccountService userAccountService,
            UserInputService inputService,
            EmailVerificationService verificationService,
            LoginSecurityService loginSecurityService,
            SecurityPolicyService securityPolicyService,
            FileService fileService
    ) {
        this.currentUserContext = currentUserContext;
        this.userQueryService = userQueryService;
        this.userAccountService = userAccountService;
        this.inputService = inputService;
        this.verificationService = verificationService;
        this.loginSecurityService = loginSecurityService;
        this.securityPolicyService = securityPolicyService;
        this.fileService = fileService;
    }

    public ProfileInfoResponse getProfile() {
        UserProfileData profile = requireProfile();
        FileRecord avatarFile = findAvatarFile(profile.userId());
        List<ProfileOrgResponse> orgs = profile.orgs().stream()
                .map(org -> new ProfileOrgResponse(org.id(), org.fullPath()))
                .toList();
        return new ProfileInfoResponse(
                profile.userId().toString(), profile.username(), profile.realName(),
                profile.avatar(), avatarFile, DataMaskingUtils.maskPhone(profile.phone()),
                DataMaskingUtils.maskEmail(profile.email()), profile.enabled(),
                profile.roleNames(), orgs, profile.primaryOrgId(),
                profile.createdAt(), profile.passwordChangedAt()
        );
    }

    public FileRecord changeAvatar(MultipartFile image) {
        Long userId = currentUserContext.getUserId();
        List<FileRecord> previousFiles = avatarFiles(userId);
        FileRecord uploaded = fileService.upload(
                image,
                FilePolicyCodes.AVATAR,
                AVATAR_BUSINESS_TYPE,
                userId,
                AVATAR_BUSINESS_POSITION,
                0
        );
        try {
            userAccountService.changeAvatar(userId, uploaded.previewUrl());
        } catch (RuntimeException exception) {
            deleteQuietly(uploaded);
            throw exception;
        }
        previousFiles.forEach(this::deleteQuietly);
        return uploaded;
    }

    public void deleteAvatar() {
        Long userId = currentUserContext.getUserId();
        List<FileRecord> files = avatarFiles(userId);
        userAccountService.changeAvatar(userId, null);
        files.forEach(this::deleteQuietly);
    }

    public void sendEmailCode(ProfileEmailCodeRequest request) {
        securityPolicyService.requireProfileEmailEnabled();
        Long userId = currentUserContext.getUserId();
        String email = requireNewEmail(request.email());
        userAccountService.requirePasswordMatches(userId, request.currentPassword());
        loginSecurityService.verifyEmailCode(
                CaptchaScene.CHANGE_EMAIL,
                request.captchaVerification()
        );
        verificationService.send(
                EmailVerificationScene.CHANGE_EMAIL,
                userId.toString(),
                email
        );
    }

    public void changeEmail(ProfileEmailChangeRequest request) {
        securityPolicyService.requireProfileEmailEnabled();
        Long userId = currentUserContext.getUserId();
        String email = requireNewEmail(request.email());
        userAccountService.requirePasswordMatches(userId, request.currentPassword());
        verificationService.verify(
                EmailVerificationScene.CHANGE_EMAIL,
                userId.toString(),
                email,
                request.code()
        );
        userAccountService.changeEmail(userId, email);
    }

    public void changePassword(ProfilePasswordChangeRequest request) {
        userAccountService.changePassword(
                currentUserContext.getUserId(),
                request.currentPassword(),
                request.newPassword()
        );
    }

    private String requireNewEmail(String value) {
        Long userId = currentUserContext.getUserId();
        String email = inputService.requireEmail(value);
        UserProfileData profile = requireProfile();
        if (email.equals(profile.email())) {
            throw PlatformException.badRequest("新邮箱不能与当前邮箱相同");
        }
        if (!userQueryService.isEmailAvailable(email, userId)) {
            throw PlatformException.conflict("电子邮箱已经存在");
        }
        return email;
    }

    private UserProfileData requireProfile() {
        return userQueryService.findProfileById(currentUserContext.getUserId())
                .orElseThrow(() -> PlatformException.notFound("当前用户不存在"));
    }

    private FileRecord findAvatarFile(Long userId) {
        return avatarFiles(userId).stream()
                .reduce((previous, current) -> current)
                .orElse(null);
    }

    private List<FileRecord> avatarFiles(Long userId) {
        return fileService.list(
                AVATAR_BUSINESS_TYPE,
                userId,
                AVATAR_BUSINESS_POSITION
        );
    }

    private void deleteQuietly(FileRecord file) {
        try {
            fileService.delete(Long.valueOf(file.id()));
        } catch (RuntimeException exception) {
            LOGGER.warn("清理用户头像文件失败：文件主键={}", file.id(), exception);
        }
    }
}
