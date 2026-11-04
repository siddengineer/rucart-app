package com.rucart.backend.service;

import com.rucart.backend.model.Offer;
import com.rucart.backend.model.PromotionalBanner;
import com.rucart.backend.repository.OfferRepository;
import com.rucart.backend.repository.PromotionalBannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OfferBannerService {

    private final OfferRepository offerRepository;
    private final PromotionalBannerRepository bannerRepository;

    public OfferBannerService(OfferRepository offerRepository, PromotionalBannerRepository bannerRepository) {
        this.offerRepository = offerRepository;
        this.bannerRepository = bannerRepository;
    }

    // --- Offers ---

    public List<Offer> getAllOffers() {
        return offerRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Offer> getActiveOffersForCustomers() {
        LocalDate today = LocalDate.now();
        return offerRepository.findByActiveTrueAndExpiryDateGreaterThanEqualOrderByExpiryDateAsc(today);
    }

    @Transactional
    public Offer createOffer(Offer offer) {
        if (offer.getCode() == null || offer.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Offer code is required");
        }
        offer.setCode(offer.getCode().trim().toUpperCase());
        offer.setCreatedAt(LocalDateTime.now());
        offer.setUpdatedAt(LocalDateTime.now());
        return offerRepository.save(offer);
    }

    @Transactional
    public Offer updateOffer(Long id, Offer update) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with id: " + id));
        if (update.getTitle() != null) offer.setTitle(update.getTitle());
        if (update.getDescription() != null) offer.setDescription(update.getDescription());
        if (update.getCode() != null) offer.setCode(update.getCode().trim().toUpperCase());
        if (update.getDiscountType() != null) offer.setDiscountType(update.getDiscountType());
        if (update.getDiscountValue() != null) offer.setDiscountValue(update.getDiscountValue());
        if (update.getMinOrderAmount() != null) offer.setMinOrderAmount(update.getMinOrderAmount());
        if (update.getStartDate() != null) offer.setStartDate(update.getStartDate());
        if (update.getExpiryDate() != null) offer.setExpiryDate(update.getExpiryDate());
        offer.setActive(update.isActive());
        offer.setUpdatedAt(LocalDateTime.now());
        return offerRepository.save(offer);
    }

    @Transactional
    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }

    // --- Promotional Banners ---

    public List<PromotionalBanner> getAllBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    public List<PromotionalBanner> getActiveBannersForCustomers() {
        return bannerRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    @Transactional
    public PromotionalBanner createBanner(PromotionalBanner banner) {
        if (banner.getTitle() == null || banner.getImageUrl() == null) {
            throw new IllegalArgumentException("Banner title and image URL are required");
        }
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        return bannerRepository.save(banner);
    }

    @Transactional
    public PromotionalBanner updateBanner(Long id, PromotionalBanner update) {
        PromotionalBanner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found with id: " + id));
        if (update.getTitle() != null) banner.setTitle(update.getTitle());
        if (update.getImageUrl() != null) banner.setImageUrl(update.getImageUrl());
        if (update.getTargetType() != null) banner.setTargetType(update.getTargetType());
        if (update.getTargetValue() != null) banner.setTargetValue(update.getTargetValue());
        banner.setDisplayOrder(update.getDisplayOrder());
        banner.setActive(update.isActive());
        banner.setUpdatedAt(LocalDateTime.now());
        return bannerRepository.save(banner);
    }

    @Transactional
    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
    }
}
