package com.vn.nganhang_pt.config;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Cấu hình chung cho việc tạo PDF
 * Quản lý font và màu sắc để hỗ trợ tiếng Việt
 */
@Component
public class PdfConfig {

    // Màu xanh header (giống customer dashboard)
    public static final Color HEADER_COLOR = new DeviceRgb(30, 58, 138);

    // Font sizes - giống customer dashboard (pdfmake)
    public static final float FONT_SIZE_HEADER = 18f; // Tiêu đề chính (SAO KÊ TÀI KHOẢN)
    public static final float FONT_SIZE_SUBHEADER = 12f; // Phụ đề (NGÂN HÀNG PTIT)
    public static final float FONT_SIZE_INFO = 11f; // Thông tin (Chủ tài khoản, Số TK...)
    public static final float FONT_SIZE_SECTION = 12f; // Tiêu đề section (Chi tiết giao dịch)
    public static final float FONT_SIZE_TABLE_HEADER = 10f; // Header bảng
    public static final float FONT_SIZE_TABLE_BODY = 9f; // Nội dung bảng
    public static final float FONT_SIZE_FOOTER = 10f; // Footer (Người in, Ngày giờ in)

    // Font paths - ưu tiên font hệ thống Windows
    private static final String ARIAL_REGULAR = "C:/Windows/Fonts/arial.ttf";
    private static final String ARIAL_BOLD = "C:/Windows/Fonts/arialbd.ttf";

    /**
     * Lấy font chữ thường hỗ trợ tiếng Việt
     */
    public PdfFont getRegularFont() throws IOException {
        try {
            // Thử dùng Arial từ Windows (hỗ trợ Unicode đầy đủ)
            return PdfFontFactory.createFont(ARIAL_REGULAR, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        } catch (Exception e) {
            System.err.println("[WARN] Không tìm thấy Arial font, sử dụng Helvetica mặc định");
            // Fallback: Helvetica (chỉ hỗ trợ ASCII)
            return PdfFontFactory.createFont();
        }
    }

    /**
     * Lấy font chữ đậm hỗ trợ tiếng Việt
     */
    public PdfFont getBoldFont() throws IOException {
        try {
            // Thử dùng Arial Bold từ Windows
            return PdfFontFactory.createFont(ARIAL_BOLD, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
        } catch (Exception e) {
            System.err.println("[WARN] Không tìm thấy Arial Bold font, sử dụng Helvetica Bold mặc định");
            // Fallback: Helvetica Bold
            return PdfFontFactory.createFont();
        }
    }

    /**
     * Kiểm tra xem có thể sử dụng font tiếng Việt không
     */
    public boolean isVietnameseFontAvailable() {
        try {
            PdfFontFactory.createFont(ARIAL_REGULAR, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Format số tiền theo định dạng Việt Nam
     */
    public String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null)
            return "0 VND";
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(amount) + " VND";
    }

    /**
     * Format ngày giờ theo định dạng Việt Nam
     */
    public String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null)
            return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Format ngày theo định dạng Việt Nam
     */
    public String formatDate(java.time.LocalDate date) {
        if (date == null)
            return "";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }
}
