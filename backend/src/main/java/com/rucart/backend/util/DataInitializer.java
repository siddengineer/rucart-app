package com.rucart.backend.util;

import com.rucart.backend.model.*;
import com.rucart.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerAddressRepository addressRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StoreInventoryRepository inventoryRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CustomerAddressRepository addressRepository,
                           StoreRepository storeRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           StoreInventoryRepository inventoryRepository,
                           DeliveryPartnerRepository deliveryPartnerRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureDefaultAdmin();

        if (userRepository.count() > 1) {
            return; // Already seeded
        }

        // 1. Create Default Users with BCrypt Hashed Passwords
        String defaultHashedPassword = passwordEncoder.encode("123456");
        String adminHashedPassword = passwordEncoder.encode("rucart@2026");

        User customer = new User("Rahul Sharma", "9876543210", "rahul@rucart.com", defaultHashedPassword, Role.CUSTOMER, "Flat 402, Green Avenue, Shivajinagar, Pune");
        User storeOwner = new User("Amit Patil", "9876543211", "amit@rucart.com", defaultHashedPassword, Role.STORE_OWNER, "Shivajinagar Market, Pune");
        User deliveryPartnerUser = new User("Priya Desai", "9876543212", "priya@rucart.com", defaultHashedPassword, Role.DELIVERY_PARTNER, "Kothrud, Pune");
        User admin = new User("Admin Rucart", "8605528342", "rucart.admin@gmail.com", adminHashedPassword, Role.ADMIN, "Pune HQ");

        customer = userRepository.save(customer);
        storeOwner = userRepository.save(storeOwner);
        deliveryPartnerUser = userRepository.save(deliveryPartnerUser);
        admin = userRepository.save(admin);

        // 2. Customer Addresses
        CustomerAddress addr1 = new CustomerAddress(
                customer,
                "Home",
                "Rahul Sharma",
                "9876543210",
                "Flat 402, Green Avenue, Lane 3",
                "Opposite Joggers Park",
                "Pune",
                "411005",
                true
        );
        CustomerAddress addr2 = new CustomerAddress(
                customer,
                "Office",
                "Rahul Sharma",
                "9876543210",
                "Floor 5, Synergy IT Tower, Senapati Bapat Road",
                "Near ICC Tech Park",
                "Pune",
                "411016",
                false
        );
        addressRepository.saveAll(List.of(addr1, addr2));

        // 3. Create Stores
        Store store1 = new Store(
                "RUCART Express Store",
                "Fresh vegetables, daily essentials, and quick groceries",
                "Lane 4, FC Road, Shivajinagar",
                "Pune",
                "020-25501122",
                storeOwner,
                "https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=500"
        );
        store1.setServiceAreas("Shivajinagar, FC Road, Model Colony, Deccan, Pune");
        store1.setContactPerson("Ramesh Patil");

        Store store2 = new Store(
                "Fresh Basket Kothrud",
                "Farm fresh produce and organic dairy",
                "Near Paud Road, Kothrud",
                "Pune",
                "020-25442233",
                storeOwner,
                "https://images.unsplash.com/photo-1542838132-92c53300491e?w=500"
        );
        store2.setServiceAreas("Kothrud, Paud Road, Karve Nagar, Warje, Pune");
        store2.setContactPerson("Suresh Kadam");

        storeRepository.saveAll(List.of(store1, store2));

        // 4. Create Categories
        Category catVeg = new Category("Fruits & Vegetables", "Fresh from the farm produce", "ic_vegetables", "", 1);
        Category catDairy = new Category("Dairy & Eggs", "Milk, butter, paneer, and farm eggs", "ic_dairy", "", 2);
        Category catSnacks = new Category("Snacks & Beverages", "Chips, cold drinks, juices and tea", "ic_snacks", "", 3);
        Category catHousehold = new Category("Household", "Detergents, cleaning and kitchen supplies", "ic_household", "", 4);

        categoryRepository.saveAll(List.of(catVeg, catDairy, catSnacks, catHousehold));

        // 5. Create Master Products
        Product p1 = new Product("Tomato Local", "Fresh red and juicy tomatoes", new BigDecimal("24.00"), new BigDecimal("28.00"), "1 kg", "", 80, catVeg, store1);
        Product p2 = new Product("Fresh Onion", "Farm sorted crisp onions", new BigDecimal("28.00"), new BigDecimal("32.00"), "1 kg", "", 120, catVeg, store1);
        Product p3 = new Product("Potato Jyoti", "Best quality clean potatoes", new BigDecimal("22.00"), new BigDecimal("26.00"), "1 kg", "", 90, catVeg, store1);
        Product p4 = new Product("Shimla Apple", "Crisp, sweet Himachal apples", new BigDecimal("130.00"), new BigDecimal("150.00"), "1 kg", "", 40, catVeg, store1);
        Product p5 = new Product("Robusta Banana", "Naturally ripened sweet bananas", new BigDecimal("40.00"), new BigDecimal("48.00"), "1 kg", "", 60, catVeg, store1);

        Product p6 = new Product("Fresh Toned Milk", "Pasteurized fresh cow milk", new BigDecimal("35.00"), new BigDecimal("36.00"), "1 L", "", 50, catDairy, store1);
        Product p7 = new Product("Whole Wheat Bread", "Soft fiber rich healthy brown bread", new BigDecimal("40.00"), new BigDecimal("45.00"), "400 g", "", 35, catDairy, store1);
        Product p8 = new Product("Farm Eggs Pack", "Nutritious white farm fresh eggs", new BigDecimal("70.00"), new BigDecimal("80.00"), "6 pcs", "", 60, catDairy, store1);

        Product p9 = new Product("Basmati Steam Rice", "Long grain aromatic basmati rice", new BigDecimal("95.00"), new BigDecimal("110.00"), "1 kg", "", 45, catHousehold, store1);
        Product p10 = new Product("Instant Masala Noodles", "Quick favorite noodle snack", new BigDecimal("56.00"), new BigDecimal("60.00"), "4 pack", "", 70, catSnacks, store1);

        List<Product> products = productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));

        // 6. Create Store-Specific Inventory
        for (Product product : products) {
            StoreInventory inv1 = new StoreInventory(store1, product, product.getPrice(), product.getStock(), 10);
            StoreInventory inv2 = new StoreInventory(store2, product, product.getPrice().add(new BigDecimal("1.00")), 40, 10);
            inventoryRepository.saveAll(List.of(inv1, inv2));
        }

        // 7. Create Delivery Partner Details
        DeliveryPartner partner = new DeliveryPartner(deliveryPartnerUser, "MOTORCYCLE", "MH-12-DE-5544", "DL-MH12-2021008765");
        deliveryPartnerRepository.save(partner);

        // 8. Seed Welcome Notifications
        Notification n1 = new Notification(customer, "Welcome to RUCART!", "Enjoy fast, fresh grocery deliveries straight to your doorstep.", NotificationType.SYSTEM, null);
        Notification n2 = new Notification(storeOwner, "Store Registered", "RUCART Express Store is live and open for incoming orders.", NotificationType.STORE_ALERT, null);
        notificationRepository.saveAll(List.of(n1, n2));
    }
    private void ensureDefaultAdmin() {
        String adminHashedPassword = passwordEncoder.encode("rucart@2026");
        userRepository.findByEmail("rucart.admin@gmail.com").ifPresentOrElse(
                admin -> {
                    admin.setPassword(adminHashedPassword);
                    admin.setRole(Role.ADMIN);
                    admin.setName("Admin Rucart");
                    admin.setPhone("8605528342");
                    userRepository.save(admin);
                },
                () -> {
                    Optional<User> byPhone = userRepository.findByPhone("8605528342");
                    if (byPhone.isPresent()) {
                        User admin = byPhone.get();
                        admin.setEmail("rucart.admin@gmail.com");
                        admin.setPassword(adminHashedPassword);
                        admin.setRole(Role.ADMIN);
                        admin.setName("Admin Rucart");
                        userRepository.save(admin);
                    } else {
                        User admin = new User(
                                "Admin Rucart",
                                "8605528342",
                                "rucart.admin@gmail.com",
                                adminHashedPassword,
                                Role.ADMIN,
                                "Pune HQ"
                        );
                        userRepository.save(admin);
                    }
                }
        );
    }
}