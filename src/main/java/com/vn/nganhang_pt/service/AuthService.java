package com.vn.nganhang_pt.service;

import com.vn.nganhang_pt.model.ChiNhanh;
import com.vn.nganhang_pt.model.NhanVien;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    // Map server name to connection string
    private final Map<String, String> serverConnections = new HashMap<>();

    public AuthService() {
        // Khởi tạo mapping các server - tên server từ view Get_Subscribes
        serverConnections.put("HCM-LAPT-001\\SQLSRV_NH01",
                "jdbc:sqlserver://10.241.78.94:1440;databaseName=NGANHANG;encrypt=true;trustServerCertificate=true");
        serverConnections.put("HCM-LAPT-001\\SQLSRV_NH02",
                "jdbc:sqlserver://10.241.78.94:1441;databaseName=NGANHANG;encrypt=true;trustServerCertificate=true");
        serverConnections.put("HCM-LAPT-001\\SQLSRV_NH03",
                "jdbc:sqlserver://10.241.78.94:1443;databaseName=NGANHANG;encrypt=true;trustServerCertificate=true");
    }

    /**
     * Lấy danh sách chi nhánh từ view V_DS_PHANMANH (tương tự C# Winform)
     * View trả về TENCN và TENSERVER của các phân mảnh đã cấu hình
     */
    public List<ChiNhanh> layDanhSachChiNhanh() {
        List<ChiNhanh> danhSach = new ArrayList<>();

        // Kết nối đến CSDL gốc (Publisher) để lấy danh sách phân mảnh
        String url = "jdbc:sqlserver://10.241.78.94;databaseName=NGANHANG;encrypt=true;trustServerCertificate=true";
        String user = "HTKN";
        String password = "123456";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Query view Get_Subscribes để lấy danh sách phân mảnh
            String sql = "SELECT TENCN, TENSERVER FROM dbo.Get_Subscribes ORDER BY TENSERVER";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String tenCN = rs.getString("TENCN");
                    String tenServer = rs.getString("TENSERVER");
                    danhSach.add(new ChiNhanh(tenCN, tenServer));
                }

                System.out.println("[DEBUG] Đã load " + danhSach.size() + " chi nhánh từ view Get_Subscribes");

            }

        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách chi nhánh từ view: " + e.getMessage());
            e.printStackTrace();

            // Fallback: Nếu lỗi thì dùng danh sách hardcode
            System.out.println("[WARN] Fallback sang danh sách hardcode");
            danhSach.add(new ChiNhanh("Chi nhánh Bến Thành", "HCM-LAPT-001\\SQLSRV_NH01"));
            danhSach.add(new ChiNhanh("Chi nhánh Tân Định", "HCM-LAPT-001\\SQLSRV_NH02"));
            danhSach.add(new ChiNhanh("Trụ cở", "HCM-LAPT-001\\SQLSRV_NH03"));
        }

        return danhSach;
    }

    /**
     * Đăng nhập và lấy thông tin nhân viên
     * 
     * @param username  Tên đăng nhập
     * @param password  Mật khẩu
     * @param tenServer Tên server chi nhánh
     * @return NhanVien object nếu thành công, null nếu thất bại
     */
    public NhanVien dangNhap(String username, String password, String tenServer) {
        // Lấy connection string tương ứng với server
        String connectionString = serverConnections.get(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return null;
        }

        System.out.println("[DEBUG] Đang thử đăng nhập với username=" + username + " vào server=" + tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            // Kết nối thành công, giờ lấy thông tin nhân viên
            String sql = "{call dbo.SP_Lay_Thong_Tin_NV_Tu_Login(?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        NhanVien nhanVien = new NhanVien();
                        nhanVien.setMaNV(rs.getString("MAGV"));
                        nhanVien.setHoTen(rs.getString("HOTEN"));
                        nhanVien.setTenNhom(rs.getString("TENNHOM"));
                        nhanVien.setMaCN(rs.getString("MACN"));

                        System.out.println("[DEBUG] Đăng nhập thành công: " + nhanVien.getHoTen());
                        return nhanVien;
                    }
                }
            }

        } catch (java.sql.SQLException e) {
            // Lỗi xác thực hoặc kết nối
            System.err.println("[ERROR] Đăng nhập thất bại: " + e.getMessage());
            if (e.getErrorCode() == 18456) {
                System.err.println("[ERROR] Sai tên đăng nhập hoặc mật khẩu");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi không xác định: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
