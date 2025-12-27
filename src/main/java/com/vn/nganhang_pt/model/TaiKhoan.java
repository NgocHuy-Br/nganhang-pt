package com.vn.nganhang_pt.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TaiKhoan {
    private String sotk;
    private BigDecimal sodu;
    private LocalDate ngayMotk;
    private String macn;
    private String tencn;
    private String site;

    public TaiKhoan() {
    }

    public String getSotk() {
        return sotk;
    }

    public void setSotk(String sotk) {
        this.sotk = sotk;
    }

    public BigDecimal getSodu() {
        return sodu;
    }

    public void setSodu(BigDecimal sodu) {
        this.sodu = sodu;
    }

    public LocalDate getNgayMotk() {
        return ngayMotk;
    }

    public void setNgayMotk(LocalDate ngayMotk) {
        this.ngayMotk = ngayMotk;
    }

    public String getMacn() {
        return macn;
    }

    public void setMacn(String macn) {
        this.macn = macn;
    }

    public String getTencn() {
        return tencn;
    }

    public void setTencn(String tencn) {
        this.tencn = tencn;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    // Aliases for service layer
    public String getSoTK() {
        return sotk;
    }

    public void setSoTK(String soTK) {
        this.sotk = soTK;
    }

    public BigDecimal getSoDu() {
        return sodu;
    }

    public void setSoDu(BigDecimal soDu) {
        this.sodu = soDu;
    }

    public java.util.Date getNgayMoTK() {
        if (ngayMotk != null) {
            return java.sql.Date.valueOf(ngayMotk);
        }
        return null;
    }

    public void setNgayMoTK(java.sql.Date ngayMoTK) {
        if (ngayMoTK != null) {
            this.ngayMotk = ngayMoTK.toLocalDate();
        }
    }

    public String getMaCN() {
        return macn;
    }

    public void setMaCN(String maCN) {
        this.macn = maCN;
    }

    public String getTenCN() {
        return tencn;
    }

    public void setTenCN(String tenCN) {
        this.tencn = tenCN;
    }
}
