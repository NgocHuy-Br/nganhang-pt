package com.vn.nganhang_pt.service;

import com.vn.nganhang_pt.config.FragmentConfig;
import com.vn.nganhang_pt.model.ChiNhanh;
import com.vn.nganhang_pt.model.GiaoDich;
import com.vn.nganhang_pt.model.KhachHang;
import com.vn.nganhang_pt.model.NhanVien;
import com.vn.nganhang_pt.model.TaiKhoan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private final DataSource dataSource;
    private final FragmentConfig fragmentConfig;

    @Autowired
    public AuthService(DataSource dataSource, FragmentConfig fragmentConfig) {
        this.dataSource = dataSource;
        this.fragmentConfig = fragmentConfig;
    }

    /**
     * Lấy danh sách chi nhánh từ view Get_Subscribes
     * Sử dụng DataSource đã cấu hình trong application.properties
     */
    public List<ChiNhanh> layDanhSachChiNhanh() {
        List<ChiNhanh> danhSach = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
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
     * Đăng nhập và lấy thông tin người dùng (nhân viên hoặc khách hàng)
     * 
     * @param username  Tên đăng nhập
     * @param password  Mật khẩu
     * @param tenServer Tên server chi nhánh
     * @return NhanVien hoặc KhachHang object nếu thành công, null nếu thất bại
     */
    public Object dangNhap(String username, String password, String tenServer) {
        // Lấy connection string từ FragmentConfig
        String connectionString = fragmentConfig.getConnectionString(tenServer);

        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            System.err.println("[ERROR] Server name: " + tenServer);
            System.err.println("[ERROR] Available servers: " + fragmentConfig.getConnections().keySet());
            return null;
        }

        System.out.println("[DEBUG] Đang thử đăng nhập với username=" + username + " vào server=" + tenServer);

        try (Connection conn = DriverManager.getConnection(connectionString, username, password)) {
            // Kết nối thành công, thử lấy thông tin nhân viên trước
            NhanVien nhanVien = layThongTinNhanVien(conn, username);
            if (nhanVien != null) {
                System.out.println("[DEBUG] Đăng nhập thành công với tư cách NHÂN VIÊN: " + nhanVien.getHoTen());
                return nhanVien;
            }

            // Nếu không phải nhân viên, thử khách hàng
            KhachHang khachHang = layThongTinKhachHang(conn, username, tenServer);
            if (khachHang != null) {
                System.out.println("[DEBUG] Đăng nhập thành công với tư cách KHÁCH HÀNG: " + khachHang.getHoten());
                return khachHang;
            }

            System.err.println("[ERROR] Không tìm thấy thông tin người dùng");

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

    /**
     * Lấy thông tin nhân viên từ SP_Lay_Thong_Tin_NV_Tu_Login
     */
    private NhanVien layThongTinNhanVien(Connection conn, String username) {
        try {
            String sql = "{call dbo.SP_Lay_Thong_Tin_NV_Tu_Login(?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String hoTen = rs.getString("HOTEN");
                        // Kiểm tra nếu HOTEN là null, có nghĩa là không tìm thấy nhân viên
                        if (hoTen == null || hoTen.trim().isEmpty()) {
                            return null;
                        }

                        NhanVien nhanVien = new NhanVien();
                        nhanVien.setMaNV(rs.getString("MAGV"));
                        nhanVien.setHoTen(hoTen);
                        nhanVien.setTenNhom(rs.getString("TENNHOM"));
                        nhanVien.setMaCN(rs.getString("MACN"));

                        // Set thêm các field mới từ ResultSet (nếu SP trả về)
                        try {
                            nhanVien.setDiaChi(rs.getString("DIACHI"));
                        } catch (Exception e) {
                            nhanVien.setDiaChi(""); // Set empty nếu không có
                        }

                        try {
                            nhanVien.setNgaySinh(rs.getDate("NGAYSINH"));
                        } catch (Exception e) {
                            nhanVien.setNgaySinh(null);
                        }

                        nhanVien.setTenDangNhap(username);

                        // Lấy role hiện tại của user
                        String roleHienTai = layRoleHienTai(conn);
                        nhanVien.setRole(roleHienTai);

                        return nhanVien;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Không phải tài khoản nhân viên");
        }
        return null;
    }

    /**
     * Lấy thông tin khách hàng từ SP_Lay_Thong_Tin_KH_Tu_Login
     */
    private KhachHang layThongTinKhachHang(Connection conn, String username, String tenServer) {
        try {
            String sql = "{call dbo.SP_Lay_Thong_Tin_KH_Tu_Login(?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        KhachHang khachHang = new KhachHang();
                        khachHang.setMakh(rs.getString("MAKH"));
                        khachHang.setHoten(rs.getString("HOTEN"));
                        khachHang.setTennhom(rs.getString("TENNHOM"));
                        khachHang.setMacn(rs.getString("MACN"));
                        khachHang.setSotk(rs.getString("SOTK"));
                        khachHang.setSodu(rs.getString("SODU"));

                        // Set các thông tin bổ sung
                        khachHang.setCmnd(rs.getString("MAKH")); // MAKH chính là CMND

                        // Lấy thêm thông tin chi tiết nếu có
                        try {
                            khachHang.setDiaChi(rs.getString("DIACHI"));
                            khachHang.setPhai(rs.getString("PHAI"));
                            khachHang.setSoDT(rs.getString("SODT"));
                        } catch (Exception e) {
                            // Nếu SP không trả về các field này, set empty
                            khachHang.setDiaChi("");
                            khachHang.setPhai("");
                            khachHang.setSoDT("");
                        }

                        // Lấy role hiện tại của user
                        String roleHienTai = layRoleHienTai(conn);
                        khachHang.setRole(roleHienTai);

                        return khachHang;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Không phải tài khoản khách hàng: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy role hiện tại của user đã đăng nhập
     * Sử dụng SP_LayDanhSachRoleTheoQuyenHienTai
     * 
     * @param conn Connection đã mở với user đã xác thực
     * @return Role hiện tại (NGANHANG, CHINHANH, hoặc KHONG_CO_QUYEN)
     */
    private String layRoleHienTai(Connection conn) {
        try {
            String sql = "{call dbo.SP_LayDanhSachRoleTheoQuyenHienTai()}";

            try (CallableStatement stmt = conn.prepareCall(sql);
                    ResultSet rs = stmt.executeQuery()) {

                // Lấy role đầu tiên (TOP 1)
                if (rs.next()) {
                    String role = rs.getString("TENNHOM");
                    System.out.println("[DEBUG] SP trả về role: " + role);
                    return role;
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Không thể lấy role hiện tại: " + e.getMessage());
            e.printStackTrace();
        }

        return "KHONG_XAC_DINH";
    }

    /**
     * Lấy thông tin đầy đủ của nhân viên đã đăng nhập
     * Sử dụng SP_ThongTinDangNhapHienTai
     * 
     * @param loginID   Mã nhân viên
     * @param role      Role hiện tại
     * @param tenServer Tên server phân mảnh
     * @return NhanVien với đầy đủ thông tin
     */
    public NhanVien layThongTinDayDuNhanVien(String loginID, String role, String tenServer) {
        System.out.println("[DEBUG AuthService] Bắt đầu lấy thông tin đầy đủ cho loginID=" + loginID + ", role=" + role
                + ", server=" + tenServer);

        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            System.out.println("[DEBUG] Available servers: " + fragmentConfig.getConnections().keySet());
            return null;
        }

        System.out.println("[DEBUG] Connection string: "
                + connectionString.substring(0, Math.min(80, connectionString.length())) + "...");
        System.out.println("[DEBUG] Username: " + fragmentConfig.getUsername());

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            System.out.println("[DEBUG] Kết nối thành công, đang gọi SP...");
            String sql = "{call dbo.SP_ThongTinDangNhapHienTai(?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, loginID);
                stmt.setString(2, role);

                System.out.println("[DEBUG] Executing SP với params: loginID=" + loginID + ", role=" + role);

                try (ResultSet rs = stmt.executeQuery()) {
                    System.out.println("[DEBUG] SP thực thi xong, kiểm tra kết quả...");

                    if (rs.next()) {
                        System.out.println("[DEBUG] Tìm thấy kết quả từ SP!");
                        NhanVien nhanVien = new NhanVien();
                        nhanVien.setMaNV(rs.getString("ID"));
                        nhanVien.setHo(rs.getString("HO"));
                        nhanVien.setTen(rs.getString("TEN"));
                        nhanVien.setHoTen(rs.getString("HO") + " " + rs.getString("TEN"));
                        nhanVien.setCmnd(rs.getString("CMND"));
                        nhanVien.setDiaChi(rs.getString("DIACHI"));
                        nhanVien.setPhai(rs.getString("PHAI"));
                        nhanVien.setSoDT(rs.getString("SODT"));
                        nhanVien.setMaCN(rs.getString("MACN"));
                        nhanVien.setRole(rs.getString("ROLE"));
                        nhanVien.setTenNhom(rs.getString("ROLE"));

                        System.out.println("[DEBUG] Đã map xong dữ liệu: maNV=" + nhanVien.getMaNV() + ", ho="
                                + nhanVien.getHo() + ", ten=" + nhanVien.getTen());
                        return nhanVien;
                    } else {
                        System.out.println("[WARNING] SP không trả về dòng nào (rs.next() = false)");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi lấy thông tin đầy đủ nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy thông tin đầy đủ của khách hàng đã đăng nhập
     * Query trực tiếp bảng KHACHHANG
     * 
     * @param cmnd      CMND khách hàng
     * @param tenServer Tên server phân mảnh
     * @return KhachHang với đầy đủ thông tin
     */
    public KhachHang layThongTinDayDuKhachHang(String cmnd, String tenServer) {
        System.out.println("[DEBUG AuthService] Bắt đầu query thông tin đầy đủ khách hàng cho CMND=" + cmnd
                + ", server=" + tenServer);

        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return null;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            System.out.println("[DEBUG] Kết nối thành công, đang query KHACHHANG...");

            // Query trực tiếp bảng KHACHHANG để lấy thông tin chi tiết
            String sql = "SELECT KH.CMND, KH.HO, KH.TEN, KH.DIACHI, KH.PHAI, KH.SODT, KH.MACN " +
                    "FROM KHACHHANG KH WHERE KH.CMND = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cmnd);

                System.out.println("[DEBUG] Executing query với CMND=" + cmnd);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("[DEBUG] Tìm thấy thông tin khách hàng từ bảng KHACHHANG!");
                        KhachHang khachHang = new KhachHang();
                        khachHang.setCmnd(rs.getString("CMND"));
                        khachHang.setHo(rs.getString("HO"));
                        khachHang.setTen(rs.getString("TEN"));
                        khachHang.setHoten(rs.getString("HO") + " " + rs.getString("TEN"));
                        khachHang.setDiaChi(rs.getString("DIACHI"));
                        khachHang.setPhai(rs.getString("PHAI"));
                        khachHang.setSoDT(rs.getString("SODT"));
                        khachHang.setMacn(rs.getString("MACN"));

                        System.out.println("[DEBUG] Đã map dữ liệu: ho=" + khachHang.getHo() + ", ten="
                                + khachHang.getTen() + ", diaChi=" + khachHang.getDiaChi() + ", phai="
                                + khachHang.getPhai() + ", soDT=" + khachHang.getSoDT());
                        return khachHang;
                    } else {
                        System.out.println("[WARNING] Không tìm thấy khách hàng với CMND=" + cmnd);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi query thông tin khách hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách tài khoản của khách hàng
     * 
     * @param cmnd      CMND khách hàng
     * @param tenServer Tên server phân mảnh
     * @return Danh sách tài khoản
     */
    public List<TaiKhoan> layDanhSachTaiKhoan(String cmnd, String tenServer) {
        System.out.println("[DEBUG] Lấy danh sách tài khoản cho CMND=" + cmnd + ", server=" + tenServer);

        List<TaiKhoan> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return danhSach;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.sp_LayDSTaiKhoan_KhachHang(?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, cmnd);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        TaiKhoan tk = new TaiKhoan();
                        tk.setSotk(rs.getString("SOTK"));
                        tk.setSodu(rs.getBigDecimal("SODU"));
                        tk.setNgayMotk(rs.getDate("NGAYMOTK") != null ? rs.getDate("NGAYMOTK").toLocalDate() : null);
                        tk.setMacn(rs.getString("MACN"));
                        tk.setTencn(rs.getString("TENCN"));
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
     * Lấy sao kê tài khoản
     * 
     * @param sotk      Số tài khoản
     * @param tuNgay    Từ ngày
     * @param denNgay   Đến ngày
     * @param tenServer Tên server phân mảnh
     * @return Danh sách giao dịch
     */
    public List<GiaoDich> xemSaoKe(String sotk, LocalDate tuNgay, LocalDate denNgay, String tenServer) {
        System.out.println("[DEBUG] Xem sao kê cho SOTK=" + sotk + ", từ " + tuNgay + " đến " + denNgay);

        List<GiaoDich> danhSach = new ArrayList<>();
        String connectionString = fragmentConfig.getConnectionString(tenServer);
        if (connectionString == null) {
            System.err.println("[ERROR] Không tìm thấy connection string cho server: " + tenServer);
            return danhSach;
        }

        try (Connection conn = DriverManager.getConnection(connectionString, fragmentConfig.getUsername(),
                fragmentConfig.getPassword())) {
            String sql = "{call dbo.SP_XemSKKHACHHANG(?, ?, ?)}";

            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, sotk);
                java.sql.Date sqlTuNgay = java.sql.Date.valueOf(tuNgay);
                java.sql.Date sqlDenNgay = java.sql.Date.valueOf(denNgay);
                stmt.setDate(2, sqlTuNgay);
                stmt.setDate(3, sqlDenNgay);

                System.out.println("[DEBUG SQL] Calling SP with params: SOTK=" + sotk + ", TuNgay=" + sqlTuNgay
                        + ", DenNgay=" + sqlDenNgay);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        GiaoDich gd = new GiaoDich();
                        gd.setSoDuDau(rs.getBigDecimal("Số dư đầu"));
                        gd.setNgay(rs.getTimestamp("Ngày") != null ? rs.getTimestamp("Ngày").toLocalDateTime() : null);
                        gd.setLoaiGiaoDich(rs.getString("Loại giao dịch"));
                        gd.setSoTien(rs.getBigDecimal("Số tiền"));
                        gd.setSoDuSau(rs.getBigDecimal("Số dư sau"));
                        danhSach.add(gd);
                    }
                    System.out.println("[DEBUG] Tìm thấy " + danhSach.size() + " giao dịch");
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Lỗi khi xem sao kê: " + e.getMessage());
            e.printStackTrace();
        }
        return danhSach;
    }
}