export type CaptchaScene =
  | 'CHANGE_EMAIL'
  | 'LOGIN'
  | 'REGISTER_EMAIL'
  | 'RESET_PASSWORD_EMAIL';

export interface TacInstance {
  config?: {
    addRequestChain?: (chain: TacRequestChain) => void;
  };
  destroyWindow: () => void;
  init: () => void;
  reloadCaptcha: () => void;
}

export interface TacRequestParam {
  data?: {
    id?: string;
  };
}

export interface TianaiCaptchaApiResponse<T = TianaiCaptchaData> {
  captcha?: TianaiCaptchaData;
  code?: number;
  data?: T;
  id?: string;
  msg?: string;
}

export interface TianaiCaptchaData {
  backgroundImage?: string;
  id?: string;
  templateImage?: string;
  type?: string;
}

export interface TianaiCaptchaVerificationData {
  captchaVerification?: string;
}

export interface TacRequestChain {
  postRequest?: (
    type: string,
    requestParam: TacRequestParam,
    response: TianaiCaptchaApiResponse,
  ) => boolean | undefined;
  preRequest?: (
    type: string,
    requestParam: TacRequestParam,
  ) => boolean | undefined;
}

export interface TacConstructor {
  new (
    config: Record<string, unknown>,
    style?: Record<string, unknown>,
  ): TacInstance;
}

declare global {
  interface Window {
    TAC?: TacConstructor;
  }
}
