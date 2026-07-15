package com.aiplatform.business.controller;

import com.aiplatform.business.entity.Order;
import com.aiplatform.business.entity.Product;
import com.aiplatform.business.service.BusinessService;
import com.aiplatform.common.dto.ApiResponse;
import com.aiplatform.common.dto.PageResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 业务控制器 — 商品 / 订单 REST API
 */
@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    // ── 商品 CRUD ──

    @GetMapping("/products")
    public ApiResponse<PageResponse<Product>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(businessService.listProducts(page, size));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<Product> getProduct(@PathVariable Long id) {
        return ApiResponse.success(businessService.getProduct(id));
    }

    @PostMapping("/products")
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        return ApiResponse.success("创建成功", businessService.createProduct(product));
    }

    @PutMapping("/products/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ApiResponse.success("更新成功", businessService.updateProduct(id, product));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        businessService.deleteProduct(id);
        return ApiResponse.success("删除成功", null);
    }

    // ── 订单 ──

    @GetMapping("/orders")
    public ApiResponse<PageResponse<Order>> listOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(businessService.listUserOrders(userId, page, size));
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<Order> getOrder(@PathVariable String orderNo) {
        return ApiResponse.success(businessService.getOrder(orderNo));
    }

    @PostMapping("/orders")
    public ApiResponse<Order> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody List<Long> productIds) {
        return ApiResponse.success("下单成功", businessService.createOrder(userId, productIds));
    }
}
