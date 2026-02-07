# 📋 HƯỚNG DẪN HOÀN THIỆN CHỨC NĂNG BÁO CÁO CHO ROLE NGANHANG

## 🎯 Tổng quan vấn đề đã sửa

### ❌ Vấn đề phát hiện:
1. **SP_LietKeTaiKhoanMoiMo KHÔNG TỒN TẠI** - Code Java gọi SP không có
2. **Thiếu RTRIM** trong các SP => JOIN thất bại do NCHAR trailing spaces
3. **SP_XemSKKHACHHANG_TatCaChiNhanh chưa hoàn thiện** cross-site logic
4. **Logic controller phức tạp** - không cần thiết phải tách 2 method

---

## ✅ Giải pháp đã triển khai

### 📁 File 1: `FIX_Complete_BaoCao_System.sql`
**Chức năng:** Tạo đầy đủ các SP với RTRIM  
**Các SP đã tạo/sửa:**

#### 1️⃣ SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh
- **Mục đích:** Role NGANHANG tra cứu thông tin KH qua LINK2
- **Đặc điểm:** 
  - JOIN qua LINK2 (site trung tâm)
  - **RTRIM** ở mọi nơi: `JOIN ... ON RTRIM(TK.CMND) = RTRIM(KH.CMND)`
  - JOIN `TK.MACN` với `CN.MACN` (đúng - lấy chi nhánh của TK)

#### 2️⃣ SP_LietKeTaiKhoanMoiMo_TheoChiNhanh
- **Mục đích:** Liệt kê TK mới mở theo chi nhánh cụ thể
- **Đặc điểm:**
  - **Cross-site support:** Tìm local trước, không có => tìm LINK1
  - **RTRIM** mọi so sánh MACN, CMND, SOTK
  - Trả về `ChiNhanh` (servername) để biết dữ liệu từ đâu
- **Sử dụng:**
  - Role NGANHANG: Chọn chi nhánh từ dropdown => gọi SP với maCN
  - Role CHINHANH: Dùng maCN của nhân viên => gọi SP

#### 3️⃣ SP_XemSKKHACHHANG (role CHINHANH)
- **Mục đích:** Sao kê giao dịch cho role CHINHANH
- **Sửa chữa:** Thêm **RTRIM** vào mọi WHERE clause
  ```sql
  WHERE RTRIM(SOTK) = RTRIM(@SOTK)
  WHERE RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK)
  ```

#### 4️⃣ SP_XemSKKHACHHANG_TatCaChiNhanh (role NGANHANG)
- **Mục đích:** Sao kê giao dịch cross-site cho role NGANHANG
- **Logic:**
  - Kiểm tra TK ở local => lấy data local
  - Nếu không => kiểm tra LINK1 => lấy data LINK1
  - **RTRIM** ở tất cả comparisons

---

### 📁 File 2: `GRANT_Permission_For_NGANHANG_Role.sql`
**Chức năng:** Cấp quyền cho role NGANHANG  
**Các quyền đã cấp:**

#### EXECUTE Permissions:
✅ SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh  
✅ SP_LietKeTaiKhoanMoiMo_TheoChiNhanh  
✅ SP_XemSKKHACHHANG  
✅ SP_XemSKKHACHHANG_TatCaChiNhanh  
✅ SP_TimThongTinKhachHangTheoSTK (CHINHANH)  

#### SELECT Permissions:
✅ ChiNhanh  
✅ TaiKhoan  
✅ KhachHang  
✅ GD_GOIRUT  
✅ GD_CHUYENTIEN  
✅ NhanVien  

---

### 📁 File 3: Sửa code Java

#### `BaoCaoService.java`
**Thay đổi:**
- ❌ **Deprecated** method `lietKeTaiKhoanMoi()` 
  - Lý do: SP_LietKeTaiKhoanMoiMo KHÔNG TỒN TẠI
  - Throw `UnsupportedOperationException` nếu gọi

#### `StaffController.java`
**Thay đổi:**
```java
// CŨ - Logic phức tạp
if (maCNRequest != null && !maCNRequest.isEmpty()) {
    accounts = baoCaoService.lietKeTaiKhoanMoiTheoChiNhanh(...);
} else {
    accounts = baoCaoService.lietKeTaiKhoanMoi(...); // SP không tồn tại!
}

// MỚI - Đơn giản, rõ ràng
String maCN = (maCNRequest != null && !maCNRequest.isEmpty()) 
    ? maCNRequest          // NGANHANG chọn từ dropdown
    : nhanVien.getMaCN();  // CHINHANH dùng mã chi nhánh của mình

List<Map<String, Object>> accounts = baoCaoService.lietKeTaiKhoanMoiTheoChiNhanh(
    tuNgay, denNgay, maCN, tenServer, username, password);
```

**Giải thích logic:**
- **Role NGANHANG:** 
  - HTML bắt buộc chọn chi nhánh (line 3135)
  - `maCNRequest` luôn có giá trị
  - Gọi SP với chi nhánh được chọn
  
- **Role CHINHANH:**
  - Dropdown chi nhánh ẩn (line 3260)
  - `maCNRequest` = null
  - Tự động dùng `nhanVien.getMaCN()`

---

## 🚀 Các bước triển khai

### Bước 1: Chạy SQL Scripts (QUAN TRỌNG: Chạy trên PUBLISHER)

```sql
-- 1. Tạo/sửa các SP
USE [NGANHANG]
GO
-- Chạy file: FIX_Complete_BaoCao_System.sql
-- Tạo 4 SP: 
--   SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh
--   SP_LietKeTaiKhoanMoiMo_TheoChiNhanh
--   SP_XemSKKHACHHANG
--   SP_XemSKKHACHHANG_TatCaChiNhanh

-- 2. Cấp quyền cho role NGANHANG
-- Chạy file: GRANT_Permission_For_NGANHANG_Role.sql
```

**⚠️ LƯU Ý REPLICATION:**
- Nếu gặp lỗi `Msg 21531` (Cannot ALTER on Subscriber)
- => **PHẢI chạy trên PUBLISHER server**
- => Replication sẽ tự động propagate sang Subscriber

### Bước 2: Build lại Java application

```bash
# Trong terminal PowerShell
cd "d:\3. PTIT\05. HK 4\06. CSDL phan tan\1. Do an\nganhang-pt"

# Clean và build lại
mvn clean install

# Hoặc nếu đang chạy app
# Ctrl+C để dừng
# Sau đó Run lại từ VS Code
```

### Bước 3: Test chức năng

#### Test Case 1: Role NGANHANG - Sao kê giao dịch
1. Đăng nhập với role NGANHANG
2. Vào tab "Thống kê & Báo cáo"
3. Chọn "Sao kê giao dịch"
4. Nhập STK: `785001251`
5. Chọn thời gian
6. **Kỳ vọng:** Hiển thị chi nhánh "Chi nhánh Tân Định"

#### Test Case 2: Role NGANHANG - Tài khoản mới mở
1. Đăng nhập với role NGANHANG
2. Vào tab "Thống kê & Báo cáo"
3. Chọn "Tài khoản mở mới"
4. **Dropdown chi nhánh hiển thị** (bắt buộc chọn)
5. Chọn chi nhánh: BENTHANH hoặc TANDINH
6. Chọn khoảng thời gian
7. **Kỳ vọng:** Hiển thị danh sách TK của chi nhánh đã chọn

#### Test Case 3: Role CHINHANH - Tài khoản mới mở
1. Đăng nhập với role CHINHANH (ví dụ: nhân viên BENTHANH)
2. Vào tab "Thống kê & Báo cáo"
3. Chọn "Tài khoản mở mới"
4. **Dropdown chi nhánh ẨN** (tự động dùng BENTHANH)
5. Chọn khoảng thời gian
6. **Kỳ vọng:** Chỉ hiển thị TK của BENTHANH

---

## 🔍 Kiểm tra lỗi phổ biến

### Lỗi 1: "Không tìm thấy stored procedure"
**Nguyên nhân:** SP chưa được tạo hoặc chạy trên sai database  
**Giải pháp:**
```sql
-- Kiểm tra SP có tồn tại không
USE NGANHANG
SELECT name FROM sys.procedures WHERE name LIKE '%TaiKhoan%'
-- Kỳ vọng thấy:
--   SP_LietKeTaiKhoanMoiMo_TheoChiNhanh
--   SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh
```

### Lỗi 2: JOIN trả về 0 rows
**Nguyên nhân:** Thiếu RTRIM trong comparisons  
**Debug:**
```sql
-- Test RTRIM
DECLARE @STK NCHAR(9) = '785001251'
SELECT 
    LEN(@STK) AS [Length],           -- Kết quả: 9
    LEN(RTRIM(@STK)) AS [Trimmed],   -- Kết quả: 9
    DATALENGTH(@STK) AS [Bytes]      -- Kết quả: 18 (NCHAR = 2 bytes/char)

-- So sánh
SELECT CASE 
    WHEN SOTK = @STK THEN 'MATCH' 
    ELSE 'NO MATCH' 
END FROM TaiKhoan WHERE SOTK = '785001251'
-- Kết quả: NO MATCH (do trailing spaces)

SELECT CASE 
    WHEN RTRIM(SOTK) = RTRIM(@STK) THEN 'MATCH' 
    ELSE 'NO MATCH' 
END FROM TaiKhoan WHERE RTRIM(SOTK) = RTRIM(@STK)
-- Kết quả: MATCH ✅
```

### Lỗi 3: Role NGANHANG không thấy dropdown
**Nguyên nhân:** JavaScript không load  
**Kiểm tra:**
1. Mở Browser DevTools (F12)
2. Tab Console
3. Tìm error: `loadChiNhanhForTKMoi is not defined`
4. Kiểm tra `currentNhanVien.role` có đúng là 'NGANHANG' không

---

## 📊 So sánh Before/After

| Tính năng | ❌ TRƯỚC | ✅ SAU |
|-----------|---------|--------|
| SP_LietKeTaiKhoanMoiMo | Không tồn tại, code lỗi | Không cần, dùng _TheoChiNhanh |
| SP_LietKeTaiKhoanMoiMo_TheoChiNhanh | Không có RTRIM | Đầy đủ RTRIM |
| SP_XemSKKHACHHANG | Không có RTRIM | Đầy đủ RTRIM |
| SP_XemSKKHACHHANG_TatCaChiNhanh | Chưa hoàn thiện | Cross-site support |
| Controller logic | If-else phức tạp | 1 dòng ternary operator |
| Service method | 2 methods (1 broken) | 1 method working |
| GRANT permissions | Thiếu | Đầy đủ 5 SP + 6 tables |

---

## 📝 Technical Notes

### RTRIM Requirement
**Vì sao cần RTRIM?**
- SQL Server NCHAR(n) **tự động pad spaces** đến n characters
- `NCHAR(9)` lưu '785001251' => '785001251' (có trailing space)
- So sánh `'785001251' = '785001251 '` => FALSE ❌
- So sánh `RTRIM('785001251 ') = RTRIM('785001251')` => TRUE ✅

**Quy tắc:**
- WHERE clause: `WHERE RTRIM(col) = RTRIM(@param)`
- JOIN: `ON RTRIM(t1.col) = RTRIM(t2.col)`
- Output: `SELECT RTRIM(col) AS col`

### Cross-Site Architecture

```
┌────────────────┐         LINK1          ┌────────────────┐
│   BENTHANH     │◄──────────────────────►│    TANDINH     │
│   (Site 1)     │                         │    (Site 2)    │
└────────┬───────┘                         └────────┬───────┘
         │                                          │
         │                                          │
         │              LINK2 (Publisher)           │
         └──────────────────┬──────────────────────┘
                            │
                     ┌──────▼──────┐
                     │   NGANHANG  │
                     │  (Central)  │
                     └─────────────┘

Data Distribution:
- TaiKhoan: REPLICATED (cả 2 site có)
- ChiNhanh: PARTITIONED (mỗi site chỉ có chi nhánh của mình)
- KhachHang: REPLICATED
```

**Ví dụ:**
- STK `785001251` có `MACN=TANDINH`
- Tồn tại ở **cả 2 site** (replicated)
- Nhưng `ChiNhanh` với `MACN=TANDINH` **chỉ tồn tại ở TANDINH site**
- => JOIN ở BENTHANH **thất bại** nếu không fallback LINK1

---

## ✅ Checklist triển khai

### Trước khi deploy:
- [ ] Backup database NGANHANG
- [ ] Xác định server nào là PUBLISHER
- [ ] Đăng nhập SSMS với quyền DDL (ALTER PROCEDURE)

### Deploy SQL:
- [ ] Chạy `FIX_Complete_BaoCao_System.sql` trên PUBLISHER
- [ ] Kiểm tra 4 SP đã được tạo (`SELECT * FROM sys.procedures`)
- [ ] Chạy `GRANT_Permission_For_NGANHANG_Role.sql`
- [ ] Verify permissions (`SELECT * FROM sys.database_permissions WHERE grantee_principal_id = DATABASE_PRINCIPAL_ID('NGANHANG')`)

### Deploy Java:
- [ ] Code Java đã được sửa (BaoCaoService.java, StaffController.java)
- [ ] `mvn clean install` thành công
- [ ] Application khởi động không lỗi

### Testing:
- [ ] Test role NGANHANG: Sao kê giao dịch
- [ ] Test role NGANHANG: Tài khoản mới (với dropdown)
- [ ] Test role CHINHANH: Tài khoản mới (không dropdown)
- [ ] Test cross-site: STK ở site này, CHINHANH ở site kia

---

## 🆘 Troubleshooting

### Connection timeout 10.241.78.94:1443
**Đã thảo luận trước đó, các bước kiểm tra:**
1. Verify port: 1443 (non-standard) vs 1433 (default)
2. Test network: `Test-NetConnection -ComputerName 10.241.78.94 -Port 1443`
3. Check SQL Server TCP/IP enabled
4. Firewall rules cho port 1443

### Replication Error Msg 21531
**Giải pháp:** Luôn chạy ALTER PROCEDURE trên **PUBLISHER**  
**Xác định Publisher:**
```sql
-- Chạy trên server hiện tại
SELECT 
    DB_NAME() AS DatabaseName,
    CASE 
        WHEN category & 1 = 1 THEN 'Published'
        WHEN category & 2 = 2 THEN 'Subscribed'
        ELSE 'Normal'
    END AS ReplicationRole
FROM sys.databases
WHERE name = 'NGANHANG'
```

---

## 📞 Liên hệ & Hỗ trợ

**Các file quan trọng:**
1. `FIX_Complete_BaoCao_System.sql` - Tạo SP
2. `GRANT_Permission_For_NGANHANG_Role.sql` - Grant permissions
3. `BaoCaoService.java` - Service layer
4. `StaffController.java` - Controller logic
5. `staff-dashboard.html` - Frontend (lines 3226-3260)

**Ghi chú:** Tài liệu này tạo ngày 2026-02-07, phiên bản 1.0
