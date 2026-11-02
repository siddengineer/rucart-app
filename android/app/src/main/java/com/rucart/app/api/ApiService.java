package com.rucart.app.api;

import com.rucart.app.model.*;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;
import java.util.Map;

public interface ApiService {

    // Auth
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    @GET("auth/users/{id}")
    Call<ApiResponse<User>> getUser(@Path("id") Long id);

    // Categories
    @GET("categories")
    Call<ApiResponse<List<Category>>> getCategories();

    // Products
    @GET("products")
    Call<ApiResponse<List<Product>>> getProducts(
            @Query("categoryId") Long categoryId,
            @Query("storeId") Long storeId,
            @Query("search") String search
    );

    @GET("products/{id}")
    Call<ApiResponse<Product>> getProductById(@Path("id") Long id);

    @GET("products/store/{storeId}")
    Call<ApiResponse<List<Product>>> getStoreProducts(
            @Path("storeId") Long storeId,
            @Query("categoryId") Long categoryId,
            @Query("search") String search
    );

    @GET("products/admin/all")
    Call<ApiResponse<List<Product>>> getAllProductsForAdmin();

    @POST("products")
    Call<ApiResponse<Product>> createProduct(@Body ProductCreateRequest request);

    @PUT("products/{id}")
    Call<ApiResponse<Product>> updateProduct(@Path("id") Long id, @Body ProductUpdateRequest request);

    @PATCH("products/{id}/status")
    Call<ApiResponse<Product>> toggleProductStatus(@Path("id") Long id, @Query("available") boolean available);

    // Inventory
    @POST("inventory/validate")
    Call<ApiResponse<InventoryValidationResponse>> validateInventory(@Body InventoryValidationRequest request);

    @GET("inventory/store/{storeId}")
    Call<ApiResponse<List<StoreInventory>>> getStoreInventory(
            @Path("storeId") Long storeId,
            @Query("availableOnly") boolean availableOnly
    );

    @POST("inventory/store/{storeId}/adjust")
    Call<ApiResponse<StoreInventory>> adjustStock(
            @Path("storeId") Long storeId,
            @Body StockAdjustmentRequest request
    );

    @GET("inventory/store/{storeId}/low-stock")
    Call<ApiResponse<List<LowStockAlert>>> getStoreLowStockAlerts(@Path("storeId") Long storeId);

    @GET("inventory/low-stock")
    Call<ApiResponse<List<LowStockAlert>>> getAllLowStockAlerts();

    @GET("inventory/store/{storeId}/history")
    Call<ApiResponse<List<StockAdjustmentHistoryDto>>> getStoreStockHistory(@Path("storeId") Long storeId);

    @GET("inventory/history")
    Call<ApiResponse<List<StockAdjustmentHistoryDto>>> getAllStockHistory();

    // Stores
    @GET("stores")
    Call<ApiResponse<List<Store>>> getStores();

    @GET("stores/{id}")
    Call<ApiResponse<Store>> getStoreById(@Path("id") Long id);

    @GET("stores/{id}/stats")
    Call<ApiResponse<StoreOrderStatsDto>> getStoreStats(@Path("id") Long id);

    @GET("stores/service-areas")
    Call<ApiResponse<List<String>>> getServiceAreas();

    @GET("stores/by-area")
    Call<ApiResponse<List<Store>>> getStoresByArea(@Query("area") String area);

    @POST("stores/assign")
    Call<ApiResponse<StoreAssignmentResult>> previewStoreAssignment(@Body StoreAssignmentRequest request);

    @PATCH("stores/{id}/status")
    Call<ApiResponse<Store>> updateStoreStatus(@Path("id") Long id, @Body StoreStatusUpdateRequest request);

    // Addresses
    @GET("customer/addresses")
    Call<ApiResponse<List<CustomerAddress>>> getCustomerAddresses();

    @POST("customer/addresses")
    Call<ApiResponse<CustomerAddress>> addCustomerAddress(@Body CustomerAddress address);

    @PUT("customer/addresses/{id}")
    Call<ApiResponse<CustomerAddress>> updateCustomerAddress(@Path("id") Long id, @Body CustomerAddress address);

    @PATCH("customer/addresses/{id}/default")
    Call<ApiResponse<CustomerAddress>> setDefaultAddress(@Path("id") Long id);

    @DELETE("customer/addresses/{id}")
    Call<ApiResponse<Void>> deleteAddress(@Path("id") Long id);

    // Orders
    @POST("orders")
    Call<ApiResponse<Order>> createOrder(@Body OrderCreateRequest request);

    @GET("orders/{id}")
    Call<ApiResponse<Order>> getOrderById(@Path("id") Long id);

    @GET("orders/customer/{customerId}")
    Call<ApiResponse<List<Order>>> getCustomerOrders(@Path("customerId") Long customerId);

    @GET("orders/customer/{customerId}")
    Call<ApiResponse<List<Order>>> getCustomerOrdersFiltered(
            @Path("customerId") Long customerId,
            @Query("status") String status
    );

    @GET("orders/store/{storeId}")
    Call<ApiResponse<List<Order>>> getStoreOrders(@Path("storeId") Long storeId);

    @GET("orders/store/{storeId}")
    Call<ApiResponse<List<Order>>> getStoreOrdersFiltered(
            @Path("storeId") Long storeId,
            @Query("status") String status
    );

    @GET("orders/delivery/available")
    Call<ApiResponse<List<Order>>> getAvailableDeliveries();

    @GET("orders/delivery/partner/{partnerId}")
    Call<ApiResponse<List<Order>>> getDeliveryPartnerOrders(@Path("partnerId") Long partnerId);

    @PATCH("orders/{id}/status")
    Call<ApiResponse<Order>> updateOrderStatus(
            @Path("id") Long id,
            @Body OrderStatusUpdateRequest request
    );

    @POST("orders/{id}/cancel")
    Call<ApiResponse<Order>> cancelOrder(
            @Path("id") Long id,
            @Query("reason") String reason
    );

    @POST("orders/{id}/accept")
    Call<ApiResponse<Order>> acceptOrder(@Path("id") Long id);

    @POST("orders/{id}/reject")
    Call<ApiResponse<Order>> rejectOrder(
            @Path("id") Long id,
            @Query("reason") String reason
    );

    // Payments & Receipts
    @GET("payments/order/{orderId}")
    Call<ApiResponse<Payment>> getPaymentByOrderId(@Path("orderId") Long orderId);

    @GET("payments/receipt/{receiptNumber}")
    Call<ApiResponse<Payment>> getPaymentByReceiptNumber(@Path("receiptNumber") String receiptNumber);

    // Admin
    @GET("admin/dashboard")
    Call<ApiResponse<DashboardStats>> getAdminDashboard();

    @GET("admin/reports")
    Call<ApiResponse<OperationalReport>> getAdminReports(@Query("period") String period);

    @GET("admin/stores")
    Call<ApiResponse<List<Store>>> getAdminStores(@Query("search") String search);

    @POST("admin/stores")
    Call<ApiResponse<Store>> createAdminStore(@Body StoreAdminRequest request);

    @PUT("admin/stores/{id}")
    Call<ApiResponse<Store>> updateAdminStore(@Path("id") Long id, @Body StoreAdminRequest request);

    @PATCH("admin/stores/{id}/status")
    Call<ApiResponse<Store>> toggleStoreStatusAdmin(@Path("id") Long id, @Query("active") Boolean active, @Query("open") Boolean open);

    @GET("admin/users")
    Call<ApiResponse<List<User>>> getAdminUsers();

    @GET("admin/users")
    Call<ApiResponse<List<User>>> getAdminUsersFiltered(@Query("role") String role, @Query("search") String search);

    @PATCH("admin/users/{id}/status")
    Call<ApiResponse<User>> toggleUserStatus(@Path("id") Long id, @Body UserStatusUpdateRequest request);

    @GET("admin/products")
    Call<ApiResponse<List<Product>>> getAdminProducts(@Query("categoryId") Long categoryId, @Query("search") String search);

    @POST("admin/products")
    Call<ApiResponse<Product>> createAdminProduct(@Body ProductCreateRequest request);

    @PUT("admin/products/{id}")
    Call<ApiResponse<Product>> updateAdminProduct(@Path("id") Long id, @Body ProductUpdateRequest request);

    @PATCH("admin/products/{id}/status")
    Call<ApiResponse<Product>> toggleProductAvailabilityAdmin(@Path("id") Long id, @Query("available") boolean available);

    @GET("admin/categories")
    Call<ApiResponse<List<Category>>> getAdminCategories();

    @POST("admin/categories")
    Call<ApiResponse<Category>> createAdminCategory(@Body CategoryRequest request);

    @PUT("admin/categories/{id}")
    Call<ApiResponse<Category>> updateAdminCategory(@Path("id") Long id, @Body CategoryRequest request);

    @GET("admin/inventory")
    Call<ApiResponse<List<StoreInventory>>> getAdminInventory(@Query("storeId") Long storeId, @Query("lowStockOnly") Boolean lowStockOnly);

    @GET("admin/orders")
    Call<ApiResponse<List<Order>>> getAdminOrders(@Query("status") String status, @Query("storeId") Long storeId, @Query("search") String search, @Query("startDate") String startDate, @Query("endDate") String endDate);

    @GET("admin/orders/{id}")
    Call<ApiResponse<Order>> getAdminOrderDetail(@Path("id") Long id);

    @PATCH("admin/orders/{id}/status-override")
    Call<ApiResponse<Order>> overrideOrderStatusAdmin(@Path("id") Long id, @Query("status") String status, @Query("reason") String reason);

    @GET("admin/payments")
    Call<ApiResponse<List<Payment>>> getAdminPayments();

    @GET("admin/refunds")
    Call<ApiResponse<List<Payment>>> getAdminRefunds();

    @POST("admin/refunds/{paymentId}")
    Call<ApiResponse<Payment>> processRefundAdmin(@Path("paymentId") Long paymentId, @Body RefundProcessRequest request);

    @GET("admin/audit-logs")
    Call<ApiResponse<List<AuditLog>>> getAdminAuditLogs(@Query("targetType") String targetType, @Query("action") String action, @Query("actor") String actor);

    // Delivery Partner APIs
    @GET("delivery/dashboard")
    Call<ApiResponse<DeliveryDashboardDto>> getDeliveryDashboard();

    @GET("delivery/partner/me")
    Call<ApiResponse<DeliveryPartner>> getDeliveryPartnerProfile();

    @PATCH("delivery/status")
    Call<ApiResponse<DeliveryPartner>> updateDeliveryStatus(@Query("status") String status);

    @GET("delivery/available-orders")
    Call<ApiResponse<List<Order>>> getAvailableDeliveryOrders();

    @GET("delivery/my-orders")
    Call<ApiResponse<List<Order>>> getMyActiveDeliveries();

    @GET("delivery/orders/{id}")
    Call<ApiResponse<Order>> getDeliveryOrderDetail(@Path("id") Long orderId);

    @POST("delivery/orders/{id}/claim")
    Call<ApiResponse<Order>> claimDeliveryOrder(@Path("id") Long orderId);

    @POST("delivery/orders/{id}/pickup")
    Call<ApiResponse<Order>> confirmPickupOrder(@Path("id") Long orderId);

    @POST("delivery/orders/{id}/out-for-delivery")
    Call<ApiResponse<Order>> markOutForDelivery(@Path("id") Long orderId);

    @POST("delivery/orders/{id}/complete")
    Call<ApiResponse<Order>> completeDeliveryOrder(@Path("id") Long orderId, @Body Map<String, String> body);

    @POST("delivery/orders/{id}/report-issue")
    Call<ApiResponse<Order>> reportDeliveryIssue(@Path("id") Long orderId, @Body DeliveryIssueReportRequest request);

    @GET("delivery/history")
    Call<ApiResponse<List<Order>>> getDeliveryHistory(@Query("period") String period);

    // Payments & Receipts
    @POST("payments/initiate")
    Call<ApiResponse<PaymentOrderResult>> initiateOnlinePayment(@Body Map<String, Object> body);

    @POST("payments/verify")
    Call<ApiResponse<Payment>> verifyPayment(@Body PaymentVerificationRequest request);

    @POST("payments/failed")
    Call<ApiResponse<Payment>> recordFailedPayment(@Body Map<String, Object> body);

    @GET("payments/my-transactions")
    Call<ApiResponse<List<Payment>>> getMyTransactions();

    @GET("payments/{paymentId}/receipt/pdf")
    @Streaming
    Call<ResponseBody> downloadPaymentReceiptPdf(@Path("paymentId") Long paymentId);

    @GET("payments/order/{orderId}/receipt/pdf")
    @Streaming
    Call<ResponseBody> downloadOrderReceiptPdf(@Path("orderId") Long orderId);

    @POST("payments/{paymentId}/refund")
    Call<ApiResponse<Payment>> processRefund(@Path("paymentId") Long paymentId, @Body Map<String, String> body);

    // Notifications
    @GET("notifications")
    Call<ApiResponse<List<Notification>>> getMyNotifications();

    @GET("notifications/unread-count")
    Call<ApiResponse<Long>> getUnreadNotificationCount();

    @PATCH("notifications/{id}/read")
    Call<ApiResponse<Notification>> markNotificationAsRead(@Path("id") Long id);

    @PATCH("notifications/mark-all-read")
    Call<ApiResponse<Map<String, Object>>> markAllNotificationsAsRead();

    @DELETE("notifications/{id}")
    Call<ApiResponse<Void>> deleteNotification(@Path("id") Long id);
}