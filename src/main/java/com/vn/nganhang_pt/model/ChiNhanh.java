package com.vn.nganhang_pt.model;

public class ChiNhanh {
    private String maCN;
    private String tenCN;
    private String tenServer;

    public ChiNhanh() {
    }

    // Constructor cho login (chỉ cần tên và server)
    public ChiNhanh(String tenCN, String tenServer) {
        this.tenCN = tenCN;
        this.tenServer = tenServer;
    }

    // Constructor đầy đủ (có cả mã chi nhánh)
    public ChiNhanh(String maCN, String tenCN, String tenServer) {
        this.maCN = maCN;
        this.tenCN = tenCN;
        this.tenServer = tenServer;
    }

    public String getMaCN() {
        return maCN;
    }

    public void setMaCN(String maCN) {
        this.maCN = maCN;
    }

    public String getTenCN() {
        return tenCN;
    }

    public void setTenCN(String tenCN) {
        this.tenCN = tenCN;
    }

    public String getTenServer() {
        return tenServer;
    }

    public void setTenServer(String tenServer) {
        this.tenServer = tenServer;
    }
}
