package com.ecommerce.bkd.services.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayXPayoutService {
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @Value("${razorpay.key.id}")     private String keyId;
    @Value("${razorpay.key.secret}") private String keySecret;
    @Value("${razorpay.x.account_number}") private String xAccountNumber;
    @Value("${razorpay.x.base_url:https://api.razorpay.com}") private String baseUrl;

    private HttpRequest.Builder baseRequest(String path) {
        String auth = Base64.getEncoder()
            .encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
        return HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .timeout(Duration.ofSeconds(10))
            .header("Content-Type", "application/json")
            .header("Authorization", "Basic " + auth);
    }

    private JSONObject sendRequest(String path, JSONObject body) throws IOException, InterruptedException {
        HttpRequest req = baseRequest(path)
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            log.error("RazorpayX {} failed: {} → {}", path, resp.statusCode(), resp.body());
            throw new IOException("RazorpayX API error: " + resp.statusCode());
        }
        return new JSONObject(resp.body());
    }

    /** 1. Create or fetch Contact */
    public JSONObject createOrFetchContact(String name, String email, String phone, String referenceId, Map<String,String> notes)
            throws IOException, InterruptedException {
        JSONObject req = new JSONObject()
            .put("name", name)
            .put("email", email)
            .put("contact", phone)
            .put("type", "customer")
            .put("reference_id", referenceId)
            .put("notes", notes == null ? new JSONObject() : new JSONObject(notes));
        return sendRequest("/v1/contacts", req);
    }

    /** 2. Create or fetch Fund Account */
    public JSONObject createOrFetchFundAccount(
            String contactId,
            boolean bankAccount,
            String accountHolderName,
            String ifsc,
            String accountNumber,
            String vpaAddress
    ) throws IOException, InterruptedException {
        JSONObject req = new JSONObject()
            .put("contact_id", contactId)
            .put("account_type", bankAccount ? "bank_account" : "vpa");
        if (bankAccount) {
            req.put("bank_account", new JSONObject()
                .put("name", accountHolderName)
                .put("ifsc", ifsc)
                .put("account_number", accountNumber)
            );
        } else {
            req.put("vpa", new JSONObject().put("address", vpaAddress));
        }
        return sendRequest("/v1/fund_accounts", req);
    }

    /** 3. Create Payout */
    public JSONObject createPayout(
            String fundAccountId,
            long amountPaise,
            boolean bankAccount,
            String referenceId,
            String narration,
            Map<String,String> notes
    ) throws IOException, InterruptedException {
        JSONObject req = new JSONObject()
            .put("account_number", xAccountNumber)
            .put("fund_account_id", fundAccountId)
            .put("amount", amountPaise)
            .put("currency", "INR")
            .put("mode", bankAccount ? "IMPS" : "UPI")
            .put("purpose", "refund")
            .put("queue_if_low_balance", true)
            .put("reference_id", referenceId)
            .put("narration", narration)
            .put("notes", notes == null ? new JSONObject() : new JSONObject(notes));
        HttpRequest httpReq = baseRequest("/v1/payouts")
            .header("X-Payout-Idempotency", referenceId)
            .POST(HttpRequest.BodyPublishers.ofString(req.toString()))
            .build();
        HttpResponse<String> resp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            log.error("Payout creation failed: {} → {}", resp.statusCode(), resp.body());
            throw new IOException("Payout API error: " + resp.statusCode());
        }
        return new JSONObject(resp.body());
    }

    /** 4. Cancel a queued payout */
    public JSONObject cancelPayout(String payoutId) throws IOException, InterruptedException {
        return sendRequest("/v1/payouts/" + payoutId + "/cancel", new JSONObject());
    }
}