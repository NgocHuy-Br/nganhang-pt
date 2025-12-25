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
    public List<NhanVien> layDanhSachNhanVien(String tenServer) {
        List<NhanVien> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return danhSach;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
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
    public List<NhanVien> layDanhSachNhanVienDaXoa(String tenServer) {
        List<NhanVien> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return danhSach;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
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
    public List<ChiNhanh> layDanhSachChiNhanh(String tenServer) {
        List<ChiNhanh> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return danhSach;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "SELECT MACN, TENCN FROM CHINHANH ORDER BY MACN";

            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {

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
    public int themNhanVien(NhanVien nv, String tenServer) {
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return -99;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
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
    public int suaNhanVien(NhanVien nv, String tenServer) {
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return -99;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
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
    public int xoaNhanVien(String maNV, String tenServer) {
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return -99;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
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
    public int phucHoiNhanVien(String maNV, String tenServer) {
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return -99;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
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
    public Map<String, Object> chuyenChiNhanh(String maNV, String maCNMoi, String tenServer) {
        Map<String, Object> response = new HashMap<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            response.put("result", -99);
            response.put("message", "Lỗi kết nối");
            return response;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.sp_ChuyenNhanVien(?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, maNV);
                stmt.setString(2, maCNMoi);
                stmt.registerOutParameter(3, Types.NCHAR);

                int result = stmt.executeUpdate();
                String maNVMoi = stmt.getString(3);

                System.out.println("[DEBUG] Kết quả chuyển chi nhánh: " + result);
                System.out.println("[DEBUG] Mã NV mới: " + maNVMoi);

                if (result == 0) {
                    response.put("result", 1);
                    response.put("message", "Chuyển chi nhánh thành công");
                    response.put("maNVMoi", maNVMoi);
                } else {
                    response.put("result", -1);
                    response.put("message", "Có lỗi xảy ra khi chuyển chi nhánh");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi chuyển chi nhánh: " + e.getMessage());
            e.printStackTrace();
            response.put("result", -99);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
        }
        return response;
    }
}
