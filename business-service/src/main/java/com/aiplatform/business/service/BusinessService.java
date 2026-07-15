package com.aiplatform.business.service;

import com.aiplatform.business.entity.Order;
import com.aiplatform.business.entity.Product;
import com.aiplatform.business.repository.OrderRepository;
import com.aiplatform.business.repository.ProductRepository;
import com.aiplatform.common.dto.PageResponse;
import com.aiplatform.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 业务服务 — 商品 CRUD、订单管理
 */
@Service
public class BusinessService {

    private static final Logger log = LoggerFactory.getLogger(BusinessService.class);

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public BusinessService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    // ── 商品管理 ──

    /** 分页查询商品列表 */
    public PageResponse<Product> listProducts(int page, int size) {
        var pageable = PageRequest.of(page - 1, size);
        var result = productRepository.findByDeletedFalse(pageable);
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }

    /** 获取商品详情 */
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("商品", id));
    }

    /** 创建商品 */
    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /** 更新商品信息 */
    @Transactional
    public Product updateProduct(Long id, Product updates) {
        var product = getProduct(id);
        if (updates.getName() != null) product.setName(updates.getName());
        if (updates.getDescription() != null) product.setDescription(updates.getDescription());
        if (updates.getCategory() != null) product.setCategory(updates.getCategory());
        if (updates.getPrice() != null) product.setPrice(updates.getPrice());
        if (updates.getStock() != null) product.setStock(updates.getStock());
        if (updates.getStatus() != null) product.setStatus(updates.getStatus());
        return productRepository.save(product);
    }

    /** 软删除商品 */
    @Transactional
    public void deleteProduct(Long id) {
        var product = getProduct(id);
        product.setDeleted(true);
        productRepository.save(product);
    }

    // ── 订单管理 ──

    /** 分页查询用户订单 */
    public PageResponse<Order> listUserOrders(Long userId, int page, int size) {
        var pageable = PageRequest.of(page - 1, size);
        var result = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }

    /** 根据订单号查询 */
    public Order getOrder(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> BusinessException.notFound("订单", orderNo));
    }

    /** 创建订单 */
    @Transactional
    public Order createOrder(Long userId, List<Long> productIds) {
        var products = productRepository.findAllById(productIds);
        if (products.isEmpty()) {
            throw BusinessException.badRequest("商品列表为空");
        }

        var total = products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("pending");

        order = orderRepository.save(order);
        log.info("新订单创建: {} 用户={} 金额={}", order.getOrderNo(), userId, total);
        return order;
    }

    /** 生成唯一订单号: ORD + yyyyMMddHHmmss + 4位随机数 */
    private String generateOrderNo() {
        var now = LocalDateTime.now();
        var rand = new Random().nextInt(10000);
        return "ORD" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", rand);
    }
}
