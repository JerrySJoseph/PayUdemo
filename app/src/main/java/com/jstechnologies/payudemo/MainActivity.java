package com.jstechnologies.payudemo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.payumoney.core.PayUmoneyConstants;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.activities.PayUmoneyActivity;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static String SALT="YOUR SALT";
    public static String MKEY="YOUR MKEY";
    public static String MID="YOUR MID";
    String serverCalculatedHash;
    String txnid,amount,email,pname,uname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txnid="txn"+System.currentTimeMillis();
        amount="1.0";
        email="youremail@gmail.com";
        pname="Product1";
        uname="Jerin";

    }

    public void InitPayment(View view) {
        PayUmoneySdkInitializer.PaymentParam.Builder builder = new
                PayUmoneySdkInitializer.PaymentParam.Builder();
        builder.setAmount(amount)                          // Payment amount
                .setTxnId(txnid)                                             // Transaction ID
                .setPhone("YOUR PHONE WITH COUNTRY CODE")                                           // User Phone number
                .setProductName(pname)                   // Product Name or description
                .setFirstName(uname)                              // User First name
                .setEmail(email)                                            // User Email ID
                .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php")                    // Success URL (surl)
                .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php")                     //Failure URL (furl)
                .setUdf1("")
                .setUdf2("")
                .setUdf3("")
                .setUdf4("")
                .setUdf5("")
                .setUdf6("")
                .setUdf7("")
                .setUdf8("")
                .setUdf9("")
                .setUdf10("")
                .setIsDebug(false)                              // Integration environment - true (Debug)/ false(Production)
                .setKey(MKEY)                        // Merchant key
                .setMerchantId(MID); // Debug Merchant ID
        PayUmoneySdkInitializer.PaymentParam paymentParam = null;
        try {
            paymentParam = builder.build();
        } catch (Exception e) {
            Log.e("PAY U ",e.getMessage());
        }
        generateHashFromServer(paymentParam);

    }


    public void generateHashFromServer(final PayUmoneySdkInitializer.PaymentParam paymentParam) {
        RequestQueue queue1 = Volley.newRequestQueue(this);
        String url = "URL FOR HASHGEN SCRIPT"; // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray dataArray;
                        JSONObject jsonObject;
                        String merchantHash="";

                        try {
                            jsonObject = new JSONObject(response);
                            //dataArray = jsonObject.getJSONArray(JSON_ARRAY);
                            //Toast.makeText(getApplicationContext(), "m" + jsonObject.getString("result"), Toast.LENGTH_SHORT).show();
                            merchantHash = jsonObject.getString("result");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //setting up response values to the fragment
                        if (merchantHash.isEmpty() || merchantHash.equals("")) {
                            Toast.makeText(MainActivity.this, "Could not generate hash", Toast.LENGTH_SHORT).show();
                        } else {
                            paymentParam.setMerchantHash(merchantHash);
                            //Toast.makeText(FinalCheckoutActivity.this, "m:"+mPaymentParams.getParams(), Toast.LENGTH_SHORT).show();
                            //Log.e(TAG, "onPostExecute: "+mPaymentParams.getParams() );
                            PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, MainActivity.this, R.style.AppTheme_default, false);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(), "Error:" + error, Toast.LENGTH_LONG).show();
            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(PayUmoneyConstants.KEY,MKEY );
                params.put(PayUmoneyConstants.AMOUNT,amount+"" );
                params.put(PayUmoneyConstants.TXNID,txnid);
                params.put(PayUmoneyConstants.EMAIL,email);
                params.put(PayUmoneyConstants.PRODUCT_INFO,pname);
                params.put(PayUmoneyConstants.FIRSTNAME,uname);

                return params;
            }
        };
        queue1.add(stringRequest);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result Code is -1 send from Payumoney activity
        Log.d("MainActivity", "request code " + requestCode + " resultcode " + resultCode);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data !=
                null) {
            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager
                    .INTENT_EXTRA_TRANSACTION_RESPONSE);

            ResultModel resultModel = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT);

            // Check which object is non-null
            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {
                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    //Success Transaction
                } else {
                    //Failure Transaction
                }

                // Response from Payumoney
                String payuResponse = transactionResponse.getPayuResponse();

                // Response from SURl and FURL
                String merchantResponse = transactionResponse.getTransactionDetails();

                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Payu's Data : " + payuResponse + "\n\n\n Merchant's Data: " + merchantResponse)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();

            } else if (resultModel != null && resultModel.getError() != null) {
                Log.d("PAYU RESPONSE", "Error response : " + resultModel.getError().getTransactionResponse());
            } else {
                Log.d("PAYU RESPONSE", "Both objects are null!");
            }
        }
    }
}
