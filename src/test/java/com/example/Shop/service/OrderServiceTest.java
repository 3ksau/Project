package com.example.Shop.service;

import com.example.Shop.entity.*;
import com.example.Shop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User user;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .email("orderuser@test.com")
                .password("pass")
                .fullName("Order User")
                .role(Role.USER)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Goods")
                .slug("goods")
                .build());

        product1 = productRepository.save(Product.builder()
                .name("Product A")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .available(true)
                .category(category)
                .build());

        product2 = productRepository.save(Product.builder()
                .name("Product B")
                .price(BigDecimal.valueOf(200))
                .stockQuantity(5)
                .available(true)
                .category(category)
                .build());
    }

    @Test
    void checkout_WhenCartHasItems_CreatesOrder() {
        cartService.addItem(user, product1.getId(), 2);
        cartService.addItem(user, product2.getId(), 1);

        Order order = orderService.checkout(user);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getTotalAmount())
                .isEqualByComparingTo(BigDecimal.valueOf(400)); // 100*2 + 200*1

        List<OrderItem> items = orderService.getOrderItems(order.getId());
        assertThat(items).hasSize(2);

        assertThat(cartService.getCartItems(user)).isEmpty();
    }

    @Test
    void checkout_WhenCartIsEmpty_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> orderService.checkout(user));
    }

    @Test
    void getUserOrders_ReturnsUserOrders() {
        cartService.addItem(user, product1.getId(), 1);
        Order order1 = orderService.checkout(user);

        cartService.addItem(user, product2.getId(), 3);
        Order order2 = orderService.checkout(user);

        List<Order> orders = orderService.getUserOrders(user);

        assertThat(orders).hasSize(2);
    }

    @Test
    void getUserOrders_WhenNoOrders_ReturnsEmpty() {
        List<Order> orders = orderService.getUserOrders(user);

        assertThat(orders).isEmpty();
    }

    @Test
    void getOrderById_WhenExists_ReturnsOrder() {
        cartService.addItem(user, product1.getId(), 1);
        Order created = orderService.checkout(user);

        Order found = orderService.getOrderById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void updateStatus_ChangesOrderStatus() {
        cartService.addItem(user, product1.getId(), 1);
        Order order = orderService.checkout(user);

        orderService.updateStatus(order.getId(), OrderStatus.SHIPPED);

        Order updated = orderService.getOrderById(order.getId());
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void getAllOrders_ReturnsAllOrdersForAdmin() {
        cartService.addItem(user, product1.getId(), 1);
        orderService.checkout(user);

        List<Order> allOrders = orderService.getAllOrders();

        assertThat(allOrders).hasSize(1);
    }

    @Test
    void checkout_ReducesStockQuantity() {
        cartService.addItem(user, product1.getId(), 2);

        orderService.checkout(user);

        Product updated = productRepository.findById(product1.getId()).orElseThrow();
        assertThat(updated.getStockQuantity()).isEqualTo(8); // 10 - 2
    }

    @Test
    void checkout_WhenInsufficientStock_ThrowsException() {
        cartService.addItem(user, product1.getId(), 2);
        product1.setStockQuantity(1);
        productRepository.save(product1);
        productRepository.flush();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(user));
        assertThat(ex.getMessage()).contains("Insufficient stock");
    }

    @Test
    void checkout_WhenProductNotAvailable_ThrowsException() {
        cartService.addItem(user, product1.getId(), 1);
        product1.setAvailable(false);
        productRepository.save(product1);
        productRepository.flush();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(user));
        assertThat(ex.getMessage()).contains("not available");
    }
}
