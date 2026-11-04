package com.rucart.backend.controller;

import com.rucart.backend.dto.ApiResponse;
import com.rucart.backend.model.Offer;
import com.rucart.backend.model.PromotionalBanner;
import com.rucart.backend.service.OfferBannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OfferBannerController {

    private final OfferBannerService offerBannerService;

    public OfferBannerController(OfferBannerService offerBannerService) {
        this.offerBannerService = offerBannerService;
    }

    @GetMapping("/offers/active")
    public ResponseEntity<ApiResponse<List<Offer>>> getActiveOffers() {
        List<Offer> offers = offerBannerService.getActiveOffersForCustomers();
        return ResponseEntity.ok(ApiResponse.ok(offers));
    }

    @GetMapping("/banners/active")
    public ResponseEntity<ApiResponse<List<PromotionalBanner>>> getActiveBanners() {
        List<PromotionalBanner> banners = offerBannerService.getActiveBannersForCustomers();
        return ResponseEntity.ok(ApiResponse.ok(banners));
    }
}
