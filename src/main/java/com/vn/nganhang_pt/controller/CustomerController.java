package com.vn.nganhang_pt.controller;

import com.vn.nganhang_pt.model.GiaoDich;
import com.vn.nganhang_pt.model.KhachHang;
import com.vn.nganhang_pt.model.TaiKhoan;
import com.vn.nganhang_pt.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private AuthService authService;

    /**
     * Lấy danh sách tài khoản của khách hàng
     */
    @GetMapping("/tai-khoan")
    @ResponseBody
    public List<TaiKhoan> layDanhSachTaiKhoan(HttpSession session) {
        KhachHang khachHang = (KhachHang) session.getAttribute("userInfo");
        if (khachHang == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = khachHang.getTenServer();
        String cmnd = khachHang.getCmnd();

        System.out.println("[DEBUG CustomerController] Lấy DS tài khoản cho CMND=" + cmnd + ", server=" + tenServer);
        return authService.layDanhSachTaiKhoan(cmnd, tenServer);
    }

    /**
     * Xem sao kê tài khoản
     */
    @GetMapping("/sao-ke")
    @ResponseBody
    public List<GiaoDich> xemSaoKe(
            @RequestParam String sotk,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            HttpSession session) {

        KhachHang khachHang = (KhachHang) session.getAttribute("userInfo");
        if (khachHang == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }

        String tenServer = khachHang.getTenServer();

        System.out.println("[DEBUG CustomerController] Xem sao kê SOTK=" + sotk + ", từ " + tuNgay + " đến " + denNgay);
        return authService.xemSaoKe(sotk, tuNgay, denNgay, tenServer);
    }
}
