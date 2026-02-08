package com.vn.nganhang_pt.service;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.vn.nganhang_pt.config.FragmentConfig;
import com.vn.nganhang_pt.config.PdfConfig;
import com.vn.nganhang_pt.model.GiaoDich;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BaoCaoService {

        @Autowired
        private FragmentConfig fragmentConfig;

        @Autowired
        private PdfConfig pdfConfig;

        /**
         * Lấy sao kê giao dịch cho tài khoản
         */
        public List<GiaoDich> saoKeGiaoDich(String soTK, LocalDate tuNgay, LocalDate denNgay,
                        String tenServer, String username, String password, String role) {
                List<GiaoDich> danhSach = new ArrayList<>();
                String connectionString = fragmentConfig.getConnectionString(tenServer);

                // Cả 2 role đều dùng SP_XemSKKHACHHANG
                String spName = "{CALL SP_XemSKKHACHHANG(?, ?, ?)}";

                try (Connection conn = DriverManager.getConnection(connectionString, username, password);
                                CallableStatement stmt = conn.prepareCall(spName)) {

                        stmt.setString(1, soTK);
                        stmt.setDate(2, Date.valueOf(tuNgay));
                        stmt.setDate(3, Date.valueOf(denNgay));

                        try (ResultSet rs = stmt.executeQuery()) {
                                while (rs.next()) {
                                        GiaoDich gd = new GiaoDich();
                                        gd.setSoDuDau(rs.getBigDecimal("Số dư đầu"));
                                        gd.setNgay(rs.getTimestamp("Ngày") != null
                                                        ? rs.getTimestamp("Ngày").toLocalDateTime()
                                                        : null);
                                        gd.setLoaiGiaoDich(rs.getString("Loại giao dịch"));
                                        gd.setSoTien(rs.getBigDecimal("Số tiền"));
                                        gd.setSoDuSau(rs.getBigDecimal("Số dư sau"));
                                        danhSach.add(gd);
                                }
                        }
                } catch (SQLException e) {
                        System.err.println("[ERROR] Lỗi lấy sao kê: " + e.getMessage());
                        throw new RuntimeException("Không thể lấy sao kê giao dịch: " + e.getMessage());
                }

                return danhSach;
        }

        /**
         * Xuất PDF sao kê giao dịch
         */
        public byte[] xuatPDFSaoKe(String soTK, LocalDate tuNgay, LocalDate denNgay,
                        String tenServer, String username, String password,
                        String chuTK, String chiNhanh, String nguoiXuat, String role) throws Exception {

                // Lấy dữ liệu giao dịch với role tương ứng
                List<GiaoDich> danhSach = saoKeGiaoDich(soTK, tuNgay, denNgay, tenServer, username, password, role);

                // Tính số dư đầu và cuối kỳ
                java.math.BigDecimal soDuDau = danhSach.isEmpty() ? java.math.BigDecimal.ZERO
                                : danhSach.get(0).getSoDuDau();
                java.math.BigDecimal soDuCuoi = danhSach.isEmpty() ? java.math.BigDecimal.ZERO
                                : danhSach.get(danhSach.size() - 1).getSoDuSau();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                // Lấy font từ PdfConfig (hỗ trợ tiếng Việt)
                PdfFont font = pdfConfig.getRegularFont();
                PdfFont fontBold = pdfConfig.getBoldFont();

                // Header chính
                Paragraph title = new Paragraph("SAO KÊ TÀI KHOẢN")
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_HEADER)
                                .setBold()
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(5);
                document.add(title);

                // Tên ngân hàng
                Paragraph bankName = new Paragraph("NGÂN HÀNG PTIT")
                                .setFont(font)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(10);
                document.add(bankName);

                // Khoảng cách
                document.add(new Paragraph("\n").setFontSize(5));

                // Thông tin tài khoản
                Paragraph chuTKPara = new Paragraph("Chủ tài khoản: " + chuTK)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setMarginTop(2)
                                .setMarginBottom(2);
                document.add(chuTKPara);

                Paragraph soTKPara = new Paragraph("Số tài khoản: " + soTK)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setMarginTop(2)
                                .setMarginBottom(2);
                document.add(soTKPara);

                Paragraph chiNhanhPara = new Paragraph("Chi nhánh: " + chiNhanh)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setMarginTop(2)
                                .setMarginBottom(2);
                document.add(chiNhanhPara);

                // Khoảng cách lớn
                document.add(new Paragraph("\n").setFontSize(10));

                // Số dư đầu và cuối kỳ
                Paragraph soDuDauNgay = new Paragraph(
                                "Số dư đầu ngày " + pdfConfig.formatDate(tuNgay) + ": "
                                                + pdfConfig.formatCurrency(soDuDau))
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setBold()
                                .setMarginTop(2)
                                .setMarginBottom(2);
                document.add(soDuDauNgay);

                Paragraph soDuCuoiNgay = new Paragraph(
                                "Số dư đến cuối ngày " + pdfConfig.formatDate(denNgay) + ": "
                                                + pdfConfig.formatCurrency(soDuCuoi))
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setBold()
                                .setMarginTop(2)
                                .setMarginBottom(10);
                document.add(soDuCuoiNgay);

                // Tiêu đề bảng
                Paragraph sectionTitle = new Paragraph("Chi tiết giao dịch:")
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_SECTION)
                                .setBold()
                                .setMarginTop(8)
                                .setMarginBottom(8);
                document.add(sectionTitle);

                // Tạo bảng với 6 cột
                float[] columnWidths = { 1, 3.5f, 2.5f, 2.5f, 2.8f, 2.8f };
                Table table = new Table(UnitValue.createPercentArray(columnWidths));
                table.setWidth(UnitValue.createPercentValue(100));

                // Header với background xanh đậm
                String[] headers = { "STT", "Ngày giao dịch", "Loại giao dịch", "Số tiền", "Số dư đầu", "Số dư sau" };
                for (String header : headers) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header).setFont(fontBold).setBold()
                                                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                        .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(30, 58, 138))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setPadding(5);
                        table.addHeaderCell(cell);
                }

                // Dữ liệu - sắp xếp theo ngày mới nhất trước
                SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                List<GiaoDich> sortedList = new ArrayList<>(danhSach);
                sortedList.sort((a, b) -> b.getNgay().compareTo(a.getNgay()));

                int stt = 1;
                for (GiaoDich gd : sortedList) {
                        // STT
                        table.addCell(new Cell()
                                        .add(new Paragraph(String.valueOf(stt++)).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setTextAlignment(TextAlignment.CENTER).setPadding(5));

                        // Ngày giao dịch
                        String ngayGD = gd.getNgay() != null
                                        ? sdfDateTime.format(java.sql.Timestamp.valueOf(gd.getNgay()))
                                        : "";
                        table.addCell(
                                        new Cell().add(new Paragraph(ngayGD).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                        .setPadding(5));

                        // Loại giao dịch
                        table.addCell(new Cell()
                                        .add(new Paragraph(gd.getLoaiGiaoDich()).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setPadding(5));

                        // Số tiền
                        table.addCell(new Cell()
                                        .add(new Paragraph(pdfConfig.formatCurrency(gd.getSoTien())).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setTextAlignment(TextAlignment.RIGHT).setPadding(5));

                        // Số dư đầu
                        table.addCell(new Cell()
                                        .add(new Paragraph(pdfConfig.formatCurrency(gd.getSoDuDau())).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setTextAlignment(TextAlignment.RIGHT).setPadding(5));

                        // Số dư sau
                        table.addCell(new Cell()
                                        .add(new Paragraph(pdfConfig.formatCurrency(gd.getSoDuSau())).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setTextAlignment(TextAlignment.RIGHT).setPadding(5));
                }

                document.add(table);

                // Footer
                document.add(new Paragraph("\n").setFontSize(15));

                Paragraph nguoiInPara = new Paragraph("Người in sao kê: " + nguoiXuat)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_FOOTER)
                                .setItalic()
                                .setMarginTop(2);
                document.add(nguoiInPara);

                String ngayGioIn = pdfConfig.formatDateTime(java.time.LocalDateTime.now());
                Paragraph ngayGioPara = new Paragraph("Ngày giờ in: " + ngayGioIn)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_FOOTER)
                                .setItalic()
                                .setMarginTop(2);
                document.add(ngayGioPara);

                document.close();
                return baos.toByteArray();
        }

        /**
         * Liệt kê tài khoản mở mới (DEPRECATED - Đã bị thay thế bởi
         * lietKeTaiKhoanMoiTheoChiNhanh)
         * SP_LietKeTaiKhoanMoiMo KHÔNG TỒN TẠI - Chỉ có
         * SP_LietKeTaiKhoanMoiMo_TheoChiNhanh
         * 
         * @deprecated Sử dụng lietKeTaiKhoanMoiTheoChiNhanh thay thế
         */
        @Deprecated
        public List<Map<String, Object>> lietKeTaiKhoanMoi(LocalDate tuNgay, LocalDate denNgay,
                        String role, String maCN,
                        String tenServer, String username, String password) {
                throw new UnsupportedOperationException(
                                "SP_LietKeTaiKhoanMoiMo không tồn tại. Sử dụng lietKeTaiKhoanMoiTheoChiNhanh thay thế.");
        }

        /**
         * Liệt kê tài khoản mới theo chi nhánh (dùng SP mới)
         */
        public List<Map<String, Object>> lietKeTaiKhoanMoiTheoChiNhanh(LocalDate tuNgay, LocalDate denNgay,
                        String maCN, String tenServer, String username, String password) {
                List<Map<String, Object>> danhSach = new ArrayList<>();
                String connectionString = fragmentConfig.getConnectionString(tenServer);

                try (Connection conn = DriverManager.getConnection(connectionString, username, password);
                                CallableStatement stmt = conn
                                                .prepareCall("{CALL SP_LietKeTaiKhoanMoiMo_TheoChiNhanh(?, ?, ?)}")) {

                        String tenChiNhanh = layTenChiNhanhTheoMaCN(conn, maCN);

                        stmt.setString(1, maCN);
                        stmt.setDate(2, Date.valueOf(tuNgay));
                        stmt.setDate(3, Date.valueOf(denNgay));

                        try (ResultSet rs = stmt.executeQuery()) {
                                while (rs.next()) {
                                        Map<String, Object> row = new HashMap<>();
                                        row.put("SOTK", rs.getString("SOTK"));
                                        row.put("CMND", rs.getString("CMND"));
                                        row.put("HOTENKH", rs.getString("HoTenKH"));
                                        row.put("SODU", rs.getBigDecimal("SODU"));
                                        row.put("NGAYDK", rs.getDate("NgayMoTK"));
                                        row.put("TENCHINHANH",
                                                        tenChiNhanh != null ? tenChiNhanh : rs.getString("ChiNhanh"));
                                        danhSach.add(row);
                                }
                        }
                } catch (SQLException e) {
                        System.err.println("[ERROR] Lỗi liệt kê tài khoản mới theo chi nhánh: " + e.getMessage());
                        throw new RuntimeException("Không thể lấy danh sách tài khoản mới: " + e.getMessage());
                }

                return danhSach;
        }

        private String layTenChiNhanhTheoMaCN(Connection conn, String maCN) {
                if (maCN == null || maCN.isBlank()) {
                        return null;
                }

                String tenChiNhanh = null;

                try (PreparedStatement stmt = conn.prepareStatement("SELECT TENCN FROM dbo.ChiNhanh WHERE MaCN = ?")) {
                        stmt.setString(1, maCN);
                        try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                        tenChiNhanh = rs.getString("TENCN");
                                }
                        }
                } catch (SQLException e) {
                        System.err.println("[WARN] Không lấy được TENCN local: " + e.getMessage());
                }

                if (tenChiNhanh != null) {
                        return tenChiNhanh;
                }

                try (PreparedStatement stmt = conn
                                .prepareStatement("SELECT TENCN FROM LINK1.NGANHANG.dbo.ChiNhanh WHERE MaCN = ?")) {
                        stmt.setString(1, maCN);
                        try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                        tenChiNhanh = rs.getString("TENCN");
                                }
                        }
                } catch (SQLException e) {
                        System.err.println("[WARN] Không lấy được TENCN LINK1: " + e.getMessage());
                }

                return tenChiNhanh;
        }

        /**
         * Xuất PDF danh sách tài khoản mở mới theo chi nhánh
         */
        public byte[] xuatPDFTaiKhoanMoi(LocalDate tuNgay, LocalDate denNgay, String role, String maCN,
                        String tenServer, String username, String password, String nguoiXuat) throws Exception {
                // Lấy dữ liệu - sử dụng lietKeTaiKhoanMoiTheoChiNhanh thay thế
                List<Map<String, Object>> danhSach = lietKeTaiKhoanMoiTheoChiNhanh(tuNgay, denNgay, maCN, tenServer,
                                username,
                                password);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                // Lấy font từ PdfConfig (hỗ trợ tiếng Việt)
                PdfFont font = pdfConfig.getRegularFont();
                PdfFont fontBold = pdfConfig.getBoldFont();

                // Tiêu đề chính
                Paragraph title = new Paragraph("DANH SÁCH TÀI KHOẢN MỞ MỚI")
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_HEADER)
                                .setBold()
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(5);
                document.add(title);

                // Tên ngân hàng
                Paragraph bankName = new Paragraph("NGÂN HÀNG PTIT")
                                .setFont(font)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(10);
                document.add(bankName);

                // Khoảng cách
                document.add(new Paragraph("\n").setFontSize(5));

                // Thông tin khoảng thời gian
                Paragraph soDuDauNgay = new Paragraph("Từ ngày " + pdfConfig.formatDate(tuNgay))
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setBold()
                                .setMarginTop(2)
                                .setMarginBottom(2);
                document.add(soDuDauNgay);

                Paragraph soDuCuoiNgay = new Paragraph("Đến ngày " + pdfConfig.formatDate(denNgay))
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setBold()
                                .setMarginTop(2)
                                .setMarginBottom(10);
                document.add(soDuCuoiNgay);

                // Tiêu đề bảng
                Paragraph sectionTitle = new Paragraph("Chi tiết tài khoản:")
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_SECTION)
                                .setBold()
                                .setMarginTop(8)
                                .setMarginBottom(8);
                document.add(sectionTitle);

                // Tạo bảng với 7 cột
                float[] columnWidths = { 1, 2, 2, 3, 2, 2, 2 };
                Table table = new Table(UnitValue.createPercentArray(columnWidths));
                table.setWidth(UnitValue.createPercentValue(100));

                // Header với background xanh đậm
                String[] headers = { "STT", "Số TK", "CMND", "Họ tên KH", "Số dư", "Ngày mở", "Chi nhánh" };
                for (String header : headers) {
                        Cell cell = new Cell()
                                        .add(new Paragraph(header).setFont(fontBold).setBold()
                                                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                        .setBackgroundColor(PdfConfig.HEADER_COLOR)
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setPadding(5);
                        table.addHeaderCell(cell);
                }

                // Dữ liệu
                DecimalFormat df = new DecimalFormat("#,###");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                int stt = 1;

                for (Map<String, Object> row : danhSach) {
                        // STT
                        table.addCell(new Cell()
                                        .add(new Paragraph(String.valueOf(stt++)).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setTextAlignment(TextAlignment.CENTER).setPadding(5));

                        // Số TK
                        table.addCell(new Cell()
                                        .add(new Paragraph((String) row.get("SOTK")).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setPadding(5));

                        // CMND
                        table.addCell(new Cell()
                                        .add(new Paragraph((String) row.get("CMND")).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setPadding(5));

                        // Họ tên KH
                        table.addCell(new Cell()
                                        .add(new Paragraph((String) row.get("HOTENKH")).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setPadding(5));

                        // Số dư
                        String soDu = row.get("SODU") != null ? df.format(row.get("SODU")) + " VND" : "0 VND";
                        table.addCell(new Cell()
                                        .add(new Paragraph(soDu).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setTextAlignment(TextAlignment.RIGHT).setPadding(5));

                        // Ngày mở
                        String ngayMo = row.get("NGAYDK") != null ? sdf.format(row.get("NGAYDK")) : "";
                        table.addCell(
                                        new Cell().add(new Paragraph(ngayMo).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                        .setTextAlignment(TextAlignment.CENTER).setPadding(5));

                        // Chi nhánh
                        table.addCell(new Cell()
                                        .add(new Paragraph((String) row.get("TENCHINHANH")).setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setPadding(5));
                }

                document.add(table);

                // Footer - Thông tin người xuất và ngày giờ
                document.add(new Paragraph("\n").setFontSize(15));

                Paragraph nguoiXuatPara = new Paragraph("Người xuất: " + nguoiXuat)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_FOOTER)
                                .setItalic()
                                .setMarginTop(2);
                document.add(nguoiXuatPara);

                String ngayGioXuat = pdfConfig.formatDateTime(java.time.LocalDateTime.now());
                Paragraph ngayGioPara = new Paragraph("Ngày giờ xuất: " + ngayGioXuat)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_FOOTER)
                                .setItalic()
                                .setMarginTop(2);
                document.add(ngayGioPara);

                document.close();
                return baos.toByteArray();
        }

        /**
         * Xuất PDF danh sách tài khoản mở mới theo chi nhánh
         */
        public byte[] xuatPDFTaiKhoanMoiTheoChiNhanh(LocalDate tuNgay, LocalDate denNgay, String maCN,
                        String tenServer, String username, String password, String nguoiXuat) throws Exception {
                // Lấy dữ liệu
                List<Map<String, Object>> danhSach = lietKeTaiKhoanMoiTheoChiNhanh(tuNgay, denNgay, maCN, tenServer,
                                username,
                                password);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                // Lấy font từ PdfConfig (hỗ trợ tiếng Việt)
                PdfFont font = pdfConfig.getRegularFont();
                PdfFont fontBold = pdfConfig.getBoldFont();

                // Tiêu đề chính
                Paragraph title = new Paragraph("DANH SÁCH TÀI KHOẢN Mở Mới - CHI NHÁNH " + maCN)
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_HEADER)
                                .setBold()
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(5);
                document.add(title);

                // Tên ngân hàng
                Paragraph bankName = new Paragraph("NGÂN HÀNG PTIT")
                                .setFont(font)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(10);
                document.add(bankName);

                // Khoảng cách
                document.add(new Paragraph("\n").setFontSize(5));

                // Thông tin khoảng thời gian
                Paragraph soDuDauNgay = new Paragraph("Từ ngày " + pdfConfig.formatDate(tuNgay))
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setMarginBottom(5);
                document.add(soDuDauNgay);

                Paragraph soDuCuoiNgay = new Paragraph("Đến ngày " + pdfConfig.formatDate(denNgay))
                                .setFont(fontBold)
                                .setFontSize(PdfConfig.FONT_SIZE_INFO)
                                .setMarginBottom(10);
                document.add(soDuCuoiNgay);

                // Bảng dữ liệu
                Table table = new Table(UnitValue.createPercentArray(new float[] { 5, 12, 20, 12, 15, 15, 21 }))
                                .useAllAvailableWidth();
                table.setMarginTop(10);

                // Header
                table.addHeaderCell(new Cell()
                                .add(new Paragraph("STT").setFont(fontBold)
                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setPadding(5)
                                .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell()
                                .add(new Paragraph("Số TK").setFont(fontBold)
                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setPadding(5)
                                .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell()
                                .add(new Paragraph("CMND").setFont(fontBold)
                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setPadding(5)
                                .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell()
                                .add(new Paragraph("Họ tên KH").setFont(fontBold)
                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setPadding(5)
                                .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell()
                                .add(new Paragraph("Số dư").setFont(fontBold)
                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setPadding(5)
                                .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell()
                                .add(new Paragraph("Ngày mở").setFont(fontBold)
                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setPadding(5)
                                .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Cell()
                                .add(new Paragraph("Chi nhánh").setFont(fontBold)
                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_HEADER))
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY).setPadding(5)
                                .setTextAlignment(TextAlignment.CENTER));

                // Body
                DecimalFormat df = new DecimalFormat("#,##0.00");
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                int stt = 1;
                if (danhSach.isEmpty()) {
                        table.addCell(new Cell(1, 7)
                                        .add(new Paragraph("Không có dữ liệu").setFont(font)
                                                        .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                        .setTextAlignment(TextAlignment.CENTER));
                } else {
                        for (Map<String, Object> row : danhSach) {
                                // STT
                                table.addCell(new Cell()
                                                .add(new Paragraph(String.valueOf(stt)).setFont(font)
                                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setPadding(5));
                                stt++;

                                // Số TK
                                table.addCell(new Cell()
                                                .add(new Paragraph((String) row.get("SOTK")).setFont(font)
                                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                .setPadding(5));

                                // CMND
                                table.addCell(new Cell()
                                                .add(new Paragraph((String) row.get("CMND")).setFont(font)
                                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                .setPadding(5));

                                // Họ tên KH
                                table.addCell(new Cell()
                                                .add(new Paragraph((String) row.get("HOTENKH")).setFont(font)
                                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                .setPadding(5));

                                // Số dư
                                String soDu = row.get("SODU") != null ? df.format(row.get("SODU")) + " VND" : "0 VND";
                                table.addCell(new Cell()
                                                .add(new Paragraph(soDu).setFont(font)
                                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                .setTextAlignment(TextAlignment.RIGHT).setPadding(5));

                                // Ngày mở
                                String ngayMo = row.get("NGAYDK") != null ? sdf.format(row.get("NGAYDK")) : "";
                                table.addCell(
                                                new Cell().add(new Paragraph(ngayMo).setFont(font)
                                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                                .setTextAlignment(TextAlignment.CENTER).setPadding(5));

                                // Chi nhánh
                                table.addCell(new Cell()
                                                .add(new Paragraph((String) row.get("TENCHINHANH")).setFont(font)
                                                                .setFontSize(PdfConfig.FONT_SIZE_TABLE_BODY))
                                                .setPadding(5));
                        }
                }

                document.add(table);

                // Footer - Thông tin người xuất và ngày giờ
                document.add(new Paragraph("\n").setFontSize(15));

                Paragraph nguoiXuatPara = new Paragraph("Người xuất: " + nguoiXuat)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_FOOTER)
                                .setItalic()
                                .setMarginTop(2);
                document.add(nguoiXuatPara);

                String ngayGioXuat = pdfConfig.formatDateTime(java.time.LocalDateTime.now());
                Paragraph ngayGioPara = new Paragraph("Ngày giờ xuất: " + ngayGioXuat)
                                .setFont(font)
                                .setFontSize(PdfConfig.FONT_SIZE_FOOTER)
                                .setItalic()
                                .setMarginTop(2);
                document.add(ngayGioPara);

                document.close();
                return baos.toByteArray();
        }
}
