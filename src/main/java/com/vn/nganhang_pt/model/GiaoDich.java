package com.vn.nganhang_pt.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GiaoDich {
    private BigDecimal soDuDau;
    private LocalDateTime ngay;
    private String loaiGiaoDich;
    private BigDecimal soTien;
    private BigDecimal soDuSau;

    public GiaoDich() {
    }

    public BigDecimal getSoDuDau() {
        return soDuDau;
    }

    public void setSoDuDau(BigDecimal soDuDau) {
        this.soDuDau = soDuDau;
    }

    public LocalDateTime getNgay() {
        return ngay;
    }

    public void setNgay(LocalDateTime ngay) {
        this.ngay = ngay;
    }

    public String getLoaiGiaoDich() {
        return loaiGiaoDich;
    }

    public void setLoaiGiaoDich(String loaiGiaoDich) {
        this.loaiGiaoDich = loaiGiaoDich;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public BigDecimal getSoDuSau() {
        return soDuSau;
    }

    public void setSoDuSau(BigDecimal soDuSau) {
        this.soDuSau = soDuSau;
    }
}
