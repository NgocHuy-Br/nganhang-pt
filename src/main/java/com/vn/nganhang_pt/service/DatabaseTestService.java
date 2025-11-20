package com.vn.nganhang_pt.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseTestService {

    private final DataSource dataSource;

    public DatabaseTestService(@Qualifier("dataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Test connection to main database only
     */
    public Map<String, Object> testConnection() {
        Map<String, Object> results = new HashMap<>();
        results.put("main", testConnectionToDb(dataSource, "NGANHANG (DB Chính - kết nối qua Linked Server)"));
        return results;
    }

    /**
     * Test connection to database
     */
    private Map<String, Object> testConnectionToDb(DataSource ds, String dbName) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", dbName);

        try (Connection conn = ds.getConnection()) {
            result.put("status", "✅ Kết nối thành công");
            result.put("connected", true);

            // Get database info
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            String dbProductVersion = conn.getMetaData().getDatabaseProductVersion();
            String dbUrl = conn.getMetaData().getURL();

            result.put("info", String.format("%s %s", dbProductName, dbProductVersion));
            result.put("url", dbUrl);

        } catch (Exception e) {
            result.put("status", "❌ Lỗi kết nối");
            result.put("connected", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Test SP: Lấy danh sách tất cả nhân viên (chưa xóa)
     * SP này sẽ tự động lấy data từ site hiện tại hoặc từ site gốc qua LINK0
     */
    public List<Map<String, Object>> layDanhSachNhanVien() {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("[DEBUG] Bắt đầu gọi SP_Lay_DS_NhanVien...");
            long startTime = System.currentTimeMillis();

            // Gọi stored procedure: dbo.SP_Lay_DS_NhanVien
            String sql = "{CALL dbo.SP_Lay_DS_NhanVien}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                // Set timeout 30 giây
                stmt.setQueryTimeout(30);

                System.out.println("[DEBUG] Đang thực thi SP...");
                ResultSet rs = stmt.executeQuery();

                long executeTime = System.currentTimeMillis() - startTime;
                System.out.println("[DEBUG] SP thực thi xong sau " + executeTime + "ms");

                // Lấy metadata để biết có bao nhiêu cột
                int columnCount = rs.getMetaData().getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }

                System.out.println("[DEBUG] Đã lấy " + results.size() + " bản ghi");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi gọi SP_Lay_DS_NhanVien: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi khi gọi SP: " + e.getMessage());
            error.put("details", e.getClass().getName());
            results.add(error);
        }

        return results;
    }

    /**
     * Test SP: Lấy danh sách nhân viên đã xóa
     */
    public List<Map<String, Object>> layDanhSachNhanVienDaXoa() {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            // Gọi stored procedure: dbo.SP_Lay_DS_NhanVien_DaXoa
            String sql = "{CALL dbo.SP_Lay_DS_NhanVien_DaXoa}";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                // Lấy metadata để biết có bao nhiêu cột
                int columnCount = rs.getMetaData().getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi khi gọi SP: " + e.getMessage());
            error.put("details", e.getClass().getName());
            results.add(error);
        }

        return results;
    }

    /**
     * Test query trực tiếp bảng NHANVIEN (không qua SP) để kiểm tra performance
     */
    public Map<String, Object> testDirectQuery() {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            // Test 1: Đếm số records
            System.out.println("[TEST] Đang đếm tổng số nhân viên...");
            long startTime = System.currentTimeMillis();

            String countSql = "SELECT COUNT(*) as total FROM dbo.NHANVIEN WHERE TrangThaiXoa = 0";
            try (CallableStatement stmt = conn.prepareCall(countSql);
                    ResultSet rs = stmt.executeQuery()) {
                rs.next();
                int total = rs.getInt("total");
                long countTime = System.currentTimeMillis() - startTime;

                result.put("totalRecords", total);
                result.put("countTime", countTime + "ms");
                System.out
                        .println("[TEST] Có " + total + " nhân viên (TrangThaiXoa=0), thời gian: " + countTime + "ms");
            }

            // Test 2: Query đơn giản (chỉ lấy MANV, HO, TEN)
            System.out.println("[TEST] Đang query danh sách nhân viên (đơn giản)...");
            startTime = System.currentTimeMillis();

            String simpleSql = "SELECT TOP 10 MANV, HO, TEN FROM dbo.NHANVIEN WHERE TrangThaiXoa = 0";
            List<Map<String, Object>> sampleData = new ArrayList<>();

            try (CallableStatement stmt = conn.prepareCall(simpleSql);
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MANV", rs.getObject("MANV"));
                    row.put("HO", rs.getObject("HO"));
                    row.put("TEN", rs.getObject("TEN"));
                    sampleData.add(row);
                }
                long queryTime = System.currentTimeMillis() - startTime;

                result.put("sampleData", sampleData);
                result.put("simpleQueryTime", queryTime + "ms");
                System.out.println("[TEST] Query đơn giản mất: " + queryTime + "ms");
            }

            // Test 3: Query có JOIN (giống SP)
            System.out.println("[TEST] Đang query với LEFT JOIN CHINHANH...");
            startTime = System.currentTimeMillis();

            String joinSql = "SELECT TOP 10 NV.MANV, NV.HO, NV.TEN, NV.MACN, CN.TENCN " +
                    "FROM dbo.NHANVIEN AS NV " +
                    "LEFT JOIN dbo.CHINHANH AS CN ON NV.MACN = CN.MACN " +
                    "WHERE NV.TrangThaiXoa = 0";

            try (CallableStatement stmt = conn.prepareCall(joinSql);
                    ResultSet rs = stmt.executeQuery()) {
                rs.next(); // Chỉ cần biết có chạy được không
                long joinTime = System.currentTimeMillis() - startTime;

                result.put("joinQueryTime", joinTime + "ms");
                System.out.println("[TEST] Query có JOIN mất: " + joinTime + "ms");
            }

            result.put("status", "success");

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            System.err.println("[TEST] Lỗi: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Test SP: Thêm nhân viên mới
     */
    public Map<String, Object> themNhanVien(String manv, String ho, String ten, String diachi,
            String cmnd, String phai, String sodt, String macn) {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("[DEBUG] Bắt đầu gọi sp_ThemNhanVien với MANV=" + manv);

            String sql = "{CALL dbo.sp_ThemNhanVien(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                // Set input parameters
                stmt.setString(1, manv);
                stmt.setString(2, ho);
                stmt.setString(3, ten);
                stmt.setString(4, diachi);
                stmt.setString(5, cmnd);
                stmt.setString(6, phai);
                stmt.setString(7, sodt);
                stmt.setString(8, macn);

                // Register output parameter
                stmt.registerOutParameter(9, java.sql.Types.INTEGER);

                // Execute
                stmt.execute();

                // Get result
                int resultCode = stmt.getInt(9);

                System.out.println("[DEBUG] sp_ThemNhanVien trả về: " + resultCode);

                result.put("resultCode", resultCode);
                result.put("success", resultCode == 1);

                // Map result code to message
                switch (resultCode) {
                    case 1:
                        result.put("message", "✅ Thêm nhân viên thành công!");
                        break;
                    case -1:
                        result.put("message", "❌ Chi nhánh không tồn tại");
                        break;
                    case -2:
                        result.put("message", "❌ Mã nhân viên đã tồn tại");
                        break;
                    case -3:
                        result.put("message", "❌ CMND đã tồn tại");
                        break;
                    case -4:
                        result.put("message", "❌ Giới tính không hợp lệ (phải là 'Nam' hoặc 'Nữ')");
                        break;
                    case -99:
                        result.put("message", "❌ Lỗi không xác định khi thực thi");
                        break;
                    default:
                        result.put("message", "❌ Lỗi không xác định (code: " + resultCode + ")");
                }

            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "❌ Lỗi khi gọi SP: " + e.getMessage());
            System.err.println("[ERROR] Lỗi khi gọi sp_ThemNhanVien: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Lấy danh sách chi nhánh (để hiển thị trong form)
     */
    public List<Map<String, Object>> layDanhSachChiNhanh() {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT MACN, TENCN FROM dbo.CHINHANH ORDER BY MACN";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("MACN", rs.getString("MACN"));
                    row.put("TENCN", rs.getString("TENCN"));
                    results.add(row);
                }
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy danh sách chi nhánh: " + e.getMessage());
        }

        return results;
    }
}
