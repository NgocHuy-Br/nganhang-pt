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
            String diaChi, String soDT) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.sp_CapNhatKhachHang(?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, cmnd);
                stmt.setString(2, ho);
                stmt.setString(3, ten);
                stmt.setString(4, diaChi);
                stmt.setString(5, soDT);
                stmt.registerOutParameter(6, Types.INTEGER);

                stmt.execute();
                int returnCode = stmt.getInt(6);

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
     * Mở tài khoản cho khách hàng
     */
    public Map<String, Object> moTaiKhoan(String tenServer, String soTK, String cmnd,
            String maCN, String maNV) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.sp_MoTaiKhoan(?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, soTK);
                stmt.setString(2, cmnd);
                stmt.setString(3, maCN);
                stmt.setString(4, maNV);
                stmt.registerOutParameter(5, Types.INTEGER);

                stmt.execute();
                int returnCode = stmt.getInt(5);

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
                        result.put("message", "Lỗi không xác định");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi mở tài khoản: " + e.getMessage());
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi: " + e.getMessage());
        }
        return result;
    }
}
