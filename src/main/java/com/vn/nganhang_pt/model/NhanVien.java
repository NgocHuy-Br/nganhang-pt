package com.vn.nganhang_pt.model;

import java.util.Date;

public class NhanVien {
    private String maNV;
    private String hoTen;
    private String tenNhom;
    private String maCN;
    private String role; // Role của user trong hệ thống (NGANHANG, CHINHANH)
    private String diaChi;
    private Date ngaySinh;
    private String tenDangNhap;
    private String tenServer;
    private String tenChiNhanh;

    public NhanVien() {
    }

    public NhanVien(String maNV, String hoTen, String tenNhom, String maCN) {
        this.maNV = maNV;
        this.hoTen = hoTen;
        this.tenNhom = tenNhom;
        this.maCN = maCN;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public String getMaCN() {
        return maCN;
    }

    public void setMaCN(String maCN) {
        this.maCN = maCN;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getTenServer() {
        return tenServer;
    }

    public void setTenServer(String tenServer) {
        this.tenServer = tenServer;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }
}
