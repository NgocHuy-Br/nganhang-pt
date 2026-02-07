USE [NGANHANG]
GO

-- =============================================
-- SCRIPT DEBUG: Kiểm tra vấn đề SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh
-- =============================================

-- Thay STK này bằng STK bạn đang test
DECLARE @TestSTK NCHAR(9) = '785001251'; -- Thay bằng STK của bạn

PRINT N'=========================================='
PRINT N'BẮT ĐẦU DEBUG - STK: ' + @TestSTK
PRINT N'=========================================='
PRINT ''

-- ========== BƯỚC 1: Kiểm tra tên server hiện tại ==========
PRINT N'--- BƯỚC 1: Server hiện tại ---'
SELECT @@SERVERNAME AS [Server Name], DB_NAME() AS [Database Name]
PRINT ''

-- ========== BƯỚC 2: Kiểm tra STK ở site hiện tại ==========
PRINT N'--- BƯỚC 2: Kiểm tra STK ở site hiện tại ---'
IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE SOTK = @TestSTK)
BEGIN
    PRINT N'✅ Tìm thấy STK ở site hiện tại'
    
    SELECT 
        TK.SOTK,
        TK.CMND,
        TK.MACN AS [MACN của TK],
        TK.SODU,
        TK.NGAYMOTK
    FROM dbo.TAIKHOAN TK
    WHERE TK.SOTK = @TestSTK
    
    -- Kiểm tra thông tin khách hàng
    SELECT 
        KH.CMND,
        KH.HO + ' ' + KH.TEN AS HOTEN,
        KH.MACN AS [MACN của KH]
    FROM dbo.TAIKHOAN TK
    JOIN dbo.KHACHHANG KH ON TK.CMND = KH.CMND
    WHERE TK.SOTK = @TestSTK
    
    -- Kiểm tra thông tin chi nhánh
    SELECT 
        CN.MACN,
        CN.TENCN
    FROM dbo.TAIKHOAN TK
    JOIN dbo.CHINHANH CN ON TK.MACN = CN.MACN
    WHERE TK.SOTK = @TestSTK
END
ELSE
BEGIN
    PRINT N'❌ KHÔNG tìm thấy STK ở site hiện tại'
END
PRINT ''

-- ========== BƯỚC 3: Kiểm tra LINK1 có hoạt động không ==========
PRINT N'--- BƯỚC 3: Kiểm tra LINK1 ---'
BEGIN TRY
    -- Kiểm tra LINK1 có tồn tại không
    IF EXISTS (SELECT 1 FROM sys.servers WHERE name = 'LINK1')
    BEGIN
        PRINT N'✅ LINK1 đã được cấu hình'
        
        -- Test kết nối đến LINK1
        DECLARE @LinkTest INT
        SELECT @LinkTest = COUNT(*) FROM [LINK1].[NGANHANG].[dbo].[CHINHANH]
        PRINT N'✅ Kết nối LINK1 thành công. Số chi nhánh ở LINK1: ' + CAST(@LinkTest AS NVARCHAR(10))
    END
    ELSE
    BEGIN
        PRINT N'❌ LINK1 CHƯA được cấu hình!'
        PRINT N'   → Cần tạo Linked Server tên "LINK1"'
    END
END TRY
BEGIN CATCH
    PRINT N'❌ LỖI khi kết nối LINK1: ' + ERROR_MESSAGE()
    PRINT N'   → Kiểm tra lại cấu hình Linked Server'
END CATCH
PRINT ''

-- ========== BƯỚC 4: Kiểm tra STK ở LINK1 ==========
PRINT N'--- BƯỚC 4: Kiểm tra STK ở LINK1 ---'
BEGIN TRY
    IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] WHERE SOTK = @TestSTK)
    BEGIN
        PRINT N'✅ Tìm thấy STK ở LINK1'
        
        SELECT 
            TK.SOTK,
            TK.CMND,
            TK.MACN AS [MACN của TK],
            TK.SODU,
            TK.NGAYMOTK
        FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
        WHERE TK.SOTK = @TestSTK
        
        -- Kiểm tra thông tin khách hàng ở LINK1
        SELECT 
            KH.CMND,
            KH.HO + ' ' + KH.TEN AS HOTEN,
            KH.MACN AS [MACN của KH]
        FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
        JOIN [LINK1].[NGANHANG].[dbo].[KHACHHANG] KH ON TK.CMND = KH.CMND
        WHERE TK.SOTK = @TestSTK
        
        -- Kiểm tra thông tin chi nhánh ở LINK1
        SELECT 
            CN.MACN,
            CN.TENCN
        FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
        JOIN [LINK1].[NGANHANG].[dbo].[CHINHANH] CN ON TK.MACN = CN.MACN
        WHERE TK.SOTK = @TestSTK
    END
    ELSE
    BEGIN
        PRINT N'❌ KHÔNG tìm thấy STK ở LINK1'
    END
END TRY
BEGIN CATCH
    PRINT N'❌ LỖI khi truy vấn LINK1: ' + ERROR_MESSAGE()
END CATCH
PRINT ''

-- ========== BƯỚC 5: Liệt kê TẤT CẢ các STK trong hệ thống ==========
PRINT N'--- BƯỚC 5: Danh sách 10 STK ở site hiện tại ---'
SELECT TOP 10 
    SOTK,
    CMND,
    MACN,
    SODU,
    NGAYMOTK
FROM dbo.TAIKHOAN
ORDER BY NGAYMOTK DESC
PRINT ''

PRINT N'--- BƯỚC 6: Danh sách 10 STK ở LINK1 (nếu có) ---'
BEGIN TRY
    SELECT TOP 10 
        SOTK,
        CMND,
        MACN,
        SODU,
        NGAYMOTK
    FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN]
    ORDER BY NGAYMOTK DESC
END TRY
BEGIN CATCH
    PRINT N'❌ Không thể lấy danh sách từ LINK1: ' + ERROR_MESSAGE()
END CATCH
PRINT ''

-- ========== BƯỚC 7: Test chạy SP với STK test ==========
PRINT N'--- BƯỚC 7: Chạy SP với STK = ' + @TestSTK + ' ---'
BEGIN TRY
    EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = @TestSTK
    PRINT N'✅ SP chạy thành công'
END TRY
BEGIN CATCH
    PRINT N'❌ SP báo lỗi: ' + ERROR_MESSAGE()
END CATCH

PRINT ''
PRINT N'=========================================='
PRINT N'KẾT THÚC DEBUG'
PRINT N'=========================================='
GO
