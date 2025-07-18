# React Native Android UPI

A simple React Native UPI payment integration for Android.

----

## Setup

* Copy these files from node_modules/react-native-android-upi/android/src/main/java/com/rnupi/ to your project:
  - RNGPayPackage.java
  - RNAndroidUPI.java

* Create folder structure:
```
android/app/src/main/java/com/rnupi/
```

* Paste both files in this folder

* Add to MainApplication.kt:
```kotlin
import com.rnupi.RNGPayPackage

// Inside getPackages()
add(RNGPayPackage())
```

----

## Basic Usage

```javascript
import { NativeModules } from 'react-native';
const { RNGPay } = NativeModules;

const paymentData = {
    vpa: "merchant.vpa@upi",
    name: "Merchant Name",
    note: "Payment Note",
    amount: "1.00",
    transactionRef: "TXN_" + Date.now()
};

// Make payment using Google Pay
const makeGPayPayment = async () => {
    try {
        const response = await RNGPay.initiateGPayPayment(paymentData);
        console.log('Success:', response);
    } catch (error) {
        console.error('Error:', error);
    }
};
```

----

## Available Methods

* Check App Installation:
```javascript
const isInstalled = await RNGPay.isAppInstalled("com.google.android.apps.nbu.paisa.user");
```

* Google Pay Payment:
```javascript
const response = await RNGPay.initiateGPayPayment(paymentData);
```

* PhonePe Payment:
```javascript
const response = await RNGPay.initiatePhonePePayment(paymentData);
```

* Paytm Payment:
```javascript
const response = await RNGPay.initiatePaytmPayment(paymentData);
```

* Generic UPI Payment:
```javascript
const response = await RNGPay.initiateUPIPayment(paymentData);
```

----

## Payment Data Structure

```javascript
{
    vpa: string,           // Merchant UPI ID
    name: string,          // Merchant Name
    amount: string,        // Amount (e.g., "1.00")
    note: string,          // Payment Note
    transactionRef: string // Unique Reference
}
```

----

## Response Structure

```javascript
{
    status: string,        // "SUCCESS" or "FAILURE"
    transactionId: string, // UPI Transaction ID
    responseCode: string,  // Response Code
    approvalRefNo: string  // Reference Number
}
```

----

## App Package Names

* Google Pay: `com.google.android.apps.nbu.paisa.user`
* PhonePe: `com.phonepe.app`
* Paytm: `net.one97.paytm`

----

## Complete Example

```javascript
import React, { useState } from 'react';
import { View, Button, Alert } from 'react-native';
import { NativeModules } from 'react-native';

const { RNGPay } = NativeModules;

const PaymentScreen = () => {
    const [loading, setLoading] = useState(false);

    const makePayment = async () => {
        try {
            setLoading(true);
            
            const paymentData = {
                vpa: "merchant.vpa@upi",
                name: "Merchant Name",
                amount: "1.00",
                note: "Test Payment",
                transactionRef: "TXN_" + Date.now()
            };

            const response = await RNGPay.initiateGPayPayment(paymentData);
            Alert.alert('Success', 'Payment completed!');
            
        } catch (error) {
            Alert.alert('Error', error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <View style={{padding: 20}}>
            <Button 
                title="Pay ₹1" 
                onPress={makePayment}
                disabled={loading}
            />
        </View>
    );
};

export default PaymentScreen;
```

----

## Error Handling

* Common Errors:
  - APP_NOT_FOUND
  - PAYMENT_CANCELLED
  - PAYMENT_FAILED
  - INVALID_DATA

----

## Supported Apps

* Google Pay
* PhonePe
* Paytm
* Other UPI apps (via generic payment)

----

## Notes

* Android only
* Requires Android SDK 21+
* Test with small amounts first
* Handle all possible errors
* Check app installation before payment

----

## License

MIT
