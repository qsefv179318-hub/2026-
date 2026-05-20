package com.livetaxlow.taxfeedback.payment;

import com.livetaxlow.taxfeedback.common.NotFoundException;
import com.livetaxlow.taxfeedback.payment.PaymentDtos.CreatePaymentOrderRequest;
import com.livetaxlow.taxfeedback.user.UserProfile;
import com.livetaxlow.taxfeedback.user.UserService;
import java.security.SecureRandom;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentOrderService {

    private static final String ORDER_ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PaymentOrderRepository paymentOrderRepository;
    private final UserService userService;

    public PaymentOrderService(PaymentOrderRepository paymentOrderRepository, UserService userService) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.userService = userService;
    }

    @Transactional
    public PaymentOrder create(CreatePaymentOrderRequest request) {
        UserProfile user = userService.get(request.userId());
        PaymentOrder order = new PaymentOrder(
                user,
                generateOrderId(),
                request.orderName(),
                request.amount(),
                request.merchantName(),
                request.merchantCategoryCode()
        );
        return paymentOrderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public PaymentOrder getByOrderId(String orderId) {
        return paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment order not found: " + orderId));
    }

    private String generateOrderId() {
        while (true) {
            StringBuilder builder = new StringBuilder("ltl_");
            for (int i = 0; i < 24; i++) {
                builder.append(ORDER_ID_CHARS.charAt(RANDOM.nextInt(ORDER_ID_CHARS.length())));
            }
            String orderId = builder.toString();
            if (paymentOrderRepository.findByOrderId(orderId).isEmpty()) {
                return orderId;
            }
        }
    }
}
