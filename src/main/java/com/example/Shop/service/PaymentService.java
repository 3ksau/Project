package com.example.Shop.service;

import com.example.Shop.entity.Order;
import com.example.Shop.entity.OrderStatus;
import com.example.Shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${yookassa.shop-id}")
    private String shopId;

    @Value("${yookassa.secret-key}")
    private String secretKey;

    @Value("${yookassa.return-url:http://localhost:8080/orders}")
    private String returnUrl;

    @Value("${yookassa.mock:true}")
    private boolean mock;

    @Transactional
    public String createPayment(Order order) {
        if (mock) {
            order.setPaymentStatus("pending");
            order.setPaymentId("mock-" + order.getId());
            order.setConfirmationUrl("/order/" + order.getId() + "/mock-pay");
            orderRepository.save(order);
            return order.getConfirmationUrl();
        }

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(shopId, secretKey);

        var amount = Map.of(
                "value", order.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toString(),
                "currency", "RUB"
        );
        var body = Map.of(
                "amount", amount,
                "confirmation", Map.of(
                        "type", "redirect",
                        "return_url", returnUrl
                ),
                "capture", true,
                "description", "Заказ №" + order.getId()
        );

        try {
            var entity = new HttpEntity<>(body, headers);
            var response = restTemplate.exchange(
                    "https://api.yookassa.ru/v3/payments",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            var payment = response.getBody();
            var confirmation = (Map<String, String>) payment.get("confirmation");
            String confirmationUrl = confirmation != null ? confirmation.get("confirmation_url") : null;
            String paymentId = (String) payment.get("id");

            order.setPaymentId(paymentId);
            order.setPaymentStatus((String) payment.get("status"));
            order.setConfirmationUrl(confirmationUrl);
            orderRepository.save(order);

            return confirmationUrl;
        } catch (Exception e) {
            log.error("YooKassa payment creation failed for order #{}: {}", order.getId(), e.getMessage());
            throw new RuntimeException("Payment processing failed, please try again later", e);
        }
    }

    @Transactional
    public void handleCallback(Map<String, Object> payload) {
        var object = (Map<String, Object>) payload.get("object");
        if (object == null) return;

        String paymentId = (String) object.get("id");
        String status = (String) object.get("status");

        var order = orderRepository.findByPaymentId(paymentId)
                .orElse(null);
        if (order == null) return;

        order.setPaymentStatus(status);
        if ("succeeded".equals(status)) {
            order.setStatus(OrderStatus.PROCESSING);
        } else if ("canceled".equals(status)) {
            order.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(order);
    }

    @Transactional
    public void mockConfirmPayment(Long orderId) {
        var order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return;
        order.setPaymentStatus("succeeded");
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }
}
