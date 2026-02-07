package com.vn.nganhang_pt.service;

import com.vn.nganhang_pt.config.FragmentConfig;
import com.vn.nganhang_pt.model.KhachHang;
import com.vn.nganhang_pt.model.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KhachHangService {

    @Autowired
    private FragmentConfig fragmentConfig;

    /**
     * Lấy danh sách khách hàng (chỉ chi nhánh hiện tại)
     * Dùng cho role CHINHANH
     */
    public List<KhachHang> layDanhSachKhachHang(String tenServer) {
        List<KhachHang> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.SP_Lay_DS_KhachHang}";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    KhachHang kh = new KhachHang();
                    kh.setCmnd(rs.getString("CMND"));
                    kh.setHo(rs.getString("HO"));
                    kh.setTen(rs.getString("TEN"));
                    kh.setHoten(rs.getString("HOTEN"));
                    kh.setDiaChi(rs.getString("DIACHI"));
                    kh.setPhai(rs.getString("PHAI"));
                    kh.setNgayCap(rs.getDate("NGAYCAP"));
                    kh.setSoDT(rs.getString("SODT"));
                    kh.setMaCN(rs.getString("MACN"));
                    kh.setTenChiNhanh(rs.getString("TENCN"));
                    danhSach.add(kh);
                }
                System.out.println("[DEBUG] Tìm thấy " + danhSach.size() + " khách hàng (chi nhánh hiện tại)");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    /**
     * Lấy danh sách khách hàng theo chi nhánh (cho role NGANHANG)
     * Sử dụng SP_Lay_DS_KhachHang_TheoChiNhanh để lấy từ 2 site
     */
    public List<KhachHang> layDanhSachKhachHangTheoChiNhanh(String tenServer, String maCN, String username,
            String password) {
        List<KhachHang> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.SP_Lay_DS_KhachHang_TheoChiNhanh(?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, maCN);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        KhachHang kh = new KhachHang();
                        kh.setCmnd(rs.getString("CMND"));
                        kh.setHo(rs.getString("HO"));
                        kh.setTen(rs.getString("TEN"));
                        kh.setHoten(rs.getString("HOTEN"));
                        kh.setDiaChi(rs.getString("DIACHI"));
                        kh.setPhai(rs.getString("PHAI"));
                        kh.setNgayCap(rs.getDate("NGAYCAP"));
                        kh.setSoDT(rs.getString("SODT"));
                        kh.setMaCN(rs.getString("MACN"));
                        kh.setTenChiNhanh(rs.getString("TENCN"));
                        danhSach.add(kh);
                    }
                    System.out.println("[DEBUG] Tìm thấy " + danhSach.size() + " khách hàng cho chi nhánh " + maCN);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách khách hàng theo chi nhánh: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    /**
     * Lấy danh sách khách hàng tất cả chi nhánh
     * Dùng cho role NGANHANG
     */
    public List<KhachHang> layDanhSachKhachHangTatCaCN(String tenServer) {
        List<KhachHang> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.SP_LietKeKHtheoCN}";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    KhachHang kh = new KhachHang();
                    kh.setTenChiNhanh(rs.getString("ChiNhanh"));
                    kh.setCmnd(rs.getString("CMND"));
                    kh.setHo(rs.getString("HO"));
                    kh.setTen(rs.getString("TEN"));
                    kh.setHoten(rs.getString("HoTenDayDu"));
                    kh.setDiaChi(rs.getString("DIACHI"));
                    kh.setPhai(rs.getString("PHAI"));
                    kh.setNgayCap(rs.getDate("NGAYCAP"));
                    kh.setSoDT(rs.getString("SODT"));
                    kh.setMaCN(rs.getString("MACN"));
                    danhSach.add(kh);
                }
                System.out.println("[DEBUG] Tìm thấy " + danhSach.size() + " khách hàng (tất cả chi nhánh)");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    /**
     * Thêm khách hàng mới
     */
    public Map<String, Object> themKhachHang(String tenServer, String cmnd, String ho, String ten,
            String diaChi, Date ngayCap, String soDT, String phai, String maCN) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.sp_ThemKhachHang(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, cmnd);
                stmt.setString(2, ho);
                stmt.setString(3, ten);
                stmt.setString(4, diaChi);
                stmt.setDate(5, ngayCap);
                stmt.setString(6, soDT);
                stmt.setString(7, phai);
                stmt.setString(8, maCN);
                stmt.registerOutParameter(9, Types.INTEGER);

                stmt.execute();
                int returnCode = stmt.getInt(9);

                result.put("result", returnCode);
                switch (returnCode) {
                    case 1:
                        result.put("message", "Thêm khách hàng thành công");
                        break;
                    case -1:
                        result.put("message", "Chi nhánh không tồn tại");
                        break;
                    case -2:
                        result.put("message", "CMND đã tồn tại");
                        break;
                    case -3:
                        result.put("message", "Giới tính không hợp lệ");
                        break;
                    default:
                        result.put("message", "Lỗi không xác định");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi thêm khách hàng: " + e.getMessage());
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        return result;
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    public Map<String, Object> capNhatKhachHang(String tenServer, String cmnd, String ho, String ten,
            String diaChi, String soDT, String phai) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.sp_CapNhatKhachHang(?, ?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, cmnd);
                stmt.setString(2, ho);
                stmt.setString(3, ten);
                stmt.setString(4, phai); // PHAI ở vị trí 4
                stmt.setString(5, diaChi);
                stmt.setString(6, soDT);
                stmt.registerOutParameter(7, Types.INTEGER);

                stmt.execute();
                int returnCode = stmt.getInt(7);

                result.put("result", returnCode);
                switch (returnCode) {
                    case 1:
                        result.put("message", "Cập nhật khách hàng thành công");
                        break;
                    case -1:
                        result.put("message", "Không tìm thấy khách hàng");
                        break;
                    case 0:
                        result.put("message", "Không có thay đổi dữ liệu");
                        break;
                    default:
                        result.put("message", "Lỗi không xác định");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi cập nhật khách hàng: " + e.getMessage());
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        return result;
    }

    /**
     * Lấy danh sách tài khoản của khách hàng
     */
    public List<TaiKhoan> layDanhSachTaiKhoanKH(String tenServer, String cmnd) {
        List<TaiKhoan> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.sp_LayDSTaiKhoan_KhachHang(?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, cmnd);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        TaiKhoan tk = new TaiKhoan();
                        tk.setSoTK(rs.getString("SOTK"));
                        tk.setSoDu(rs.getBigDecimal("SODU"));
                        tk.setNgayMoTK(rs.getDate("NGAYMOTK"));
                        tk.setMaCN(rs.getString("MACN"));
                        tk.setTenCN(rs.getString("TENCN"));
                        tk.setSite(rs.getString("SITE"));
                        danhSach.add(tk);
                    }
                    System.out.println("[DEBUG] Tìm thấy " + danhSach.size() + " tài khoản");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách tài khoản: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    /**
     * Tìm khách hàng theo CMND trên tất cả các site
     */
    public Map<String, Object> timKhachHangTheoCMND(String tenServer, String cmnd) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.sp_TimKhachHangTheoCMND(?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, cmnd);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String cmndResult = rs.getString("CMND");
                    if (cmndResult != null && !cmndResult.trim().isEmpty()) {
                        result.put("found", true);
                        result.put("cmnd", cmndResult.trim());
                        result.put("ho", rs.getString("HO") != null ? rs.getString("HO").trim() : "");
                        result.put("ten", rs.getString("TEN") != null ? rs.getString("TEN").trim() : "");
                        result.put("hoten", rs.getString("HOTEN") != null ? rs.getString("HOTEN").trim() : "");
                        result.put("maCN", rs.getString("MACN") != null ? rs.getString("MACN").trim() : "");
                        result.put("tenChiNhanh",
                                rs.getString("TENCHINHANH") != null ? rs.getString("TENCHINHANH").trim() : "");
                        result.put("message", "Tìm thấy khách hàng");
                    } else {
                        result.put("found", false);
                        result.put("message", "Không tìm thấy khách hàng với CMND: " + cmnd);
                    }
                } else {
                    result.put("found", false);
                    result.put("message", "Không tìm thấy khách hàng với CMND: " + cmnd);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi tìm khách hàng: " + e.getMessage());
            e.printStackTrace();
            result.put("found", false);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        return result;
    }

    /**
     * Mở tài khoản cho khách hàng
     */
    public Map<String, Object> moTaiKhoan(String tenServer, String soTK, String cmnd,
            String maCN, String maNV) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {

            // Set timeout để tránh treo với DISTRIBUTED TRANSACTION
            conn.setNetworkTimeout(null, 60000); // 60 seconds timeout

            String sql = "{call dbo.sp_MoTaiKhoan(?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                // Set query timeout
                stmt.setQueryTimeout(60); // 60 seconds

                stmt.setString(1, soTK);
                stmt.setString(2, cmnd);
                stmt.setString(3, maCN);
                stmt.setString(4, maNV);
                stmt.registerOutParameter(5, Types.INTEGER);

                System.out.println("[DEBUG] Executing sp_MoTaiKhoan with: soTK=" + soTK +
                        ", cmnd=" + cmnd + ", maCN=" + maCN + ", maNV=" + maNV);
                System.out.println("[DEBUG] Using DISTRIBUTED TRANSACTION - may take longer...");

                long startTime = System.currentTimeMillis();
                stmt.execute();
                long endTime = System.currentTimeMillis();

                System.out.println("[DEBUG] SP executed in " + (endTime - startTime) + "ms");

                int returnCode = stmt.getInt(5);

                System.out.println("[DEBUG] sp_MoTaiKhoan returned: " + returnCode);

                result.put("result", returnCode);
                switch (returnCode) {
                    case 1:
                        result.put("message", "Mở tài khoản thành công");
                        break;
                    case -1:
                        result.put("message", "Khách hàng không tồn tại");
                        break;
                    case -2:
                        result.put("message", "Chi nhánh không tồn tại");
                        break;
                    case -3:
                        result.put("message", "Số tài khoản đã tồn tại");
                        break;
                    case -4:
                        result.put("message", "Nhân viên không hợp lệ");
                        break;
                    default:
                        result.put("message", "Lỗi không xác định (code: " + returnCode + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] SQLException khi mở tài khoản: " + e.getMessage());
            System.err.println("[ERROR] SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            e.printStackTrace();

            // Handle RAISERROR from stored procedure
            String errorMsg = e.getMessage();

            // Check for MSDTC/Distributed Transaction errors
            if (errorMsg.contains("MSDTC") || errorMsg.contains("distributed transaction")
                    || errorMsg.contains("transaction manager") || e.getErrorCode() == 8501) {
                result.put("result", -98);
                result.put("message", "Lỗi MSDTC: Vui lòng kiểm tra dịch vụ MS DTC đã được bật và cấu hình đúng");
            } else if (errorMsg.contains("timeout") || errorMsg.contains("Timeout")) {
                result.put("result", -97);
                result.put("message", "Lỗi timeout: Distributed Transaction mất quá nhiều thời gian");
            } else if (errorMsg.contains("Khách hàng không tồn tại")) {
                result.put("result", -1);
                result.put("message", "Khách hàng không tồn tại");
            } else if (errorMsg.contains("Chi nhánh không tồn tại")) {
                result.put("result", -2);
                result.put("message", "Chi nhánh không tồn tại");
            } else if (errorMsg.contains("Số tài khoản đã tồn tại")) {
                result.put("result", -3);
                result.put("message", "Số tài khoản đã tồn tại");
            } else if (errorMsg.contains("Nhân viên không hợp lệ")) {
                result.put("result", -4);
                result.put("message", "Nhân viên không hợp lệ");
            } else {
                result.put("result", -99);
                result.put("message", "Lỗi SQL: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi mở tài khoản: " + e.getMessage());
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        return result;
    }

    /**
     * Xóa khách hàng (gọi SP_XoaKhachHang)
     */
    public Map<String, Object> xoaKhachHang(String tenServer, String cmnd) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.SP_XoaKhachHang(?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, cmnd);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int errorCode = rs.getInt("ErrorCode");
                        String message = rs.getString("ThongBao");
                        result.put("result", errorCode);
                        result.put("message", message);
                        System.out.println("[DEBUG] Xóa khách hàng " + cmnd + ": " + message);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi xóa khách hàng: " + e.getMessage());
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        return result;
    }

    /**
     * Phục hồi khách hàng (gọi sp_ThemKhachHang để tạo lại khách hàng)
     */
    public Map<String, Object> phucHoiKhachHang(String tenServer, KhachHang kh) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            // Disable auto-commit to handle DISTRIBUTED TRANSACTION properly
            conn.setAutoCommit(false);

            String sql = "{call dbo.sp_ThemKhachHang(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, kh.getCmnd());
                stmt.setString(2, kh.getHo());
                stmt.setString(3, kh.getTen());
                stmt.setString(4, kh.getDiaChi());
                stmt.setDate(5, new java.sql.Date(kh.getNgayCap().getTime()));
                stmt.setString(6, kh.getSoDT());
                stmt.setString(7, kh.getPhai());
                stmt.setString(8, kh.getMacn());
                stmt.registerOutParameter(9, Types.INTEGER);

                System.out.println("[DEBUG] Phục hồi khách hàng bằng sp_ThemKhachHang - CMND: " + kh.getCmnd()
                        + ", Họ tên: " + kh.getHo() + " " + kh.getTen());
                System.out.println("[DEBUG] Params: phai=" + kh.getPhai() + ", macn=" + kh.getMacn() + ", ngaycap="
                        + kh.getNgayCap());

                stmt.execute();
                int returnCode = stmt.getInt(9);

                result.put("result", returnCode);
                switch (returnCode) {
                    case 1:
                        result.put("message", "Phục hồi khách hàng thành công");
                        break;
                    case -1:
                        result.put("message", "Chi nhánh không tồn tại");
                        break;
                    case -2:
                        result.put("message", "CMND đã tồn tại, không thể phục hồi");
                        break;
                    case -3:
                        result.put("message", "Giới tính không hợp lệ");
                        break;
                    case -99:
                        result.put("message", "Lỗi hệ thống");
                        break;
                    default:
                        result.put("message", "Lỗi không xác định (code: " + returnCode + ")");
                }
                System.out.println("[DEBUG] Phục hồi khách hàng " + kh.getCmnd() + ": " + result.get("message"));

                // Commit if successful (SP handles its own DISTRIBUTED TRANSACTION)
                conn.commit();

            } catch (SQLException e) {
                System.err.println("[ERROR] SQLException during SP execution: " + e.getMessage());
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("[ERROR] Rollback failed: " + rollbackEx.getMessage());
                }

                result.put("result", -99);
                result.put("message", "Lỗi SQL: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi phục hồi khách hàng: " + e.getMessage());
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        return result;
    }

    /**
     * Tạo lại tài khoản (gọi SP_TaoLaiTaiKhoan)
     */
    public Map<String, Object> taoLaiTaiKhoan(String tenServer, String soTK, String cmnd, String maCN,
            java.util.Date ngayMoTK, double soDu) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.SP_TaoLaiTaiKhoan(?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, soTK);
                stmt.setString(2, cmnd);
                stmt.setString(3, maCN);
                stmt.setDate(4, new java.sql.Date(ngayMoTK.getTime()));
                stmt.setDouble(5, soDu);
                stmt.registerOutParameter(6, Types.INTEGER);

                stmt.execute();
                int returnCode = stmt.getInt(6);

                result.put("result", returnCode);
                switch (returnCode) {
                    case 1:
                        result.put("message", "Tạo lại tài khoản thành công");
                        break;
                    case -1:
                        result.put("message", "Số tài khoản đã tồn tại");
                        break;
                    case -2:
                        result.put("message", "Khách hàng không tồn tại (phải phục hồi khách hàng trước)");
                        break;
                    case -3:
                        result.put("message", "Chi nhánh không tồn tại");
                        break;
                    case -4:
                        result.put("message", "Số dư không hợp lệ");
                        break;
                    default:
                        result.put("message", "Lỗi không xác định");
                }
                System.out.println("[DEBUG] Tạo lại tài khoản " + soTK + ": " + result.get("message"));
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi tạo lại tài khoản: " + e.getMessage());
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        return result;
    }
}
