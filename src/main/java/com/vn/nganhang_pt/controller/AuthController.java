package com.vn.nganhang_pt.controller;

import com.vn.nganhang_pt.model.ChiNhanh;
import com.vn.nganhang_pt.model.KhachHang;
import com.vn.nganhang_pt.model.NhanVien;
import com.vn.nganhang_pt.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Hiển thị trang đăng nhập
     */
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        // Lấy danh sách chi nhánh từ view
        List<ChiNhanh> danhSachChiNhanh = authService.layDanhSachChiNhanh();
        model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);
        return "login";
    }

    /**
     * Xử lý đăng nhập
     */
    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("tenServer") String tenServer,
            HttpSession session,
            Model model) {

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập tên đăng nhập");
            List<ChiNhanh> danhSachChiNhanh = authService.layDanhSachChiNhanh();
            model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);
            return "login";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập mật khẩu");
            List<ChiNhanh> danhSachChiNhanh = authService.layDanhSachChiNhanh();
            model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);
            return "login";
        }

        if (tenServer == null || tenServer.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng chọn chi nhánh");
            List<ChiNhanh> danhSachChiNhanh = authService.layDanhSachChiNhanh();
            model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);
            return "login";
        }

        // Thử đăng nhập
        Object userInfo = authService.dangNhap(username, password, tenServer);

        if (userInfo != null) {
            // Nếu là khách hàng và chọn Trụ cở (NH03) -> không cho phép
            if (userInfo instanceof KhachHang && tenServer.contains("NH03")) {
                model.addAttribute("error",
                        "Tài khoản khách hàng không có quyền truy cập Trụ cở (Tra cứu). Vui lòng chọn Chi nhánh Bến Thành hoặc Tân Định.");
                List<ChiNhanh> danhSachChiNhanh = authService.layDanhSachChiNhanh();
                model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);
                model.addAttribute("username", username);
                model.addAttribute("selectedServer", tenServer);
                return "login";
            }

            // Lấy danh sách chi nhánh để tìm tên chi nhánh đã chọn
            List<ChiNhanh> danhSachChiNhanh = authService.layDanhSachChiNhanh();
            String tenChiNhanh = "";
            for (ChiNhanh cn : danhSachChiNhanh) {
                if (cn.getTenServer().equals(tenServer)) {
                    tenChiNhanh = cn.getTenCN();
                    break;
                }
            }

            // Lưu vào session tùy theo loại user
            if (userInfo instanceof NhanVien) {
                NhanVien nhanVien = (NhanVien) userInfo;

                // Lấy thông tin đầy đủ từ SP_ThongTinDangNhapHienTai
                try {
                    System.out.println("[DEBUG] Gọi SP_ThongTinDangNhapHienTai với maNV=" + nhanVien.getMaNV()
                            + ", role=" + nhanVien.getRole() + ", server=" + tenServer);
                    NhanVien thongTinDayDu = authService.layThongTinDayDuNhanVien(
                            nhanVien.getMaNV(),
                            nhanVien.getRole(),
                            tenServer);

                    if (thongTinDayDu != null) {
                        System.out.println("[DEBUG] Nhận được thông tin đầy đủ: ho=" + thongTinDayDu.getHo() + ", ten="
                                + thongTinDayDu.getTen() + ", cmnd=" + thongTinDayDu.getCmnd());
                        // Copy thông tin đầy đủ, giữ lại tenDangNhap và ngaySinh từ object cũ
                        thongTinDayDu.setTenDangNhap(nhanVien.getTenDangNhap());
                        thongTinDayDu.setNgaySinh(nhanVien.getNgaySinh());
                        nhanVien = thongTinDayDu;
                    } else {
                        System.out.println("[WARNING] SP_ThongTinDangNhapHienTai trả về null");
                    }
                } catch (Exception e) {
                    System.err.println("[WARNING] Không lấy được thông tin đầy đủ: " + e.getMessage());
                    e.printStackTrace();
                    // Vẫn tiếp tục với thông tin hiện có
                }

                // Set thêm thông tin server và chi nhánh vào object
                nhanVien.setTenServer(tenServer);
                nhanVien.setTenChiNhanh(tenChiNhanh);

                session.setAttribute("userType", "NHANVIEN");
                session.setAttribute("userInfo", nhanVien);
                session.setAttribute("nhanVien", nhanVien); // Giữ lại cho backward compatibility
            } else if (userInfo instanceof KhachHang) {
                KhachHang khachHang = (KhachHang) userInfo;

                // Lấy thông tin đầy đủ từ bảng KHACHHANG
                try {
                    System.out.println("[DEBUG CONTROLLER] Khách hàng trước khi query: cmnd=" + khachHang.getCmnd()
                            + ", hoten=" + khachHang.getHoten() + ", server=" + tenServer);

                    KhachHang thongTinDayDu = authService.layThongTinDayDuKhachHang(
                            khachHang.getCmnd(),
                            tenServer);

                    if (thongTinDayDu != null) {
                        System.out.println("[DEBUG CONTROLLER] Nhận được thông tin đầy đủ khách hàng: ho="
                                + thongTinDayDu.getHo() + ", ten=" + thongTinDayDu.getTen() + ", cmnd="
                                + thongTinDayDu.getCmnd() + ", diaChi=" + thongTinDayDu.getDiaChi() + ", phai="
                                + thongTinDayDu.getPhai() + ", soDT=" + thongTinDayDu.getSoDT());

                        // Copy thông tin đầy đủ, giữ lại các trường từ object cũ
                        thongTinDayDu.setMakh(khachHang.getMakh());
                        thongTinDayDu.setSotk(khachHang.getSotk());
                        thongTinDayDu.setSodu(khachHang.getSodu());
                        thongTinDayDu.setRole(khachHang.getRole());
                        thongTinDayDu.setTennhom(khachHang.getTennhom());
                        khachHang = thongTinDayDu;

                        System.out.println("[DEBUG CONTROLLER] Đã update khách hàng: ho=" + khachHang.getHo()
                                + ", ten=" + khachHang.getTen() + ", hoten=" + khachHang.getHoten());
                    } else {
                        System.out.println("[WARNING CONTROLLER] Không query được thông tin khách hàng từ CMND");
                    }
                } catch (Exception e) {
                    System.err.println("[WARNING CONTROLLER] Lỗi khi query thông tin khách hàng: "
                            + e.getMessage());
                    e.printStackTrace();
                    // Vẫn tiếp tục với thông tin hiện có
                }

                // Set thêm thông tin server và chi nhánh vào object
                khachHang.setTenServer(tenServer);
                khachHang.setTenChiNhanh(tenChiNhanh);

                session.setAttribute("userType", "KHACHHANG");
                session.setAttribute("userInfo", khachHang);
                session.setAttribute("khachHang", khachHang);
            }

            session.setAttribute("username", username);
            session.setAttribute("tenServer", tenServer);
            session.setAttribute("tenChiNhanh", tenChiNhanh);

            // Redirect về trang home
            return "redirect:/home";
        } else {
            // Đăng nhập thất bại
            model.addAttribute("error",
                    "Đăng nhập thất bại! Vui lòng kiểm tra lại tên đăng nhập, mật khẩu và chi nhánh.");
            List<ChiNhanh> danhSachChiNhanh = authService.layDanhSachChiNhanh();
            model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);
            model.addAttribute("username", username);
            model.addAttribute("selectedServer", tenServer);
            return "login";
        }
    }

    /**
     * Trang home sau khi đăng nhập
     */
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        // Kiểm tra loại user
        String userType = (String) session.getAttribute("userType");

        if (userType == null) {
            // Chưa đăng nhập, redirect về trang login
            return "redirect:/login";
        }

        // Thêm thông tin chung vào model
        model.addAttribute("userType", userType);
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("tenServer", session.getAttribute("tenServer"));
        model.addAttribute("tenChiNhanh", session.getAttribute("tenChiNhanh"));

        // Thêm thông tin cụ thể tùy theo loại user
        if ("NHANVIEN".equals(userType)) {
            NhanVien nhanVien = (NhanVien) session.getAttribute("nhanVien");
            model.addAttribute("nhanVien", nhanVien);
            // Điều hướng nhân viên đến dashboard
            return "staff-dashboard";
        } else if ("KHACHHANG".equals(userType)) {
            KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");
            model.addAttribute("khachHang", khachHang);
            // Điều hướng khách hàng đến customer dashboard
            return "customer-dashboard";
        }

        return "home";
    }

    /**
     * Đăng xuất
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /**
     * Redirect root về login
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}
