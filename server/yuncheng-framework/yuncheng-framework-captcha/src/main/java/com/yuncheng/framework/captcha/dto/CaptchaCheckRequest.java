package com.yuncheng.framework.captcha.dto;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;

/** 天爱验证码前端提交的轨迹参数。 */
public class CaptchaCheckRequest {

    private String id;
    private ImageCaptchaTrack data;
    private ImageCaptchaTrack track;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ImageCaptchaTrack getData() {
        return data;
    }

    public void setData(ImageCaptchaTrack data) {
        this.data = data;
    }

    public ImageCaptchaTrack getTrack() {
        return track;
    }

    public void setTrack(ImageCaptchaTrack track) {
        this.track = track;
    }

    public ImageCaptchaTrack resolveTrack() {
        return data == null ? track : data;
    }
}
