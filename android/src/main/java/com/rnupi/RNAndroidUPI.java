package com.rnupi;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import android.util.Log;

public class RNAndroidUPI extends ReactContextBaseJavaModule {
    private static final int UPI_REQUEST_CODE = 123;
    private static final String TAG = "RNAndroidUPI";

    // UPI App Package Names
    private static final String GPAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    private static final String PHONEPE_PACKAGE_NAME = "com.phonepe.app";
    private static final String PAYTM_PACKAGE_NAME = "net.one97.paytm";
    private static final String BHIM_PACKAGE_NAME = "in.org.npci.upiapp";

    private Promise mPromise;
    private final ReactApplicationContext reactContext;

    public RNAndroidUPI(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        
        ActivityEventListener activityEventListener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode == UPI_REQUEST_CODE && mPromise != null) {
                    try {
                        WritableMap response = standardizeUPIResponse(data);
                        String status = response.getString("status");

                        if ("SUCCESS".equalsIgnoreCase(status)) {
                            mPromise.resolve(response);
                        } else {
                            mPromise.reject("PAYMENT_FAILED", "Payment failed with status: " + status);
                        }
                    } catch (Exception e) {
                        mPromise.reject("PAYMENT_ERROR", "Error processing payment response: " + e.getMessage());
                    } finally {
                        mPromise = null;
                    }
                }
            }
        };
        
        reactContext.addActivityEventListener(activityEventListener);
    }

    @Override
    public String getName() {
        return "RNGPay";
    }

    // Generic UPI Payment
    @ReactMethod
    public void initiateUPIPayment(ReadableMap paymentData, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        mPromise = promise;

        if (currentActivity == null) {
            promise.reject("ACTIVITY_NOT_FOUND", "Activity is not available");
            return;
        }

        try {
            Uri uri = buildUPIUri(paymentData);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);

            Intent chooser = Intent.createChooser(intent, "Pay with");
            currentActivity.startActivityForResult(chooser, UPI_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(TAG, "Error initiating UPI payment", e);
            promise.reject("PAYMENT_FAILED", e.getMessage());
        }
    }

    // Google Pay Specific
    @ReactMethod
    public void initiateGPayPayment(ReadableMap paymentData, Promise promise) {
        initiateAppSpecificPayment(paymentData, GPAY_PACKAGE_NAME, promise);
    }

    // PhonePe Specific
    @ReactMethod
    public void initiatePhonePePayment(ReadableMap paymentData, Promise promise) {
        initiateAppSpecificPayment(paymentData, PHONEPE_PACKAGE_NAME, promise);
    }

    // Paytm Specific
    @ReactMethod
    public void initiatePaytmPayment(ReadableMap paymentData, Promise promise) {
        initiateAppSpecificPayment(paymentData, PAYTM_PACKAGE_NAME, promise);
    }

    // BHIM Specific
    @ReactMethod
    public void initiateBHIMPayment(ReadableMap paymentData, Promise promise) {
        initiateAppSpecificPayment(paymentData, BHIM_PACKAGE_NAME, promise);
    }

    // Check if specific UPI app is installed
    @ReactMethod
    public void isAppInstalled(String packageName, Promise promise) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.reject("ACTIVITY_NOT_FOUND", "Activity is not available");
            return;
        }

        try {
            activity.getPackageManager().getPackageInfo(packageName, 0);
            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    // Helper Methods
    private void initiateAppSpecificPayment(ReadableMap paymentData, String packageName, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        mPromise = promise;

        if (currentActivity == null) {
            promise.reject("ACTIVITY_NOT_FOUND", "Activity is not available");
            return;
        }

        try {
            Uri uri = buildUPIUri(paymentData);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(packageName);

            if (intent.resolveActivity(currentActivity.getPackageManager()) != null) {
                currentActivity.startActivityForResult(intent, UPI_REQUEST_CODE);
            } else {
                promise.reject("APP_NOT_FOUND", "The specified UPI app is not installed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initiating app specific payment", e);
            promise.reject("PAYMENT_FAILED", e.getMessage());
        }
    }

    private Uri buildUPIUri(ReadableMap paymentData) {
        Uri.Builder builder = new Uri.Builder()
            .scheme("upi")
            .authority("pay");

        // Mandatory parameters
        builder.appendQueryParameter("pa", paymentData.getString("vpa"))
               .appendQueryParameter("pn", paymentData.getString("name"))
               .appendQueryParameter("tn", paymentData.getString("note"))
               .appendQueryParameter("am", paymentData.getString("amount"))
               .appendQueryParameter("cu", "INR");

        // Optional parameters
        if (paymentData.hasKey("transactionRef")) {
            builder.appendQueryParameter("tr", paymentData.getString("transactionRef"));
        }
        if (paymentData.hasKey("merchantCode")) {
            builder.appendQueryParameter("mc", paymentData.getString("merchantCode"));
        }

        return builder.build();
    }

    private WritableMap standardizeUPIResponse(Intent data) {
        WritableMap standardResponse = Arguments.createMap();
        
        try {
            if (data == null || data.getExtras() == null) {
                standardResponse.putString("status", "FAILURE");
                return standardResponse;
            }

            Bundle bundle = data.getExtras();
            
            // Status
            String status = bundle.getString("Status",  // Google Pay
                           bundle.getString("status",   // PhonePe
                           bundle.getString("response", // Other UPI apps
                           "FAILURE")));
            
            standardResponse.putString("status", status);

            // Transaction ID
            String txnId = bundle.getString("txnId",    // Google Pay
                          bundle.getString("txnRef",     // Google Pay alternative
                          bundle.getString("transactionId", // Other UPI apps
                          "")));
            
            if (!txnId.isEmpty()) {
                standardResponse.putString("transactionId", txnId);
            }

            // Response Code
            String responseCode = bundle.getString("responseCode",
                                bundle.getString("response_code",
                                ""));
            
            if (!responseCode.isEmpty()) {
                standardResponse.putString("responseCode", responseCode);
            }

            // Approval Reference Number
            String approvalRefNo = bundle.getString("ApprovalRefNo",
                                 bundle.getString("approvalRefNo",
                                 ""));
            
            if (!approvalRefNo.isEmpty()) {
                standardResponse.putString("approvalRefNo", approvalRefNo);
            }

            // Add raw response for debugging
            WritableMap rawResponse = Arguments.createMap();
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                if (value != null) {
                    rawResponse.putString(key, value.toString());
                }
            }
            standardResponse.putMap("rawResponse", rawResponse);

        } catch (Exception e) {
            Log.e(TAG, "Error standardizing UPI response", e);
            standardResponse.putString("error", e.getMessage());
        }

        return standardResponse;
    }
}