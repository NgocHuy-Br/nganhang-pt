USE [NGANHANG]
GO

-- =============================================
-- SP: Tìm thông tin khách hàng theo STK (CHO NHÂN VIÊN NGÂN HÀNG)
-- FIX: Thêm RTRIM cho tất cả điều kiện JOIN để tránh lỗi trailing spaces
-- =============================================

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
GO

CREATE PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
    @SOTK NCHAR(9)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        -- 🔹 BƯỚC 1: Kiểm tra STK ở site hiện tại
        IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@SOTK))
        BEGIN
            -- ✅ STK tồn tại ở site hiện tại
            -- 🔑 FIX: Thêm RTRIM cho TẤT CẢ các điều kiện JOIN
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK,
                'SITE1' AS SITE
            FROM dbo.TAIKHOAN TK
            INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)  -- FIX: RTRIM
            INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)   -- FIX: RTRIM
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);  -- FIX: RTRIM
        END
        ELSE IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] WHERE RTRIM(SOTK) = RTRIM(@SOTK))
        BEGIN
            -- ✅ STK tồn tại ở LINK1
            -- 🔑 FIX: Thêm RTRIM cho TẤT CẢ các điều kiện JOIN
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK,
                'LINK1' AS SITE
            FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
            INNER JOIN [LINK1].[NGANHANG].[dbo].[KHACHHANG] KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)  -- FIX: RTRIM
            INNER JOIN [LINK1].[NGANHANG].[dbo].[CHINHANH] CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)   -- FIX: RTRIM
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);  -- FIX: RTRIM
        END
        ELSE
        BEGIN
            -- ❌ STK không tồn tại ở bất kỳ site nào
            DECLARE @ErrMsg NVARCHAR(4000) = 
                N'Tài khoản "' + RTRIM(@SOTK) + N'" không tồn tại trong hệ thống!';
            RAISERROR(@ErrMsg, 16, 1);
            RETURN -1;
        END
    END TRY
    BEGIN CATCH
        DECLARE 
            @ErrorMsg NVARCHAR(4000),
            @ErrorSeverity INT,
            @ErrorState INT;

        SELECT 
            @ErrorMsg = N'Lỗi khi tìm thông tin khách hàng theo STK: ' + ERROR_MESSAGE(),
            @ErrorSeverity = ERROR_SEVERITY(),
            @ErrorState = ERROR_STATE();

        RAISERROR(@ErrorMsg, @ErrorSeverity, @ErrorState);
        RETURN -1;
    END CATCH;
END
GO

-- =============================================
-- SP: Tìm thông tin khách hàng theo STK (CHO NHÂN VIÊN CHI NHÁNH)
-- FIX: Thêm RTRIM cho tất cả điều kiện JOIN
-- =============================================

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_TimThongTinKhachHangTheoSTK]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK]
GO

CREATE PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK]
    @SOTK NCHAR(9)
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Tìm kiếm chỉ trong site hiện tại
    -- 🔑 FIX: Thêm RTRIM cho TẤT CẢ các điều kiện JOIN
    SELECT 
        KH.HO + ' ' + KH.TEN AS HOTEN,
        CN.TENCN,
        KH.CMND,
        TK.NGAYMOTK,
        TK.SODU
    FROM dbo.TAIKHOAN TK
    INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)  -- FIX: RTRIM
    INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)   -- FIX: RTRIM
    WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);  -- FIX: RTRIM
END
GO

PRINT N'=========================================='
PRINT N'✅ Đã tạo/cập nhật 2 Stored Procedures với FIX RTRIM'
PRINT N'=========================================='
PRINT ''

-- ========== TEST CASES ==========

PRINT N'--- Test 1: SP cho nhân viên chi nhánh ---'
EXEC SP_TimThongTinKhachHangTheoSTK @SOTK = '785001251'
PRINT ''

PRINT N'--- Test 2: SP cho nhân viên ngân hàng (site hiện tại) ---'
EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = '785001251'
PRINT ''

PRINT N'--- Test 3: SP cho nhân viên ngân hàng (LINK1 - nếu STK tồn tại) ---'
-- Tìm 1 STK ở LINK1 để test
DECLARE @TestLinkSTK NCHAR(9)
SELECT TOP 1 @TestLinkSTK = SOTK 
FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN]
WHERE RTRIM(MACN) != 'BENTHANH' -- Lấy STK không phải ở BENTHANH

IF @TestLinkSTK IS NOT NULL
BEGIN
    PRINT N'Test với STK từ LINK1: ' + RTRIM(@TestLinkSTK)
    EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = @TestLinkSTK
END
ELSE
BEGIN
    PRINT N'Không tìm thấy STK ở LINK1 để test'
END
PRINT ''

PRINT N'--- Test 4: Tài khoản không tồn tại ---'
BEGIN TRY
    EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = '999999999'
END TRY
BEGIN CATCH
    PRINT N'❌ Lỗi (expected): ' + ERROR_MESSAGE()
END CATCH

PRINT N''
PRINT N'=========================================='
PRINT N'✅ HOÀN THÀNH - Hãy kiểm tra kết quả bên trên'
PRINT N'=========================================='
GO
