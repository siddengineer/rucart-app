import requests
import json
import random
import sys

BASE_URL = "http://localhost:8080/api/v1"

def print_result(test_name, passed, detail=""):
    status = "PASS" if passed else "FAIL"
    print(f"[{status}] {test_name}: {detail}")
    return passed

def run_audit():
    results = {}
    session = requests.Session()
    
    # 1. Admin Login
    print("\n--- 1. Testing Admin Login ---")
    admin_login_payload = {
        "phone": "rucart.admin@gmail.com",
        "password": "rucart@2026"
    }
    r = session.post(f"{BASE_URL}/auth/login", json=admin_login_payload)
    data = r.json().get("data", {}) if isinstance(r.json(), dict) else {}
    admin_token = data.get("token") or r.json().get("token")
    if r.status_code == 200 and admin_token:
        admin_headers = {"Authorization": f"Bearer {admin_token}", "Content-Type": "application/json"}
        results["admin_login"] = print_result("Admin Login with rucart.admin@gmail.com", True, f"Token received (role: {data.get('role')})")
    else:
        results["admin_login"] = print_result("Admin Login with rucart.admin@gmail.com", False, f"Status: {r.status_code}, Body: {r.text}")
        return results

    # 2. Customer Login
    print("\n--- 2. Testing Customer Login ---")
    cust_login_payload = {
        "phone": "9876543210",
        "password": "password"
    }
    r = requests.post(f"{BASE_URL}/auth/login", json=cust_login_payload)
    if r.status_code != 200:
        cust_login_payload["password"] = "123456"
        r = requests.post(f"{BASE_URL}/auth/login", json=cust_login_payload)
    cdata = r.json().get("data", {}) if isinstance(r.json(), dict) else {}
    cust_token = cdata.get("token") or r.json().get("token")
    if r.status_code == 200 and cust_token:
        cust_headers = {"Authorization": f"Bearer {cust_token}", "Content-Type": "application/json"}
        results["customer_login"] = print_result("Customer Login (9876543210)", True, f"Token received (role: {cdata.get('role')})")
    else:
        results["customer_login"] = print_result("Customer Login", False, f"Status: {r.status_code}, Body: {r.text}")

    # 3. Rider Management Endpoints
    print("\n--- 3. Testing Rider Management ---")
    rand_suffix = random.randint(10000, 99999)
    rider_phone = f"98111{rand_suffix}"
    rider_email = f"rider{rand_suffix}@rucart.com"
    new_rider_payload = {
        "name": f"Test Rider {rand_suffix}",
        "phone": rider_phone,
        "email": rider_email,
        "address": "45 Market Road, Pune",
        "vehicleType": "MOTORCYCLE",
        "vehicleNumber": f"MH-12-AB-{rand_suffix % 10000}",
        "licenseNumber": f"DL-MH-2026-{rand_suffix}"
    }
    r = session.post(f"{BASE_URL}/admin/riders", json=new_rider_payload, headers=admin_headers)
    new_rider_id = None
    initial_password = None
    if r.status_code in [200, 201] and r.json().get("success"):
        data = r.json().get("data", {})
        new_rider_id = data.get("id")
        initial_password = data.get("generatedPassword")
        results["rider_registration"] = print_result("Admin Register Rider", True, f"Rider ID: {new_rider_id}, Generated Password: {initial_password}")
    else:
        results["rider_registration"] = print_result("Admin Register Rider", False, f"Status: {r.status_code}, Body: {r.text}")

    # List Riders
    r = session.get(f"{BASE_URL}/admin/riders", headers=admin_headers)
    if r.status_code == 200 and r.json().get("success"):
        riders_list = r.json().get("data", [])
        results["rider_list"] = print_result("Admin List Riders", True, f"Found {len(riders_list)} riders")
    else:
        results["rider_list"] = print_result("Admin List Riders", False, f"Status: {r.status_code}")

    # Rider Password Reset
    if new_rider_id:
        r = session.post(f"{BASE_URL}/admin/riders/{new_rider_id}/reset-password", headers=admin_headers)
        if r.status_code == 200 and r.json().get("success"):
            new_pass = r.json().get("data")
            results["rider_password_reset"] = print_result("Admin Reset Rider Password", True, f"New Password: {new_pass}")
        else:
            results["rider_password_reset"] = print_result("Admin Reset Rider Password", False, f"Status: {r.status_code}, Body: {r.text}")

        # Rider Status Update (Suspend & Reactivate)
        r = session.patch(f"{BASE_URL}/admin/riders/{new_rider_id}/status?status=SUSPENDED", headers=admin_headers)
        if r.status_code == 200 and r.json().get("success"):
            results["rider_deactivation"] = print_result("Admin Suspend Rider Status", True, "Status set to SUSPENDED")
        else:
            results["rider_deactivation"] = print_result("Admin Suspend Rider Status", False, f"Status: {r.status_code}, Body: {r.text}")

        r = session.patch(f"{BASE_URL}/admin/riders/{new_rider_id}/status?status=AVAILABLE", headers=admin_headers)
        if r.status_code == 200 and r.json().get("success"):
            print_result("Admin Reactivate Rider Status", True, "Status restored to AVAILABLE")

    # 4. Store Owner Management
    print("\n--- 4. Testing Store Owner Management ---")
    owner_phone = f"98222{rand_suffix}"
    owner_email = f"owner{rand_suffix}@rucart.com"
    new_owner_payload = {
        "name": f"Store Owner {rand_suffix}",
        "phone": owner_phone,
        "email": owner_email,
        "address": "12 High St, Pune",
        "storeName": f"Fresh Mart {rand_suffix}",
        "storeArea": "Kothrud, Shivaji Nagar, Baner"
    }
    r = session.post(f"{BASE_URL}/admin/store-owners", json=new_owner_payload, headers=admin_headers)
    new_owner_id = None
    if r.status_code in [200, 201] and r.json().get("success"):
        data = r.json().get("data", {})
        new_owner_id = data.get("userId")
        owner_pass = data.get("generatedPassword")
        results["store_owner_registration"] = print_result("Admin Register Store Owner", True, f"Owner UserID: {new_owner_id}, Generated Password: {owner_pass}")
    else:
        results["store_owner_registration"] = print_result("Admin Register Store Owner", False, f"Status: {r.status_code}, Body: {r.text}")

    # List Store Owners
    r = session.get(f"{BASE_URL}/admin/store-owners", headers=admin_headers)
    if r.status_code == 200 and r.json().get("success"):
        owners_list = r.json().get("data", [])
        results["store_owner_list"] = print_result("Admin List Store Owners", True, f"Found {len(owners_list)} store owners")
    else:
        results["store_owner_list"] = print_result("Admin List Store Owners", False, f"Status: {r.status_code}, Body: {r.text}")

    if new_owner_id:
        # Reset Owner Password
        r = session.post(f"{BASE_URL}/admin/store-owners/{new_owner_id}/reset-password", headers=admin_headers)
        if r.status_code == 200 and r.json().get("success"):
            new_pass = r.json().get("data")
            results["store_owner_password_reset"] = print_result("Admin Reset Store Owner Password", True, f"New Password: {new_pass}")
        else:
            results["store_owner_password_reset"] = print_result("Admin Reset Store Owner Password", False, f"Status: {r.status_code}, Body: {r.text}")

        # Toggle Owner Status
        r = session.patch(f"{BASE_URL}/admin/store-owners/{new_owner_id}/status?active=false", headers=admin_headers)
        if r.status_code == 200 and r.json().get("success"):
            results["store_owner_deactivation"] = print_result("Admin Deactivate Store Owner", True, "Active set to false")
        else:
            results["store_owner_deactivation"] = print_result("Admin Deactivate Store Owner", False, f"Status: {r.status_code}")

    # 5. Commission Management
    print("\n--- 5. Testing Commission Configuration ---")
    r = session.get(f"{BASE_URL}/admin/commission", headers=admin_headers)
    if r.status_code == 200 and r.json().get("success"):
        comm = r.json().get("data", {}).get("current", {})
        results["commission_get"] = print_result("Admin Get Commission Config", True, f"Type: {comm.get('commissionType')}, Rate: {comm.get('commissionRate')}")
    else:
        results["commission_get"] = print_result("Admin Get Commission Config", False, f"Status: {r.status_code}")

    update_comm_payload = {
        "commissionType": "FIXED",
        "commissionRate": 30.00,
        "notes": "Audited standard flat rate"
    }
    r = session.post(f"{BASE_URL}/admin/commission", json=update_comm_payload, headers=admin_headers)
    if r.status_code == 200 and r.json().get("success"):
        results["commission_update"] = print_result("Admin Update Commission Config", True, "Successfully updated to FIXED 30.00")
    else:
        results["commission_update"] = print_result("Admin Update Commission Config", False, f"Status: {r.status_code}, Body: {r.text}")

    # 6. Invoices and PDF Downloads
    print("\n--- 6. Testing Invoices and PDF Generation ---")
    r = session.get(f"{BASE_URL}/admin/invoices", headers=admin_headers)
    first_order_id = None
    if r.status_code == 200 and r.json().get("success"):
        invoices = r.json().get("data", [])
        results["invoices_list"] = print_result("Admin List Invoices", True, f"Found {len(invoices)} invoices")
        if invoices:
            first_order_id = invoices[0].get("orderId")
    else:
        results["invoices_list"] = print_result("Admin List Invoices", False, f"Status: {r.status_code}")

    if first_order_id:
        r = session.get(f"{BASE_URL}/admin/invoices/{first_order_id}/pdf", headers=admin_headers)
        if r.status_code == 200 and len(r.content) > 100 and r.content.startswith(b"%PDF"):
            results["invoice_pdf"] = print_result(f"Admin Invoice PDF Download (Order #{first_order_id})", True, f"Valid PDF received, size: {len(r.content)} bytes")
        else:
            results["invoice_pdf"] = print_result(f"Admin Invoice PDF Download (Order #{first_order_id})", False, f"Status: {r.status_code}, content prefix: {r.content[:20]}")

        # Test Payment Confirmation
        r = session.post(f"{BASE_URL}/admin/invoices/{first_order_id}/confirm-payment", headers=admin_headers)
        if r.status_code == 200 and r.json().get("success"):
            results["payment_confirm"] = print_result(f"Admin Confirm Payment (Order #{first_order_id})", True, f"Payment confirmed: {r.json().get('message')}")
        else:
            results["payment_confirm"] = print_result(f"Admin Confirm Payment (Order #{first_order_id})", False, f"Status: {r.status_code}, Body: {r.text}")

    # 7. Offers & Banners APIs
    print("\n--- 7. Testing Offers & Banners APIs ---")
    offer_payload = {
        "title": f"AUDIT SALE {rand_suffix}",
        "description": "Functional Audit 20% off",
        "code": f"AUDIT{rand_suffix}",
        "discountType": "FIXED",
        "discountValue": 20.0,
        "minOrderAmount": 100.0,
        "startDate": "2026-01-01",
        "expiryDate": "2026-12-31",
        "active": True
    }
    r = session.post(f"{BASE_URL}/admin/offers", json=offer_payload, headers=admin_headers)
    if r.status_code in [200, 201] and r.json().get("success"):
        offer_id = r.json().get("data", {}).get("id")
        results["admin_create_offer"] = print_result("Admin Create Offer", True, f"Created offer ID {offer_id}")
    else:
        results["admin_create_offer"] = print_result("Admin Create Offer", False, f"Status: {r.status_code}, Body: {r.text}")

    r = session.get(f"{BASE_URL}/offers/active")
    if r.status_code == 200 and r.json().get("success"):
        active_offers = r.json().get("data", [])
        results["public_active_offers"] = print_result("Public Active Offers", True, f"Retrieved {len(active_offers)} active offers")
    else:
        results["public_active_offers"] = print_result("Public Active Offers", False, f"Status: {r.status_code}")

    banner_payload = {
        "title": f"Audit Banner {rand_suffix}",
        "imageUrl": "https://images.unsplash.com/photo-1542838132-92c53300491e",
        "redirectType": "CATEGORY",
        "redirectTarget": "GROCERY",
        "displayOrder": 1,
        "active": True
    }
    r = session.post(f"{BASE_URL}/admin/banners", json=banner_payload, headers=admin_headers)
    if r.status_code in [200, 201] and r.json().get("success"):
        banner_id = r.json().get("data", {}).get("id")
        results["admin_create_banner"] = print_result("Admin Create Banner", True, f"Created banner ID {banner_id}")
    else:
        results["admin_create_banner"] = print_result("Admin Create Banner", False, f"Status: {r.status_code}, Body: {r.text}")

    r = session.get(f"{BASE_URL}/banners/active")
    if r.status_code == 200 and r.json().get("success"):
        active_banners = r.json().get("data", [])
        results["public_active_banners"] = print_result("Public Active Banners", True, f"Retrieved {len(active_banners)} active banners")
    else:
        results["public_active_banners"] = print_result("Public Active Banners", False, f"Status: {r.status_code}")

    print("\n--- Summary of API Audit ---")
    passed = sum(1 for v in results.values() if v)
    total = len(results)
    print(f"Total Tests: {total}, Passed: {passed}, Failed: {total - passed}")
    return results

if __name__ == "__main__":
    run_audit()
