export interface PaymentData {
  vpa: string;
  name: string;
  note: string;
  amount: string;
  transactionRef?: string;
  merchantCode?: string;
}

export interface PaymentResponse {
  status: string;
  transactionId?: string;
  responseCode?: string;
  approvalRefNo?: string;
  rawResponse?: {
    [key: string]: string;
  };
}

export interface PackageNames {
  GPAY: string;
  PHONEPE: string;
  PAYTM: string;
  BHIM: string;
}

declare const UPIPayment: {
  PackageNames: PackageNames;
  
  initiatePayment(paymentData: PaymentData): Promise<PaymentResponse>;
  initiateGPayPayment(paymentData: PaymentData): Promise<PaymentResponse>;
  initiatePhonePePayment(paymentData: PaymentData): Promise<PaymentResponse>;
  initiatePaytmPayment(paymentData: PaymentData): Promise<PaymentResponse>;
  initiateBHIMPayment(paymentData: PaymentData): Promise<PaymentResponse>;
  isAppInstalled(packageName: string): Promise<boolean>;
};

export default UPIPayment;