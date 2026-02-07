USE [NGANHANG]
GO

-- =============================================
-- SP: Tìm thông tin khách hàng theo STK - CHO NHÂN VIÊN NGÂN HÀNG
-- VERSION: FINAL FIX - Thêm RTRIM vào TẤT CẢ chỗ so sánh
-- =============================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
    @SOTK NCHAR(9)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        -- 🔹 BƯỚC 1: Kiểm tra STK ở site hiện tại (PHẢI CÓ RTRIM!)
        IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@SOTK))
        BEGIN
            -- ✅ STK tồn tại ở site hiện tại
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK,
                'SITE1' AS SITE
            FROM dbo.TAIKHOAN TK
            INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);
        END
        -- 🔹 BƯỚC 2: Kiểm tra STK ở LINK1 (PHẢI CÓ RTRIM!)
        ELSE IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] WHERE RTRIM(SOTK) = RTRIM(@SOTK))
        BEGIN
            -- ✅ STK tồn tại ở LINK1
            SELECT 
                KH.HO + ' ' + KH.TEN AS HOTEN,
                KH.CMND,
                CN.TENCN,
                TK.NGAYMOTK,
                'LINK1' AS SITE
            FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
            INNER JOIN [LINK1].[NGANHANG].[dbo].[KHACHHANG] KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            INNER JOIN [LINK1].[NGANHANG].[dbo].[CHINHANH] CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
            WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);
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

PRINT N'✅ Đã UPDATE SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh'
PRINT N'   → Đã thêm RTRIM vào TẤT CẢ điều kiện (EXISTS và JOIN)'
GO

-- ========== TEST NGAY ==========
PRINT N''
PRINT N'=========================================='
PRINT N'TEST: Tìm STK 785001251'
PRINT N'=========================================='

EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = '785001251'

PRINT N''
PRINT N'=========================================='
PRINT N'Nếu thấy kết quả bên trên → ✅ THÀNH CÔNG!'
PRINT N'Nếu vẫn trả về 0 rows → Kiểm tra lại STK có đúng không'
PRINT N'=========================================='
GO
