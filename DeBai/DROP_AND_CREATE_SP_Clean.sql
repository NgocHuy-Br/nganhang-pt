USE [NGANHANG]
GO

-- =============================================
-- BƯỚC 1: DROP SP cũ hoàn toàn
-- =============================================
PRINT N'=========================================='
PRINT N'Bước 1: Drop SP cũ (nếu có)'
PRINT N'=========================================='

IF EXISTS (SELECT * FROM sys.objects 
           WHERE object_id = OBJECT_ID(N'[dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]') 
           AND type in (N'P', N'PC'))
BEGIN
    DROP PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
    PRINT N'✅ Đã DROP SP cũ'
END
ELSE
BEGIN
    PRINT N'⚠️ SP chưa tồn tại (sẽ tạo mới)'
END
GO

-- =============================================
-- BƯỚC 2: CREATE SP mới hoàn toàn
-- =============================================
PRINT N''
PRINT N'=========================================='
PRINT N'Bước 2: Tạo SP mới'
PRINT N'=========================================='

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
    @SOTK NCHAR(9)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        -- Kiểm tra STK ở site hiện tại
        IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@SOTK))
        BEGIN
            -- STK tồn tại ở site hiện tại
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK
            FROM dbo.TAIKHOAN TK
            INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);
            
            RETURN 0; -- Success
        END
        -- Kiểm tra STK ở LINK1
        ELSE IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] WHERE RTRIM(SOTK) = RTRIM(@SOTK))
        BEGIN
            -- STK tồn tại ở LINK1
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK
            FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
            INNER JOIN [LINK1].[NGANHANG].[dbo].[KHACHHANG] KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            INNER JOIN [LINK1].[NGANHANG].[dbo].[CHINHANH] CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);
            
            RETURN 0; -- Success
        END
        ELSE
        BEGIN
            -- STK không tồn tại
            RAISERROR(N'Tài khoản không tồn tại trong hệ thống!', 16, 1);
            RETURN -1;
        END
    END TRY
    BEGIN CATCH
        DECLARE 
            @ErrorMsg NVARCHAR(4000),
            @ErrorSeverity INT,
            @ErrorState INT;

        SELECT 
            @ErrorMsg = N'Lỗi: ' + ERROR_MESSAGE(),
            @ErrorSeverity = ERROR_SEVERITY(),
            @ErrorState = ERROR_STATE();

        RAISERROR(@ErrorMsg, @ErrorSeverity, @ErrorState);
        RETURN -1;
    END CATCH;
END
GO

PRINT N'✅ Đã tạo SP mới thành công'
GO

-- =============================================
-- BƯỚC 3: Xóa cache và recompile
-- =============================================
PRINT N''
PRINT N'=========================================='
PRINT N'Bước 3: Xóa cache & recompile'
PRINT N'=========================================='

-- Xóa plan cache cho SP này
DBCC FREEPROCCACHE
GO

-- Recompile SP
EXEC sp_recompile 'SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh'
GO

PRINT N'✅ Đã xóa cache & recompile'
GO

-- =============================================
-- BƯỚC 4: TEST NGAY
-- =============================================
PRINT N''
PRINT N'=========================================='
PRINT N'Bước 4: TEST với STK = 785001251'
PRINT N'=========================================='
PRINT N''

-- Test 1: STK cụ thể
PRINT N'--- Test 1: STK 785001251 ---'
EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = '785001251'
PRINT N''

-- Test 2: Kiểm tra xem server đang ở đâu
PRINT N'--- Kiểm tra server hiện tại ---'
SELECT 
    @@SERVERNAME AS ServerName,
    DB_NAME() AS DatabaseName,
    GETDATE() AS CurrentTime
PRINT N''

-- Test 3: Liệt kê các STK có sẵn để test
PRINT N'--- Danh sách 5 STK ở site hiện tại ---'
SELECT TOP 5
    TK.SOTK,
    TK.CMND,
    KH.HO + ' ' + KH.TEN AS HOTEN,
    CN.TENCN AS [Chi nhánh của TK],
    TK.MACN AS [MACN của TK]
FROM dbo.TAIKHOAN TK
INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
ORDER BY TK.NGAYMOTK DESC
PRINT N''

PRINT N'=========================================='
PRINT N'HOÀN THÀNH!'
PRINT N'Nếu vẫn thấy 0 rows → Hãy kiểm tra:'
PRINT N'1. Bạn đang chạy trên server nào (BENTHANH hay TANDINH)?'
PRINT N'2. STK 785001251 có tồn tại ở server đó không?'
PRINT N'3. LINK1 đã được cấu hình chưa?'
PRINT N'=========================================='
GO
