package com.vn.nganhang_pt.model;

public class ChiNhanh {
    private String tenCN;
    private String tenServer;

    public ChiNhanh() {
    }

    public ChiNhanh(String tenCN, String tenServer) {
        this.tenCN = tenCN;
        this.tenServer = tenServer;
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
