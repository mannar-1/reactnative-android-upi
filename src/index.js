import { NativeModules, Platform } from 'react-native';

const { RNGPay } = NativeModules;

const UPIPayment = {
  // Package Names Constants
  PackageNames: {
    GPAY: "com.google.android.apps.nbu.paisa.user",
    PHONEPE: "com.phonepe.app",
    PAYTM: "net.one97.paytm",
    BHIM: "in.org.npci.upiapp"
  },

  // Generic UPI Payment
  initiatePayment(paymentData) {
    if (Platform.OS !== 'android') {
      return Promise.reject('UPI Payment is supported only on Android');
    }
    return RNGPay.initiateUPIPayment(paymentData);
  },

  // Google Pay
  initiateGPayPayment(paymentData) {
    if (Platform.OS !== 'android') {
      return Promise.reject('Google Pay is supported only on Android');
    }
    return RNGPay.initiateGPayPayment(paymentData);
  },

  // PhonePe
  initiatePhonePePayment(paymentData) {
    if (Platform.OS !== 'android') {
      return Promise.reject('PhonePe is supported only on Android');
    }
    return RNGPay.initiatePhonePePayment(paymentData);
  },

  // Paytm
  initiatePaytmPayment(paymentData) {
    if (Platform.OS !== 'android') {
      return Promise.reject('Paytm is supported only on Android');
    }
    return RNGPay.initiatePaytmPayment(paymentData);
  },

  // BHIM
  initiateBHIMPayment(paymentData) {
    if (Platform.OS !== 'android') {
      return Promise.reject('BHIM is supported only on Android');
    }
    return RNGPay.initiateBHIMPayment(paymentData);
  },

  // Check if app is installed
  isAppInstalled(packageName) {
    if (Platform.OS !== 'android') {
      return Promise.resolve(false);
    }
    return RNGPay.isAppInstalled(packageName);
  }
};

export default UPIPayment;