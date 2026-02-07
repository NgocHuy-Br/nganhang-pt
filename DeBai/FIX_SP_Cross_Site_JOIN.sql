USE [NGANHANG]
GO

-- =============================================
-- FIX: SP hỗ trợ JOIN cross-site
-- Cho phép STK ở site A JOIN với CHINHANH ở site B
-- =============================================

IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
GO

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
        -- Biến lưu kết quả tạm
        DECLARE @Found BIT = 0;
        
        -- ============================================================
        -- BƯỚC 1: Tìm ở site hiện tại
        -- ============================================================
        IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@SOTK))
        BEGIN
            -- Thử JOIN với CHINHANH site hiện tại trước
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK
            FROM dbo.TAIKHOAN TK
            INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);
            
            -- Kiểm tra có trả về kết quả không
            IF @@ROWCOUNT > 0
            BEGIN
                SET @Found = 1;
                RETURN 0;
            END
            
            -- Nếu không có kết quả → MACN không tồn tại ở site này
            -- Thử JOIN cross-site với LINK1.CHINHANH
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK
            FROM dbo.TAIKHOAN TK
            INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            INNER JOIN [LINK1].[NGANHANG].[dbo].[CHINHANH] CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);
            
            IF @@ROWCOUNT > 0
            BEGIN
                SET @Found = 1;
                RETURN 0;
            END
        END
        
        -- ============================================================
        -- BƯỚC 2: Tìm ở LINK1
        -- ============================================================
        IF @Found = 0 AND EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] WHERE RTRIM(SOTK) = RTRIM(@SOTK))
        BEGIN
            -- Thử JOIN với CHINHANH LINK1 trước
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK
            FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
            INNER JOIN [LINK1].[NGANHANG].[dbo].[KHACHHANG] KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            INNER JOIN [LINK1].[NGANHANG].[dbo].[CHINHANH] CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);
            
            IF @@ROWCOUNT > 0
            BEGIN
                SET @Found = 1;
                RETURN 0;
            END
            
            -- Nếu không có kết quả → Thử JOIN cross-site với site hiện tại
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK
            FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
            INNER JOIN [LINK1].[NGANHANG].[dbo].[KHACHHANG] KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);
            
            IF @@ROWCOUNT > 0
            BEGIN
                SET @Found = 1;
                RETURN 0;
            END
        END
        
        -- ============================================================
        -- BƯỚC 3: Không tìm thấy hoặc không JOIN được
        -- ============================================================
        IF @Found = 0
        BEGIN
            RAISERROR(N'Tài khoản không tồn tại hoặc dữ liệu không hợp lệ!', 16, 1);
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

PRINT N'✅ Đã tạo SP với hỗ trợ JOIN cross-site'
GO

-- =============================================
-- TEST
-- =============================================
PRINT N''
PRINT N'=========================================='
PRINT N'TEST SP sau khi fix'
PRINT N'=========================================='

-- Test với STK có MACN ở site khác
EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = '785001251'

PRINT N''
PRINT N'=========================================='
PRINT N'Nếu thấy kết quả TENCN = "Chi nhánh Tân Định"'
PRINT N'→ ✅ SP đã hoạt động đúng với cross-site JOIN!'
PRINT N'=========================================='
GO
