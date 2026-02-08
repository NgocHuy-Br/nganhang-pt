package com.vn.nganhang_pt.service;

import com.vn.nganhang_pt.config.FragmentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Service xử lý các giao dịch: Rút tiền, Gửi tiền, Chuyển tiền
 */
@Service
public class GiaoDichService {

    @Autowired
    private FragmentConfig fragmentConfig;

    /**
     * Lấy thông tin tài khoản (tên KH, chi nhánh, số dư)
     * Sử dụng SP_TimThongTinKhachHangTheoSTK hoặc
     * SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh
     * 
     * @param soTK          Số tài khoản
     * @param tenServer     Tên server
     * @param username      Username đăng nhập
     * @param password      Password đăng nhập
     * @param role          Role của user (NGANHANG sử dụng SP khác)
     * @param tenChiNhanh   Tên chi nhánh hiện tại
     * @param isTransaction Có phải là giao dịch (true) hay sao kê (false)
     * @return Map chứa tenKH, tenChiNhanh, soDu, cmnd
     */
    public Map<String, Object> layThongTinTaiKhoan(String soTK, String tenServer, String username, String password,
            String role, String tenChiNhanh, boolean isTransaction) {
        Map<String, Object> result = new HashMap<>();
        String jdbcUrl = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {

            // Chọn SP phù hợp theo loại thao tác
            String spCall = isTransaction
                    ? "{call SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh(?)}" // Giao dịch: tìm tất cả chi nhánh
                    : "{call SP_TimThongTinKhachHangTheoSTK(?)}"; // Sao kê: chỉ tìm chi nhánh hiện tại

            try (CallableStatement stmt = conn.prepareCall(spCall)) {
                stmt.setString(1, soTK);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Kiểm tra xem có cột ThongBao không (SP trả về lỗi)
                    try {
                        String thongBao = rs.getString("ThongBao");
                        if (thongBao != null) {
                            // SP trả về thông báo lỗi
                            result.put("success", false);
                            result.put("message", thongBao);
                            return result;
                        }
                    } catch (SQLException e) {
                        // Không có cột ThongBao, continue với logic bình thường
                    }

                    // SP trả về dữ liệu hợp lệ
                    result.put("success", true);
                    result.put("tenKH", rs.getString("HOTEN"));
                    result.put("tenChiNhanh", rs.getString("TENCN"));
                    result.put("cmnd", rs.getString("CMND"));

                    // Lấy số dư hiện tại (SP trả về SODU)
                    try {
                        Object soDuObj = rs.getObject("SODU");
                        if (soDuObj != null) {
                            java.math.BigDecimal soDu = rs.getBigDecimal("SODU");
                            result.put("soDu", soDu);
                            System.out.println("[DEBUG] Số dư: " + soDu + " cho tài khoản " + soTK);
                        } else {
                            System.out.println("[DEBUG] SODU is null for account " + soTK);
                        }
                    } catch (SQLException e) {
                        System.err.println("[ERROR] Không thể lấy SODU: " + e.getMessage());
                        e.printStackTrace();
                    }

                    // Lấy ngày mở tài khoản (đã được thêm vào SP)
                    java.sql.Date ngayMoTK = rs.getDate("NGAYMOTK");
                    if (ngayMoTK != null) {
                        result.put("ngayMoTK", ngayMoTK.toString()); // Format: yyyy-MM-dd
                    }
                } else {
                    // Không có kết quả nào được trả về
                    result.put("success", false);
                    result.put("message", isTransaction
                            ? "Số tài khoản không tồn tại ở bất kỳ chi nhánh nào"
                            : "Không tìm thấy số tài khoản ở chi nhánh hiện tại");
                    return result;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Lỗi SQL: " + e.getMessage());
        }

        return result;
    }

    /**
     * Rút tiền từ tài khoản
     * 
     * @param soTK        Số tài khoản
     * @param soTien      Số tiền rút
     * @param maNV        Mã nhân viên thực hiện
     * @param tenServer   Tên server
     * @param username    Username đăng nhập
     * @param password    Password đăng nhập
     * @param tenChiNhanh Tên chi nhánh hiện tại
     * @return Map chứa result (1: success, -1: invalid amount, -2: account not
     *         found, -3: invalid employee, -4: insufficient balance, -99: error)
     *         và maGD (mã giao dịch)
     */
    public Map<String, Object> rutTien(String soTK, BigDecimal soTien, String maNV,
            String tenServer, String username, String password, String tenChiNhanh) {
        Map<String, Object> result = new HashMap<>();
        String jdbcUrl = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                CallableStatement stmt = conn.prepareCall("{call sp_RutTien(?, ?, ?, ?, ?)}")) {

            stmt.setString(1, soTK);
            stmt.setBigDecimal(2, soTien);
            stmt.setString(3, maNV);
            stmt.registerOutParameter(4, Types.INTEGER); // @Result
            stmt.registerOutParameter(5, Types.NVARCHAR); // @MaGD

            stmt.execute();

            int resultCode = stmt.getInt(4);
            String maGD = stmt.getString(5);

            result.put("result", resultCode);
            result.put("maGD", maGD);

            // Set message based on result code
            switch (resultCode) {
                case 1:
                    result.put("message", "Rút tiền thành công. Mã GD: " + maGD);
                    break;
                case -1:
                    result.put("message", "Số tiền không hợp lệ (tối thiểu 100,000 VNĐ)");
                    break;
                case -2:
                    result.put("message", "Số tài khoản không tồn tại ở bất kỳ chi nhánh nào");
                    break;
                case -3:
                    result.put("message", "Nhân viên không hợp lệ");
                    break;
                case -4:
                    result.put("message", "Số dư không đủ");
                    break;
                default:
                    result.put("message", "Lỗi khi thực hiện giao dịch");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi SQL: " + e.getMessage());
        }

        return result;
    }

    /**
     * Gửi tiền vào tài khoản
     * 
     * @param soTK        Số tài khoản
     * @param soTien      Số tiền gửi
     * @param maNV        Mã nhân viên thực hiện
     * @param tenServer   Tên server
     * @param username    Username đăng nhập
     * @param password    Password đăng nhập
     * @param tenChiNhanh Tên chi nhánh hiện tại
     * @return Map chứa result và maGD
     */
    public Map<String, Object> goiTien(String soTK, BigDecimal soTien, String maNV,
            String tenServer, String username, String password, String tenChiNhanh) {
        Map<String, Object> result = new HashMap<>();
        String jdbcUrl = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                CallableStatement stmt = conn.prepareCall("{call sp_GoiTien(?, ?, ?, ?, ?)}")) {

            stmt.setString(1, soTK);
            stmt.setBigDecimal(2, soTien);
            stmt.setString(3, maNV);
            stmt.registerOutParameter(4, Types.INTEGER); // @Result
            stmt.registerOutParameter(5, Types.NVARCHAR); // @MaGD

            stmt.execute();

            int resultCode = stmt.getInt(4);
            String maGD = stmt.getString(5);

            result.put("result", resultCode);
            result.put("maGD", maGD);

            // Set message based on result code
            switch (resultCode) {
                case 1:
                    result.put("message", "Gửi tiền thành công. Mã GD: " + maGD);
                    break;
                case -1:
                    result.put("message", "Số tiền không hợp lệ (tối thiểu 100,000 VNĐ)");
                    break;
                case -2:
                    result.put("message", "Số tài khoản không tồn tại ở bất kỳ chi nhánh nào");
                    break;
                case -3:
                    result.put("message", "Nhân viên không hợp lệ");
                    break;
                default:
                    result.put("message", "Lỗi khi thực hiện giao dịch");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi SQL: " + e.getMessage());
        }

        return result;
    }

    /**
     * Chuyển tiền giữa 2 tài khoản
     * 
     * @param soTKGui     Số tài khoản gửi
     * @param soTKNhan    Số tài khoản nhận
     * @param soTien      Số tiền chuyển
     * @param maNV        Mã nhân viên thực hiện
     * @param tenServer   Tên server
     * @param username    Username đăng nhập
     * @param password    Password đăng nhập
     * @param tenChiNhanh Tên chi nhánh hiện tại
     * @return Map chứa result (1: success, -1: sender not found, -2: insufficient
     *         balance,
     *         -3: invalid amount, -4: receiver not found, -5: invalid employee,
     *         -99: error)
     */
    public Map<String, Object> chuyenTien(String soTKGui, String soTKNhan, BigDecimal soTien,
            String maNV, String tenServer, String username, String password, String tenChiNhanh) {
        Map<String, Object> result = new HashMap<>();
        String jdbcUrl = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                CallableStatement stmt = conn.prepareCall("{call sp_ChuyenTien(?, ?, ?, ?, ?)}")) {

            stmt.setString(1, soTKGui);
            stmt.setString(2, soTKNhan);
            stmt.setBigDecimal(3, soTien);
            stmt.setString(4, maNV);
            stmt.registerOutParameter(5, Types.INTEGER); // @Result

            stmt.execute();

            int resultCode = stmt.getInt(5);
            result.put("result", resultCode);

            // Set message based on result code
            switch (resultCode) {
                case 1:
                    result.put("message", "Chuyển tiền thành công");
                    break;
                case -1:
                    result.put("message", "Số tài khoản không tồn tại ở bất kỳ chi nhánh nào");
                    break;
                case -2:
                    result.put("message", "Số dư không đủ");
                    break;
                case -3:
                    result.put("message", "Số tiền không hợp lệ");
                    break;
                case -4:
                    result.put("message", "Số tài khoản không tồn tại ở bất kỳ chi nhánh nào");
                    break;
                case -5:
                    result.put("message", "Nhân viên không hợp lệ");
                    break;
                default:
                    result.put("message", "Lỗi khi thực hiện giao dịch");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            result.put("result", -99);
            result.put("message", "Lỗi SQL: " + e.getMessage());
        }

        return result;
    }
}
