package com.rucart.backend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rucart.backend.model.Order;
import com.rucart.backend.model.OrderItem;
import com.rucart.backend.model.Payment;
import com.rucart.backend.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiptPdfService {

    private static final Color BRAND_GREEN = new Color(46, 125, 50);
    private static final Color DARK_TEXT = new Color(33, 33, 33);
    private static final Color LIGHT_BG = new Color(245, 245, 245);
    private static final Color BORDER_GRAY = new Color(224, 224, 224);
    private static final Color ALERT_ORANGE = new Color(230, 81, 0);

    public byte[] generateReceiptPdf(Order order, Payment payment) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND_GREEN);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND_GREEN);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            // 1. Header Banner Table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60, 40});

            PdfPCell brandCell = new PdfPCell();
            brandCell.setBorder(Rectangle.NO_BORDER);
            brandCell.addElement(new Paragraph("RUCART", titleFont));
            brandCell.addElement(new Paragraph("Fresh & Fast Grocery Delivery Platform", subtitleFont));
            headerTable.addCell(brandCell);

            PdfPCell metaCell = new PdfPCell();
            metaCell.setBorder(Rectangle.NO_BORDER);
            metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph metaPara = new Paragraph();
            metaPara.setAlignment(Element.ALIGN_RIGHT);
            metaPara.add(new Chunk("TAX INVOICE / RECEIPT\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, DARK_TEXT)));
            metaPara.add(new Chunk("Receipt #: " + payment.getReceiptNumber() + "\n", boldFont));
            metaPara.add(new Chunk("Order #: " + order.getOrderNumber() + "\n", boldFont));
            String dateStr = order.getCreatedAt() != null ? order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")) : "N/A";
            metaPara.add(new Chunk("Date: " + dateStr, normalFont));
            metaCell.addElement(metaPara);
            headerTable.addCell(metaCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // 2. Status Banner (PAID vs PAYABLE ON DELIVERY)
            PdfPTable statusBanner = new PdfPTable(1);
            statusBanner.setWidthPercentage(100);
            PdfPCell statusCell = new PdfPCell();
            statusCell.setPadding(8);

            boolean isPaid = payment.getPaymentStatus() == PaymentStatus.COMPLETED;
            if (isPaid) {
                statusCell.setBackgroundColor(new Color(232, 245, 233));
                Paragraph statusPara = new Paragraph("✓ PAYMENT STATUS: COMPLETED (PAID IN FULL)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_GREEN));
                statusPara.setAlignment(Element.ALIGN_CENTER);
                statusCell.addElement(statusPara);
            } else {
                statusCell.setBackgroundColor(new Color(255, 243, 224));
                Paragraph statusPara = new Paragraph("PAYMENT STATUS: " + payment.getPaymentStatus().name() + " — AMOUNT DUE ON DELIVERY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, ALERT_ORANGE));
                statusPara.setAlignment(Element.ALIGN_CENTER);
                statusCell.addElement(statusPara);
            }
            statusBanner.addCell(statusCell);
            document.add(statusBanner);
            document.add(new Paragraph(" "));

            // 3. Store & Customer Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{50, 50});

            // Store Info
            PdfPCell storeCell = new PdfPCell();
            storeCell.setPadding(10);
            storeCell.setBorderColor(BORDER_GRAY);
            storeCell.setBackgroundColor(LIGHT_BG);
            storeCell.addElement(new Paragraph("FULFILLING STORE", sectionHeaderFont));
            storeCell.addElement(new Paragraph(order.getStoreName(), boldFont));
            if (order.getStore() != null) {
                storeCell.addElement(new Paragraph(order.getStore().getAddress(), normalFont));
                storeCell.addElement(new Paragraph("City: " + order.getStore().getCity() + " | Phone: " + order.getStore().getPhone(), normalFont));
            }
            infoTable.addCell(storeCell);

            // Customer Info
            PdfPCell custCell = new PdfPCell();
            custCell.setPadding(10);
            custCell.setBorderColor(BORDER_GRAY);
            custCell.setBackgroundColor(LIGHT_BG);
            custCell.addElement(new Paragraph("DELIVER TO CUSTOMER", sectionHeaderFont));
            custCell.addElement(new Paragraph(order.getCustomerName() != null ? order.getCustomerName() : "Valued Customer", boldFont));
            custCell.addElement(new Paragraph("Phone: " + (order.getCustomerPhone() != null ? order.getCustomerPhone() : "N/A"), normalFont));
            custCell.addElement(new Paragraph("Address: " + (order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "N/A"), normalFont));
            infoTable.addCell(custCell);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // 4. Products Table
            PdfPTable itemTable = new PdfPTable(5);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{45, 15, 12, 14, 14});

            // Table Header
            String[] headers = {"Item Description", "Unit", "Qty", "Unit Price", "Total (INR)"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(BRAND_GREEN);
                cell.setPadding(6);
                cell.setHorizontalAlignment(h.contains("Price") || h.contains("Total") || h.contains("Qty") ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                itemTable.addCell(cell);
            }

            // Table Rows
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    String pName = item.getProductName() != null ? item.getProductName()
                            : (item.getProduct() != null ? item.getProduct().getName() : "Grocery Item");

                    PdfPCell nameCell = new PdfPCell(new Phrase(pName, normalFont));
                    nameCell.setPadding(6);
                    nameCell.setBorderColor(BORDER_GRAY);
                    itemTable.addCell(nameCell);

                    PdfPCell unitCell = new PdfPCell(new Phrase(item.getUnit() != null ? item.getUnit() : "1 unit", normalFont));
                    unitCell.setPadding(6);
                    unitCell.setBorderColor(BORDER_GRAY);
                    itemTable.addCell(unitCell);

                    PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                    qtyCell.setPadding(6);
                    qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    qtyCell.setBorderColor(BORDER_GRAY);
                    itemTable.addCell(qtyCell);

                    PdfPCell priceCell = new PdfPCell(new Phrase(String.format("%.2f", item.getUnitPrice()), normalFont));
                    priceCell.setPadding(6);
                    priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    priceCell.setBorderColor(BORDER_GRAY);
                    itemTable.addCell(priceCell);

                    PdfPCell totCell = new PdfPCell(new Phrase(String.format("%.2f", item.getTotalPrice()), boldFont));
                    totCell.setPadding(6);
                    totCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totCell.setBorderColor(BORDER_GRAY);
                    itemTable.addCell(totCell);
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Standard Grocery Package", normalFont));
                emptyCell.setColspan(5);
                emptyCell.setPadding(8);
                itemTable.addCell(emptyCell);
            }

            document.add(itemTable);
            document.add(new Paragraph(" "));

            // 5. Financial Totals & Payment Summary
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(100);
            summaryTable.setWidths(new float[]{55, 45});

            // Payment metadata
            PdfPCell payMetaCell = new PdfPCell();
            payMetaCell.setPadding(8);
            payMetaCell.setBorderColor(BORDER_GRAY);
            payMetaCell.addElement(new Paragraph("PAYMENT METHOD & REFERENCE", sectionHeaderFont));
            payMetaCell.addElement(new Paragraph("Method: " + payment.getPaymentMethod().name().replace('_', ' '), boldFont));
            payMetaCell.addElement(new Paragraph("Transaction ID: " + payment.getTransactionId(), normalFont));
            if (payment.getGatewayPaymentId() != null) {
                payMetaCell.addElement(new Paragraph("Gateway Ref: " + payment.getGatewayPaymentId(), normalFont));
            }
            if (payment.isSandbox()) {
                payMetaCell.addElement(new Paragraph("[TEST / SANDBOX TRANSACTION]", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, ALERT_ORANGE)));
            }
            summaryTable.addCell(payMetaCell);

            // Financial amounts
            PdfPCell totalsCell = new PdfPCell();
            totalsCell.setPadding(8);
            totalsCell.setBorderColor(BORDER_GRAY);

            BigDecimal subtotal = order.getItemTotal() != null ? order.getItemTotal() : payment.getAmount();
            BigDecimal deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.ZERO;
            BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
            BigDecimal grandTotal = order.getGrandTotal() != null ? order.getGrandTotal() : payment.getAmount();

            Paragraph pTotals = new Paragraph();
            pTotals.setAlignment(Element.ALIGN_RIGHT);
            pTotals.add(new Chunk("Item Subtotal:  INR " + String.format("%.2f", subtotal) + "\n", normalFont));
            pTotals.add(new Chunk("Delivery Charges:  INR " + String.format("%.2f", deliveryFee) + "\n", normalFont));
            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                pTotals.add(new Chunk("Discount: -INR " + String.format("%.2f", discount) + "\n", normalFont));
            }
            pTotals.add(new Chunk("----------------------------------------\n", normalFont));
            pTotals.add(new Chunk("FINAL AMOUNT:  INR " + String.format("%.2f", grandTotal) + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND_GREEN)));
            totalsCell.addElement(pTotals);
            summaryTable.addCell(totalsCell);

            document.add(summaryTable);
            document.add(new Paragraph(" "));

            // 6. Footer Disclaimer
            Paragraph footer = new Paragraph(
                    "This is an electronically generated official receipt issued by RUCART.\n" +
                            "For customer queries, support, or return/refund requests, please contact support@rucart.com.",
                    subtitleFont
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF receipt for order " + order.getOrderNumber(), e);
        }
    }
}
