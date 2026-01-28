package com.vn.nganhang_pt.service;

import com.vn.nganhang_pt.config.FragmentConfig;
import com.vn.nganhang_pt.model.ChiNhanh;
import com.vn.nganhang_pt.model.NhanVien;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NhanVienService {

    @Autowired
    private FragmentConfig fragmentConfig;

    /**
     * Lấy danh sách nhân viên
     */
    public List<NhanVien> layDanhSachNhanVien(String tenServer, String username, String password) {
        List<NhanVien> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return danhSach;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.SP_Lay_DS_NhanVien}";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("MANV"));
                    nv.setHo(rs.getString("HO"));
                    nv.setTen(rs.getString("TEN"));
                    nv.setHoTen(rs.getString("HOTEN"));
                    nv.setDiaChi(rs.getString("DIACHI"));
                    nv.setCmnd(rs.getString("CMND"));
                    nv.setPhai(rs.getString("PHAI"));
                    nv.setSoDT(rs.getString("SODT"));
                    nv.setMaCN(rs.getString("MACN"));
                    nv.setTenChiNhanh(rs.getString("TENCN"));
                    nv.setTrangThaiXoa(rs.getInt("TrangThaiXoa"));
                    danhSach.add(nv);
                }
                System.out.println("[DEBUG] Tìm thấy " + danhSach.size() + " nhân viên");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    /**
     * Lấy danh sách nhân viên đã xóa
     */
    public List<NhanVien> layDanhSachNhanVienDaXoa(String tenServer, String username, String password) {
        List<NhanVien> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return danhSach;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.SP_Lay_DS_NhanVien_DaXoa}";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("MANV"));
                    nv.setHo(rs.getString("HO"));
                    nv.setTen(rs.getString("TEN"));
                    nv.setHoTen(rs.getString("HOTEN"));
                    nv.setDiaChi(rs.getString("DIACHI"));
                    nv.setCmnd(rs.getString("CMND"));
                    nv.setPhai(rs.getString("PHAI"));
                    nv.setSoDT(rs.getString("SODT"));
                    nv.setMaCN(rs.getString("MACN"));
                    nv.setTenChiNhanh(rs.getString("TENCN"));
                    nv.setTrangThaiXoa(rs.getInt("TrangThaiXoa"));
                    danhSach.add(nv);
                }
                System.out.println("[DEBUG] Tìm thấy " + danhSach.size() + " nhân viên đã xóa");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách nhân viên đã xóa: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    /**
     * Lấy danh sách chi nhánh
     */
    public List<ChiNhanh> layDanhSachChiNhanh(String tenServer, String username, String password) {
        List<ChiNhanh> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return danhSach;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.sp_LoadDanhSachChiNhanh}";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    ChiNhanh cn = new ChiNhanh();
                    cn.setMaCN(rs.getString("MACN"));
                    cn.setTenCN(rs.getString("TENCN"));
                    danhSach.add(cn);
                }
                System.out.println("[DEBUG] Tìm thấy " + danhSach.size() + " chi nhánh");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách chi nhánh: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }

    /**
     * Thêm nhân viên mới
     */
    public int themNhanVien(NhanVien nv, String tenServer, String username, String password) {
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return -99;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.sp_ThemNhanVien(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, nv.getMaNV());
                stmt.setString(2, nv.getHo());
                stmt.setString(3, nv.getTen());
                stmt.setString(4, nv.getDiaChi());
                stmt.setString(5, nv.getCmnd());
                stmt.setString(6, nv.getPhai());
                stmt.setString(7, nv.getSoDT());
                stmt.setString(8, nv.getMaCN());
                stmt.registerOutParameter(9, Types.INTEGER);

                stmt.execute();
                int result = stmt.getInt(9);
                System.out.println("[DEBUG] Kết quả thêm nhân viên: " + result);
                return result;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
            return -99;
        }
    }

    /**
     * Sửa thông tin nhân viên
     */
    public int suaNhanVien(NhanVien nv, String tenServer, String username, String password) {
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return -99;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.SP_SuaNhanVien(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, nv.getMaNV());
                stmt.setString(2, nv.getHo());
                stmt.setString(3, nv.getTen());
                stmt.setString(4, nv.getDiaChi());
                stmt.setString(5, nv.getCmnd());
                stmt.setString(6, nv.getPhai());
                stmt.setString(7, nv.getSoDT());
                stmt.setString(8, nv.getMaCN());
                stmt.registerOutParameter(9, Types.INTEGER);

                stmt.execute();
                int result = stmt.getInt(9);
                System.out.println("[DEBUG] Kết quả sửa nhân viên: " + result);
                return result;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi sửa nhân viên: " + e.getMessage());
            e.printStackTrace();
            return -99;
        }
    }

    /**
     * Xóa nhân viên (soft delete)
     */
    public int xoaNhanVien(String maNV, String tenServer, String username, String password) {
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return -99;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.sp_XoaNhanVien(?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, maNV);
                stmt.registerOutParameter(2, Types.INTEGER);

                stmt.execute();
                int result = stmt.getInt(2);
                System.out.println("[DEBUG] Kết quả xóa nhân viên: " + result);
                return result;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi xóa nhân viên: " + e.getMessage());
            e.printStackTrace();
            return -99;
        }
    }

    /**
     * Phục hồi nhân viên đã xóa
     */
    public int phucHoiNhanVien(String maNV, String tenServer, String username, String password) {
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return -99;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.SP_PhucHoiNhanVien(?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, maNV);
                stmt.registerOutParameter(2, Types.INTEGER);

                stmt.execute();
                int result = stmt.getInt(2);
                System.out.println("[DEBUG] Kết quả phục hồi nhân viên: " + result);
                return result;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi phục hồi nhân viên: " + e.getMessage());
            e.printStackTrace();
            return -99;
        }
    }

    /**
     * Chuyển nhân viên sang chi nhánh khác
     */
    public Map<String, Object> chuyenChiNhanh(String maNVCu, String maCNMoi, String maNVMoi, String tenServer,
            String username, String password) {
        Map<String, Object> response = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            response.put("result", -99);
            response.put("message", "Lỗi kết nối");
            return response;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            String sql = "{call dbo.sp_ChuyenChiNhanhNhanVien(?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, maNVCu);
                stmt.setString(2, maCNMoi);
                stmt.setString(3, maNVMoi);

                stmt.execute();

                response.put("result", 1);
                response.put("message", "Chuyển chi nhánh thành công! Mã nhân viên mới: " + maNVMoi);
                response.put("maNVMoi", maNVMoi);

            }
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            System.err.println("[ERROR] Lỗi khi chuyển chi nhánh: " + errorMsg);

            if (errorMsg.contains("không tồn tại") || errorMsg.contains("đã bị xóa")) {
                response.put("result", -1);
                response.put("message", errorMsg);
            } else if (errorMsg.contains("đã ở chi nhánh này")) {
                response.put("result", -2);
                response.put("message", errorMsg);
            } else {
                response.put("result", -99);
                response.put("message", "Lỗi: " + errorMsg);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
            response.put("result", -99);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
        }
        return response;
    }

    /**
     * Tạo tài khoản đăng nhập cho nhân viên
     */
    public Map<String, Object> taoTaiKhoanDangNhap(String tenServer, String loginName, String password,
            String userName, String role, String currentUsername, String currentPassword) {
        Map<String, Object> result = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        System.out.println("[DEBUG NhanVienService] Tạo login với credentials: username=" + currentUsername
                + ", password=" + (currentPassword != null ? "***" : "NULL"));

        try (Connection conn = DriverManager.getConnection(connectionString, currentUsername, currentPassword)) {
            String sql = "{call dbo.sp_TaoLogin(?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, loginName);
                stmt.setString(2, password);
                stmt.setString(3, userName);
                stmt.setString(4, role);

                stmt.execute();

                result.put("result", 1);
                result.put("message", "Tạo tài khoản đăng nhập thành công");
                System.out.println("[DEBUG] Đã tạo login: " + loginName + " với role: " + role);

            }
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            System.err.println("[ERROR] Lỗi khi tạo login: " + errorMsg);

            // Kiểm tra lỗi login đã tồn tại
            if (errorMsg.contains("đã tồn tại")) {
                result.put("result", -1);
                result.put("message", errorMsg);
            } else if (errorMsg.contains("không hợp lệ")) {
                result.put("result", -2);
                result.put("message", errorMsg);
            } else {
                result.put("result", -99);
                result.put("message", "Lỗi khi tạo tài khoản: " + errorMsg);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
        }
        return result;
    }
}
