package com.ecommerce.bkd.controllers.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    // private final AdminDashboardService dashboardService;

    // @GetMapping("/stats")
    // public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
    // return ResponseEntity.ok(new ApiResponse<>(
    // true,
    // "Dashboard stats retrieved successfully",
    // dashboardService.getDashboardStats()));
    // }

    // @GetMapping("/recent-orders")
    // public ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> getRecentOrders(
    // @RequestParam(defaultValue = "10") int limit) {
    // return ResponseEntity.ok(new ApiResponse<>(
    // true,
    // "Recent orders retrieved successfully",
    // dashboardService.getRecentOrders(limit)));
    // }

    // @GetMapping("/low-stock-products")
    // public ResponseEntity<ApiResponse<List<ProductDTO>>> getLowStockProducts(
    // @RequestParam(defaultValue = "10") int threshold) {
    // return ResponseEntity.ok(new ApiResponse<>(
    // true,
    // "Low stock products retrieved successfully",
    // dashboardService.getLowStockProducts(threshold)));
    // }

    // @GetMapping("/sales-data")
    // public ResponseEntity<ApiResponse<List<SalesDataDTO>>> getSalesData(
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate
    // startDate,
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate
    // endDate) {
    // return ResponseEntity.ok(new ApiResponse<>(
    // true,
    // "Sales data retrieved successfully",
    // dashboardService.getSalesData(startDate, endDate)));
    // }

    // @GetMapping("/top-selling")
    // public ResponseEntity<ApiResponse<List<TopSellingProductDTO>>>
    // getTopSellingProducts(
    // @RequestParam(defaultValue = "10") int limit) {
    // return ResponseEntity.ok(new ApiResponse<>(
    // true,
    // "Top selling products retrieved successfully",
    // dashboardService.getTopSellingProducts(limit)));
    // }

    // @GetMapping("/payment-analytics")
    // public ResponseEntity<ApiResponse<PaymentAnalyticsDTO>> getPaymentAnalytics(
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate
    // startDate,
    // @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate
    // endDate) {
    // return ResponseEntity.ok(new ApiResponse<>(
    // true,
    // "Payment analytics retrieved successfully",
    // dashboardService.getPaymentAnalytics(startDate, endDate)));
    // }

    // @GetMapping("/payment-methods-distribution")
    // public ResponseEntity<ApiResponse<Map<String, Object>>>
    // getPaymentMethodsDistribution() {
    // return ResponseEntity.ok(new ApiResponse<>(
    // true,
    // "Payment methods distribution retrieved successfully",
    // dashboardService.getPaymentMethodsDistribution()));
    // }
}
