package com.vn.nganhang_pt.controller;

import com.vn.nganhang_pt.service.DatabaseTestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class DatabaseTestController {

    private final DatabaseTestService databaseTestService;

    public DatabaseTestController(DatabaseTestService databaseTestService) {
        this.databaseTestService = databaseTestService;
    }

    @GetMapping("/test-connection")
    public String testConnection(Model model) {
        Map<String, Object> results = databaseTestService.testConnection();
        model.addAttribute("results", results);
        return "test-connection";
    }

    @GetMapping("/test-nhanvien")
    public String testNhanVien(Model model) {
        List<Map<String, Object>> results = databaseTestService.layDanhSachNhanVien();
        model.addAttribute("results", results);
        model.addAttribute("spName", "SP_Lay_DS_NhanVien");
        return "test-nhanvien";
    }

    @GetMapping("/test-nhanvien-daxoa")
    public String testNhanVienDaXoa(Model model) {
        List<Map<String, Object>> results = databaseTestService.layDanhSachNhanVienDaXoa();
        model.addAttribute("results", results);
        model.addAttribute("spName", "SP_Lay_DS_NhanVien_DaXoa");
        return "test-nhanvien-daxoa";
    }

    @GetMapping("/test-direct-query")
    public String testDirectQuery(Model model) {
        Map<String, Object> result = databaseTestService.testDirectQuery();
        model.addAttribute("result", result);
        return "test-direct-query";
    }

    @GetMapping("/test-them-nhanvien")
    public String showFormThemNhanVien(Model model) {
        // Lấy danh sách chi nhánh để hiển thị trong dropdown
        List<Map<String, Object>> danhSachChiNhanh = databaseTestService.layDanhSachChiNhanh();
        model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);
        return "test-them-nhanvien";
    }

    @PostMapping("/test-them-nhanvien")
    public String themNhanVien(
            @RequestParam("manv") String manv,
            @RequestParam("ho") String ho,
            @RequestParam("ten") String ten,
            @RequestParam("diachi") String diachi,
            @RequestParam("cmnd") String cmnd,
            @RequestParam("phai") String phai,
            @RequestParam("sodt") String sodt,
            @RequestParam("macn") String macn,
            Model model) {

        // Gọi SP thêm nhân viên
        Map<String, Object> result = databaseTestService.themNhanVien(
                manv, ho, ten, diachi, cmnd, phai, sodt, macn);

        // Lấy lại danh sách chi nhánh
        List<Map<String, Object>> danhSachChiNhanh = databaseTestService.layDanhSachChiNhanh();
        model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);

        // Thêm kết quả vào model
        model.addAttribute("result", result);

        return "test-them-nhanvien";
    }

    /**
     * Hiển thị form thêm khách hàng
     */
    @GetMapping("/test-them-khachhang")
    public String showFormThemKhachHang(Model model) {
        List<Map<String, Object>> danhSachChiNhanh = databaseTestService.layDanhSachChiNhanh();
        model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);
        return "test-them-khachhang";
    }

    /**
     * Xử lý form thêm khách hàng
     */
    @PostMapping("/test-them-khachhang")
    public String themKhachHang(
            @RequestParam("cmnd") String cmnd,
            @RequestParam("ho") String ho,
            @RequestParam("ten") String ten,
            @RequestParam("diachi") String diachi,
            @RequestParam("ngaycap") String ngaycap,
            @RequestParam("sodt") String sodt,
            @RequestParam("phai") String phai,
            @RequestParam("macn") String macn,
            Model model) {

        // Gọi service để thêm khách hàng
        Map<String, Object> result = databaseTestService.themKhachHang(
                cmnd, ho, ten, diachi, ngaycap, sodt, phai, macn);

        // Lấy lại danh sách chi nhánh
        List<Map<String, Object>> danhSachChiNhanh = databaseTestService.layDanhSachChiNhanh();
        model.addAttribute("danhSachChiNhanh", danhSachChiNhanh);

        // Thêm kết quả vào model
        model.addAttribute("result", result);

        return "test-them-khachhang";
    }
}
