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
     * Sử dụng SP_TimThongTinKhachHangTheoSTK
     * 
     * @param soTK      Số tài khoản
     * @param tenServer Tên server
     * @param username  Username đăng nhập
     * @param password  Password đăng nhập
     * @return Map chứa tenKH, tenChiNhanh, soDu, cmnd
     */
    public Map<String, Object> layThongTinTaiKhoan(String soTK, String tenServer, String username, String password) {
        Map<String, Object> result = new HashMap<>();
        String jdbcUrl = fragmentConfig.getConnectionString(tenServer);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {

            // Gọi SP để lấy thông tin khách hàng
            String spCall = "{call SP_TimThongTinKhachHangTheoSTK(?)}";
            try (CallableStatement stmt = conn.prepareCall(spCall)) {
                stmt.setString(1, soTK);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    result.put("success", true);
                    result.put("tenKH", rs.getString("HOTEN"));
                    result.put("tenChiNhanh", rs.getString("TENCN"));
                    result.put("cmnd", rs.getString("CMND"));

                    // TODO: Cần cập nhật SP để trả về SODU và NGAYMOTK
                    // Hiện tại SP chỉ trả về HOTEN, TENCN, CMND
                } else {
                    result.put("success", false);
                    result.put("message", "Tài khoản không tồn tại");
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
     * @param soTK      Số tài khoản
     * @param soTien    Số tiền rút
     * @param maNV      Mã nhân viên thực hiện
     * @param tenServer Tên server
     * @param username  Username đăng nhập
     * @param password  Password đăng nhập
     * @return Map chứa result (1: success, -1: invalid amount, -2: account not
     *         found, -3: invalid employee, -4: insufficient balance, -99: error)
     *         và maGD (mã giao dịch)
     */
    public Map<String, Object> rutTien(String soTK, BigDecimal soTien, String maNV,
            String tenServer, String username, String password) {
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
                    result.put("message", "Tài khoản không tồn tại");
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
     * @param soTK      Số tài khoản
     * @param soTien    Số tiền gửi
     * @param maNV      Mã nhân viên thực hiện
     * @param tenServer Tên server
     * @param username  Username đăng nhập
     * @param password  Password đăng nhập
     * @return Map chứa result và maGD
     */
    public Map<String, Object> goiTien(String soTK, BigDecimal soTien, String maNV,
            String tenServer, String username, String password) {
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
                    result.put("message", "Tài khoản không tồn tại");
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
     * @param soTKGui   Số tài khoản gửi
     * @param soTKNhan  Số tài khoản nhận
     * @param soTien    Số tiền chuyển
     * @param maNV      Mã nhân viên thực hiện
     * @param tenServer Tên server
     * @param username  Username đăng nhập
     * @param password  Password đăng nhập
     * @return Map chứa result (1: success, -1: sender not found, -2: insufficient
     *         balance,
     *         -3: invalid amount, -4: receiver not found, -5: invalid employee,
     *         -99: error)
     */
    public Map<String, Object> chuyenTien(String soTKGui, String soTKNhan, BigDecimal soTien,
            String maNV, String tenServer, String username, String password) {
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
                    result.put("message", "Tài khoản gửi không tồn tại");
                    break;
                case -2:
                    result.put("message", "Số dư không đủ");
                    break;
                case -3:
                    result.put("message", "Số tiền không hợp lệ");
                    break;
                case -4:
                    result.put("message", "Tài khoản nhận không tồn tại");
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
