package com.vn.nganhang_pt.model;

public class KhachHang {
    private String makh; // CMND
    private String hoten;
    private String tennhom;
    private String macn;
    private String sotk; // Số tài khoản
    private String sodu; // Số dư
    private String role; // Role trong hệ thống

    // Thông tin bổ sung
    private String cmnd;
    private String diaChi;
    private String phai;
    private String soDT;
    private String tenChiNhanh;
    private String tenServer;

    public KhachHang() {
    }

    public KhachHang(String makh, String hoten, String tennhom, String macn, String sotk, String sodu) {
        this.makh = makh;
        this.hoten = hoten;
        this.tennhom = tennhom;
        this.macn = macn;
        this.sotk = sotk;
        this.sodu = sodu;
    }

    public String getMakh() {
        return makh;
    }

    public void setMakh(String makh) {
        this.makh = makh;
    }

    public String getHoten() {
        return hoten;
    }

    public void setHoten(String hoten) {
        this.hoten = hoten;
    }

    public String getTennhom() {
        return tennhom;
    }

    public void setTennhom(String tennhom) {
        this.tennhom = tennhom;
    }

    public String getMacn() {
        return macn;
    }

    public void setMacn(String macn) {
        this.macn = macn;
    }

    public String getSotk() {
        return sotk;
    }

    public void setSotk(String sotk) {
        this.sotk = sotk;
    }

    public String getSodu() {
        return sodu;
    }

    public void setSodu(String sodu) {
        this.sodu = sodu;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCmnd() {
        return cmnd;
    }

    public void setCmnd(String cmnd) {
        this.cmnd = cmnd;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getPhai() {
        return phai;
    }

    public void setPhai(String phai) {
        this.phai = phai;
    }

    public String getSoDT() {
        return soDT;
    }

    public void setSoDT(String soDT) {
        this.soDT = soDT;
    }

    public String getTenChiNhanh() {
        return tenChiNhanh;
    }

    public void setTenChiNhanh(String tenChiNhanh) {
        this.tenChiNhanh = tenChiNhanh;
    }

    public String getTenServer() {
        return tenServer;
    }

    public void setTenServer(String tenServer) {
        this.tenServer = tenServer;
    }
}
