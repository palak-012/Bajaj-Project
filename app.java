package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebhookApplication implements CommandLineRunner {

    private static final String REGISTER_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    // Your registration details
    private static final String NAME = "Palak Agrawal";
    private static final String REG_NO = "86";
    private static final String EMAIL = "your.email@example.com"; // replace with your email

    // Final SQL Query
    private static final String FINAL_QUERY =
            "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
            "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
            "FROM EMPLOYEE e1 " +
            "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
            "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT " +
            "AND e2.DOB > e1.DOB " +
            "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
            "ORDER BY e1.EMP_ID DESC;";

    public static void main(String[] args) {
        SpringApplication.run(WebhookApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // Step 1: Register
        Map<String, String> body = new HashMap<>();
        body.put("name", NAME);
        body.put("regNo", REG_NO);
        body.put("email", EMAIL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(REGISTER_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            String webhookUrl = (String) response.getBody().get("webhook");
            String accessToken = (String) response.getBody().get("accessToken");

            // Step 2: Submit final query
            Map<String, String> solution = new HashMap<>();
            solution.put("finalQuery", FINAL_QUERY);

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);
            authHeaders.setBearerAuth(accessToken);

            HttpEntity<Map<String, String>> submitRequest = new HttpEntity<>(solution, authHeaders);

            ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhookUrl, submitRequest, String.class);

            System.out.println("Submission response: " + submitResponse.getBody());
        } else {
            System.out.println("Failed to register. Status: " + response.getStatusCode());
        }
    }
}
