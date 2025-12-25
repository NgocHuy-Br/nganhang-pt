package com.vn.nganhang_pt.controller;

import com.vn.nganhang_pt.model.ChiNhanh;
import com.vn.nganhang_pt.model.NhanVien;
import com.vn.nganhang_pt.service.NhanVienService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private NhanVienService nhanVienService;

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
        System.out.println("[DEBUG StaffController] Lấy DS nhân viên từ server=" + tenServer);
        return nhanVienService.layDanhSachNhanVien(tenServer);
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
        System.out.println("[DEBUG StaffController] Lấy DS nhân viên đã xóa từ server=" + tenServer);
        return nhanVienService.layDanhSachNhanVienDaXoa(tenServer);
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

        String tenServer = nhanVien.getTenServer();
        System.out.println("[DEBUG StaffController] Lấy DS chi nhánh từ server=" + tenServer);
        return nhanVienService.layDanhSachChiNhanh(tenServer);
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
        System.out.println("[DEBUG StaffController] Thêm nhân viên mới: " + nv.getMaNV());

        int result = nhanVienService.themNhanVien(nv, tenServer);
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
        nv.setMaNV(maNV); // Đảm bảo mã NV đúng
        System.out.println("[DEBUG StaffController] Sửa nhân viên: " + maNV);

        int result = nhanVienService.suaNhanVien(nv, tenServer);
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
        System.out.println("[DEBUG StaffController] Xóa nhân viên: " + maNV);

        int result = nhanVienService.xoaNhanVien(maNV, tenServer);
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
        System.out.println("[DEBUG StaffController] Phục hồi nhân viên: " + maNV);

        int result = nhanVienService.phucHoiNhanVien(maNV, tenServer);
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
        String maCNMoi = payload.get("maCNMoi");
        System.out.println("[DEBUG StaffController] Chuyển nhân viên " + maNV + " sang chi nhánh " + maCNMoi);

        Map<String, Object> result = nhanVienService.chuyenChiNhanh(maNV, maCNMoi, tenServer);
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
}
