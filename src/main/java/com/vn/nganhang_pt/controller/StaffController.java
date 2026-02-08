package com.vn.nganhang_pt.controller;

import com.vn.nganhang_pt.model.ChiNhanh;
import com.vn.nganhang_pt.model.GiaoDich;
import com.vn.nganhang_pt.model.KhachHang;
import com.vn.nganhang_pt.model.NhanVien;
import com.vn.nganhang_pt.model.TaiKhoan;
import com.vn.nganhang_pt.service.BaoCaoService;
import com.vn.nganhang_pt.service.GiaoDichService;
import com.vn.nganhang_pt.service.KhachHangService;
import com.vn.nganhang_pt.service.NhanVienService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private KhachHangService khachHangService;

    @Autowired
    private GiaoDichService giaoDichService;

    @Autowired
    private BaoCaoService baoCaoService;

    /**
     * Lấy danh sách nhân viên
     */
    @GetMapping("/nhan-vien")
    @ResponseBody
    public List<NhanVien> layDanhSachNhanVien(HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out.println("[DEBUG StaffController] Lấy DS nhân viên từ server=" + tenServer);
        return nhanVienService.layDanhSachNhanVien(tenServer, username, password);
    }

    /**
     * Lấy danh sách nhân viên theo chi nhánh (cho role NGANHANG)
     */
    @GetMapping("/nhan-vien/theo-chi-nhanh/{maCN}")
    @ResponseBody
    public List<NhanVien> layDanhSachNhanVienTheoChiNhanh(@PathVariable String maCN, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out
                .println("[DEBUG StaffController] Lấy DS nhân viên theo chi nhánh=" + maCN + " từ server=" + tenServer);
        return nhanVienService.layDanhSachNhanVienTheoChiNhanh(tenServer, maCN, username, password);
    }

    /**
     * Lấy danh sách nhân viên đã xóa
     */
    @GetMapping("/nhan-vien/da-xoa")
    @ResponseBody
    public List<NhanVien> layDanhSachNhanVienDaXoa(HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out.println("[DEBUG StaffController] Lấy DS nhân viên đã xóa từ server=" + tenServer);
        return nhanVienService.layDanhSachNhanVienDaXoa(tenServer, username, password);
    }

    /**
     * Lấy danh sách nhân viên đã xóa theo chi nhánh (cho role NGANHANG)
     */
    @GetMapping("/nhan-vien/da-xoa/theo-chi-nhanh/{maCN}")
    @ResponseBody
    public List<NhanVien> layDanhSachNhanVienDaXoaTheoChiNhanh(@PathVariable String maCN, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out.println(
                "[DEBUG StaffController] Lấy DS nhân viên đã xóa theo chi nhánh=" + maCN + " từ server=" + tenServer);
        return nhanVienService.layDanhSachNhanVienDaXoaTheoChiNhanh(tenServer, maCN, username, password);
    }

    /**
     * Lấy danh sách chi nhánh
     */
    @GetMapping("/chi-nhanh")
    @ResponseBody
    public List<ChiNhanh> layDanhSachChiNhanh(HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        if ("NGANHANG".equalsIgnoreCase(nhanVien.getRole())) {
            System.out.println("[DEBUG StaffController] NGANHANG: lấy DS chi nhánh từ trụ sở");
            return nhanVienService.layDanhSachChiNhanhTuTruSo();
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out.println("[DEBUG StaffController] Lấy DS chi nhánh từ server=" + tenServer);
        return nhanVienService.layDanhSachChiNhanh(tenServer, username, password);
    }

    /**
     * Thêm nhân viên mới
     */
    @PostMapping("/nhan-vien")
    @ResponseBody
    public Map<String, Object> themNhanVien(@RequestBody NhanVien nv, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out.println("[DEBUG StaffController] Thêm nhân viên mới: " + nv.getMaNV());

        int result = nhanVienService.themNhanVien(nv, tenServer, username, password);
        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("message", getMessageByCode(result, "thêm"));
        return response;
    }

    /**
     * Sửa nhân viên
     */
    @PutMapping("/nhan-vien/{maNV}")
    @ResponseBody
    public Map<String, Object> suaNhanVien(@PathVariable String maNV, @RequestBody NhanVien nv,
            HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        nv.setMaNV(maNV); // Đảm bảo mã NV đúng
        System.out.println("[DEBUG StaffController] Sửa nhân viên: " + maNV);

        int result = nhanVienService.suaNhanVien(nv, tenServer, username, password);
        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("message", getMessageByCode(result, "sửa"));
        return response;
    }

    /**
     * Xóa nhân viên (soft delete)
     */
    @DeleteMapping("/nhan-vien/{maNV}")
    @ResponseBody
    public Map<String, Object> xoaNhanVien(@PathVariable String maNV, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out.println("[DEBUG StaffController] Xóa nhân viên: " + maNV);

        int result = nhanVienService.xoaNhanVien(maNV, tenServer, username, password);
        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("message", getMessageByCode(result, "xóa"));
        return response;
    }

    /**
     * Phục hồi nhân viên đã xóa
     */
    @PostMapping("/nhan-vien/{maNV}/phuc-hoi")
    @ResponseBody
    public Map<String, Object> phucHoiNhanVien(@PathVariable String maNV, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out.println("[DEBUG StaffController] Phục hồi nhân viên: " + maNV);

        int result = nhanVienService.phucHoiNhanVien(maNV, tenServer, username, password);
        Map<String, Object> response = new HashMap<>();
        response.put("result", result);
        response.put("message", getMessageByCode(result, "phục hồi"));
        return response;
    }

    /**
     * Chuyển nhân viên sang chi nhánh khác
     */
    @PostMapping("/nhan-vien/{maNV}/chuyen-chi-nhanh")
    @ResponseBody
    public Map<String, Object> chuyenChiNhanh(@PathVariable String maNV, @RequestBody Map<String, String> payload,
            HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        String maCNMoi = payload.get("maCNMoi");
        String maNVMoi = payload.get("maNVMoi");

        System.out.println("[DEBUG StaffController] Chuyển nhân viên " + maNV + " sang chi nhánh " + maCNMoi
                + " với mã mới: " + maNVMoi);

        Map<String, Object> result = nhanVienService.chuyenChiNhanh(maNV, maCNMoi, maNVMoi, tenServer, username,
                password);
        return result;
    }

    /**
     * Helper: Lấy message theo mã kết quả
     */
    private String getMessageByCode(int result, String action) {
        switch (result) {
            case 1:
                return "Thành công";
            case -1:
                if (action.equals("thêm") || action.equals("sửa"))
                    return "Chi nhánh không tồn tại";
                else if (action.equals("xóa") || action.equals("phục hồi"))
                    return "Nhân viên không tồn tại";
                else
                    return "Lỗi: Không tìm thấy";
            case -2:
                if (action.equals("thêm"))
                    return "Mã nhân viên đã tồn tại";
                else if (action.equals("sửa"))
                    return "Chi nhánh không hợp lệ";
                else if (action.equals("xóa"))
                    return "Nhân viên đã bị xóa";
                else if (action.equals("phục hồi"))
                    return "Nhân viên chưa bị xóa";
                else
                    return "Lỗi: Dữ liệu không hợp lệ";
            case -3:
                if (action.equals("thêm") || action.equals("sửa"))
                    return "CMND đã tồn tại";
                else
                    return "Lỗi không xác định";
            case -4:
                return "Giới tính không hợp lệ (phải là Nam hoặc Nữ)";
            case -99:
                return "Lỗi hệ thống";
            default:
                return "Lỗi không xác định";
        }
    }

    /* ==================== KHÁCH HÀNG APIs ==================== */

    /**
     * Lấy danh sách khách hàng
     */
    @GetMapping("/khach-hang")
    @ResponseBody
    public List<KhachHang> layDanhSachKhachHang(HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String role = nhanVien.getRole();

        // Role NGANHANG: Xem tất cả chi nhánh
        if ("NGANHANG".equals(role)) {
            System.out.println("[DEBUG] Role NGANHANG - Lấy KH tất cả chi nhánh");
            return khachHangService.layDanhSachKhachHangTatCaCN(tenServer);
        } else {
            // Role CHINHANH: Chỉ xem chi nhánh hiện tại
            System.out.println("[DEBUG] Role CHINHANH - Lấy KH chi nhánh hiện tại");
            return khachHangService.layDanhSachKhachHang(tenServer);
        }
    }

    /**
     * Lấy danh sách khách hàng theo chi nhánh (cho role NGANHANG)
     */
    @GetMapping("/khach-hang/theo-chi-nhanh/{maCN}")
    @ResponseBody
    public List<KhachHang> layDanhSachKhachHangTheoChiNhanh(@PathVariable String maCN, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        System.out.println(
                "[DEBUG StaffController] Lấy DS khách hàng theo chi nhánh=" + maCN + " từ server=" + tenServer);
        return khachHangService.layDanhSachKhachHangTheoChiNhanh(tenServer, maCN, username, password);
    }

    /**
     * Thêm khách hàng mới
     */
    @PostMapping("/khach-hang")
    @ResponseBody
    public Map<String, Object> themKhachHang(@RequestBody Map<String, String> data, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String cmnd = data.get("cmnd");
        String ho = data.get("ho");
        String ten = data.get("ten");
        String diaChi = data.get("diaChi");
        Date ngayCap = Date.valueOf(data.get("ngayCap"));
        String soDT = data.get("soDT");
        String phai = data.get("phai");
        String maCN = data.get("maCN");

        return khachHangService.themKhachHang(tenServer, cmnd, ho, ten, diaChi, ngayCap, soDT, phai, maCN);
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    @PutMapping("/khach-hang/{cmnd}")
    @ResponseBody
    public Map<String, Object> capNhatKhachHang(@PathVariable String cmnd,
            @RequestBody Map<String, String> data,
            HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String ho = data.get("ho");
        String ten = data.get("ten");
        String diaChi = data.get("diaChi");
        String soDT = data.get("soDT");
        String phai = data.get("phai");

        return khachHangService.capNhatKhachHang(tenServer, cmnd, ho, ten, diaChi, soDT, phai);
    }

    /**
     * Lấy danh sách tài khoản của khách hàng
     */
    @GetMapping("/khach-hang/{cmnd}/tai-khoan")
    @ResponseBody
    public List<TaiKhoan> layDanhSachTaiKhoanKH(@PathVariable String cmnd, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        return khachHangService.layDanhSachTaiKhoanKH(tenServer, cmnd);
    }

    /**
     * Tìm khách hàng theo CMND
     */
    @GetMapping("/khach-hang/tim-theo-cmnd/{cmnd}")
    @ResponseBody
    public Map<String, Object> timKhachHangTheoCMND(@PathVariable String cmnd, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        return khachHangService.timKhachHangTheoCMND(tenServer, cmnd);
    }

    /**
     * Mở tài khoản cho khách hàng
     */
    @PostMapping("/khach-hang/{cmnd}/mo-tai-khoan")
    @ResponseBody
    public Map<String, Object> moTaiKhoan(@PathVariable String cmnd,
            @RequestBody Map<String, String> data,
            HttpSession session) {
        try {
            NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
            if (nhanVien == null) {
                return Map.of("result", -1, "message", "Chưa đăng nhập");
            }

            String tenServer = nhanVien.getTenServer();
            String soTK = data.get("soTK");
            String maCN = data.get("maCN");
            String maNV = nhanVien.getMaNV();

            System.out.println("[DEBUG] Mở tài khoản - Server: " + tenServer + ", SoTK: " + soTK +
                    ", CMND: " + cmnd + ", MaCN: " + maCN + ", MaNV: " + maNV);

            Map<String, Object> result = khachHangService.moTaiKhoan(tenServer, soTK, cmnd, maCN, maNV);
            System.out.println("[DEBUG] Kết quả mở tài khoản: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("[ERROR] Exception trong moTaiKhoan: " + e.getMessage());
            e.printStackTrace();
            return Map.of("result", -99, "message", "Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Tạo tài khoản đăng nhập
     */
    @PostMapping("/tao-login")
    @ResponseBody
    public Map<String, Object> taoTaiKhoanDangNhap(@RequestBody Map<String, String> data, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String loginName = data.get("loginName");
        String password = data.get("password");
        String userName = data.get("userName");
        String role = data.get("role");

        // Lấy credentials của user hiện tại từ session
        String currentUsername = (String) session.getAttribute("username");
        String currentPassword = (String) session.getAttribute("password");

        System.out.println("[DEBUG StaffController] Tạo login - currentUsername=" + currentUsername
                + ", currentPassword=" + (currentPassword != null ? "***" : "NULL"));

        return nhanVienService.taoTaiKhoanDangNhap(tenServer, loginName, password, userName, role,
                currentUsername, currentPassword);
    }

    /**
     * Lấy thông tin tài khoản cho giao dịch (tìm tất cả chi nhánh)
     */
    @GetMapping("/giao-dich/thong-tin-tai-khoan/{soTK}")
    @ResponseBody
    public Map<String, Object> layThongTinTaiKhoan(@PathVariable String soTK, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        String role = nhanVien.getRole(); // Lấy role từ session
        String tenChiNhanh = nhanVien.getTenChiNhanh(); // Lấy tên chi nhánh

        return giaoDichService.layThongTinTaiKhoan(soTK, tenServer, username, password, role, tenChiNhanh, true); // true
                                                                                                                  // =
                                                                                                                  // transaction
    }

    /**
     * Lấy thông tin tài khoản cho sao kê (chỉ tìm chi nhánh hiện tại)
     */
    @GetMapping("/giao-dich/thong-tin-tai-khoan-sao-ke/{soTK}")
    @ResponseBody
    public Map<String, Object> layThongTinTaiKhoanSaoKe(@PathVariable String soTK, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        String role = nhanVien.getRole(); // Lấy role từ session
        String tenChiNhanh = nhanVien.getTenChiNhanh(); // Lấy tên chi nhánh

        return giaoDichService.layThongTinTaiKhoan(soTK, tenServer, username, password, role, tenChiNhanh, false); // false
                                                                                                                   // =
                                                                                                                   // account
                                                                                                                   // statement
    }

    /**
     * Rút tiền
     */
    @PostMapping("/giao-dich/rut-tien")
    @ResponseBody
    public Map<String, Object> rutTien(@RequestBody Map<String, String> data, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String soTK = data.get("soTK");
        BigDecimal soTien = new BigDecimal(data.get("soTien"));
        String maNV = nhanVien.getMaNV();

        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        String tenChiNhanh = nhanVien.getTenChiNhanh(); // Lấy tên chi nhánh

        return giaoDichService.rutTien(soTK, soTien, maNV, tenServer, username, password, tenChiNhanh);
    }

    /**
     * Gửi tiền
     */
    @PostMapping("/giao-dich/goi-tien")
    @ResponseBody
    public Map<String, Object> goiTien(@RequestBody Map<String, String> data, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String soTK = data.get("soTK");
        BigDecimal soTien = new BigDecimal(data.get("soTien"));
        String maNV = nhanVien.getMaNV();

        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        String tenChiNhanh = nhanVien.getTenChiNhanh(); // Lấy tên chi nhánh

        return giaoDichService.goiTien(soTK, soTien, maNV, tenServer, username, password, tenChiNhanh);
    }

    /**
     * Chuyển tiền
     */
    @PostMapping("/giao-dich/chuyen-tien")
    @ResponseBody
    public Map<String, Object> chuyenTien(@RequestBody Map<String, String> data, HttpSession session) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
        if (nhanVien == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = nhanVien.getTenServer();
        String soTKGui = data.get("soTKGui");
        String soTKNhan = data.get("soTKNhan");
        BigDecimal soTien = new BigDecimal(data.get("soTien"));
        String maNV = nhanVien.getMaNV();

        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        String tenChiNhanh = nhanVien.getTenChiNhanh(); // Lấy tên chi nhánh

        return giaoDichService.chuyenTien(soTKGui, soTKNhan, soTien, maNV, tenServer, username, password, tenChiNhanh);
    }

    /**
     * Sao kê giao dịch
     */
    @PostMapping("/bao-cao/sao-ke")
    @ResponseBody
    public Map<String, Object> saoKeGiaoDich(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
            if (nhanVien == null) {
                response.put("success", false);
                response.put("message", "Chưa đăng nhập");
                return response;
            }

            String soTK = data.get("soTK");
            LocalDate tuNgay = LocalDate.parse(data.get("tuNgay"));
            LocalDate denNgay = LocalDate.parse(data.get("denNgay"));

            String tenServer = nhanVien.getTenServer();
            String username = (String) session.getAttribute("username");
            String password = (String) session.getAttribute("password");
            String role = nhanVien.getRole(); // Lấy role từ session

            List<GiaoDich> transactions = baoCaoService.saoKeGiaoDich(soTK, tuNgay, denNgay, tenServer, username,
                    password, role);

            response.put("success", true);
            response.put("transactions", transactions);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Xuất PDF sao kê giao dịch
     */
    @PostMapping("/bao-cao/sao-ke/pdf")
    public ResponseEntity<byte[]> xuatPDFSaoKe(@RequestBody Map<String, String> data, HttpSession session) {
        try {
            NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
            if (nhanVien == null) {
                return ResponseEntity.status(401).body(null);
            }

            String soTK = data.get("soTK");
            LocalDate tuNgay = LocalDate.parse(data.get("tuNgay"));
            LocalDate denNgay = LocalDate.parse(data.get("denNgay"));

            String tenServer = nhanVien.getTenServer();
            String username = (String) session.getAttribute("username");
            String password = (String) session.getAttribute("password");
            String role = nhanVien.getRole(); // Lấy role từ session

            // Lấy thông tin tài khoản
            String chuTK = data.get("chuTK");
            String chiNhanh = data.get("chiNhanh");
            String nguoiXuat = nhanVien.getHo() + " " + nhanVien.getTen();

            byte[] pdfBytes = baoCaoService.xuatPDFSaoKe(soTK, tuNgay, denNgay, tenServer, username, password, chuTK,
                    chiNhanh, nguoiXuat, role);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("SaoKeTaiKhoan_" + soTK + "_" + tuNgay + "_" + denNgay + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Liệt kê tài khoản mở mới
     */
    @PostMapping("/bao-cao/tai-khoan-moi")
    @ResponseBody
    public Map<String, Object> lietKeTaiKhoanMoi(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
            if (nhanVien == null) {
                response.put("success", false);
                response.put("message", "Chưa đăng nhập");
                return response;
            }

            LocalDate tuNgay = LocalDate.parse(data.get("tuNgay"));
            LocalDate denNgay = LocalDate.parse(data.get("denNgay"));
            String maCNRequest = data.get("maCN");

            String tenServer = nhanVien.getTenServer();
            String role = nhanVien.getRole();
            String username = (String) session.getAttribute("username");
            String password = (String) session.getAttribute("password");

            // Luôn dùng SP_LietKeTaiKhoanMoiMo_TheoChiNhanh
            // - Role NGANHANG: maCNRequest từ dropdown (bắt buộc chọn)
            // - Role CHINHANH: maCN từ nhân viên (tự động)
            String maCN = (maCNRequest != null && !maCNRequest.isEmpty())
                    ? maCNRequest
                    : nhanVien.getMaCN();

            List<Map<String, Object>> accounts = baoCaoService.lietKeTaiKhoanMoiTheoChiNhanh(
                    tuNgay, denNgay, maCN, tenServer, username, password);

            response.put("success", true);
            response.put("accounts", accounts);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Xuất PDF danh sách tài khoản mở mới
     */
    @PostMapping("/bao-cao/tai-khoan-moi/pdf")
    public ResponseEntity<byte[]> xuatPDFTaiKhoanMoi(@RequestBody Map<String, String> data, HttpSession session) {
        try {
            NhanVien nhanVien = (NhanVien) session.getAttribute("userInfo");
            if (nhanVien == null) {
                return ResponseEntity.status(401).body(null);
            }

            LocalDate tuNgay = LocalDate.parse(data.get("tuNgay"));
            LocalDate denNgay = LocalDate.parse(data.get("denNgay"));
            String maCNRequest = data.get("maCN");

            String tenServer = nhanVien.getTenServer();
            String role = nhanVien.getRole();
            String username = (String) session.getAttribute("username");
            String password = (String) session.getAttribute("password");

            // Lấy tên người xuất
            String nguoiXuat = nhanVien.getHo() + " " + nhanVien.getTen();

            byte[] pdfBytes;
            if (maCNRequest != null && !maCNRequest.isEmpty()) {
                pdfBytes = baoCaoService.xuatPDFTaiKhoanMoiTheoChiNhanh(tuNgay, denNgay, maCNRequest, tenServer,
                        username,
                        password, nguoiXuat);
            } else {
                String maCN = nhanVien.getMaCN();
                pdfBytes = baoCaoService.xuatPDFTaiKhoanMoi(tuNgay, denNgay, role, maCN, tenServer, username,
                        password, nguoiXuat);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("DanhSachTaiKhoanMoi_" + tuNgay + "_" + denNgay + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/khach-hang/{cmnd}")
    public ResponseEntity<Map<String, Object>> xoaKhachHang(@PathVariable String cmnd,
            HttpSession session) {
        try {
            String tenServer = (String) session.getAttribute("tenServer");
            if (tenServer == null) {
                return ResponseEntity.status(401).body(Map.of("result", -1, "message", "Chưa đăng nhập"));
            }

            Map<String, Object> result = khachHangService.xoaKhachHang(tenServer, cmnd);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("result", -1, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/khach-hang/phuc-hoi")
    public ResponseEntity<Map<String, Object>> phucHoiKhachHang(@RequestBody KhachHang khachHang,
            HttpSession session) {
        try {
            String tenServer = (String) session.getAttribute("tenServer");
            if (tenServer == null) {
                return ResponseEntity.status(401).body(Map.of("result", -1, "message", "Chưa đăng nhập"));
            }

            Map<String, Object> result = khachHangService.phucHoiKhachHang(tenServer, khachHang);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("result", -1, "message", "Lỗi: " + e.getMessage()));
        }
    }

    @PostMapping("/tai-khoan/tao-lai")
    public ResponseEntity<Map<String, Object>> taoLaiTaiKhoan(@RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            String tenServer = (String) session.getAttribute("tenServer");
            if (tenServer == null) {
                return ResponseEntity.status(401).body(Map.of("result", -1, "message", "Chưa đăng nhập"));
            }

            String soTK = (String) request.get("soTK");
            String cmnd = (String) request.get("cmnd");
            String maCN = (String) request.get("maCN");
            String ngayMoTKStr = (String) request.get("ngayMoTK");
            Double soDu = ((Number) request.get("soDu")).doubleValue();

            // Parse ngayMoTK from string to Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = sdf.parse(ngayMoTKStr);
            java.sql.Date ngayMoTK = new java.sql.Date(utilDate.getTime());

            Map<String, Object> result = khachHangService.taoLaiTaiKhoan(tenServer, soTK, cmnd, maCN, ngayMoTK, soDu);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("result", -1, "message", "Lỗi: " + e.getMessage()));
        }
    }
}