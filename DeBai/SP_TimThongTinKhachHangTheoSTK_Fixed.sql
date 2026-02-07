USE [NGANHANG]
GO

-- =============================================
-- SP: Lấy thông tin khách hàng theo số tài khoản
-- LƯU Ý: Lấy chi nhánh từ TAIKHOAN.MACN (nơi mở tài khoản)
--         KHÔNG PHẢI từ KHACHHANG.MACN (nơi đăng ký khách hàng)
-- =============================================

-- ========== CHO NHÂN VIÊN CHI NHÁNH ==========
-- SP này chỉ tìm kiếm trong site hiện tại
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_TimThongTinKhachHangTheoSTK]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK]
GO

CREATE PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK]
    @SOTK NCHAR(9)
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Lấy thông tin từ TAIKHOAN JOIN KHACHHANG JOIN CHINHANH
    -- CHÚ Ý: Lấy TENCN từ TAIKHOAN.MACN (chi nhánh mở tài khoản)
    SELECT 
        KH.HO + ' ' + KH.TEN AS HOTEN,
        CN.TENCN,                    -- Chi nhánh của TÀI KHOẢN, không phải của KHÁCH HÀNG
        KH.CMND,
        TK.NGAYMOTK,                 -- Ngày mở tài khoản
        TK.SODU
    FROM dbo.TaiKhoan TK
    INNER JOIN dbo.KhachHang KH ON TK.CMND = KH.CMND
    INNER JOIN dbo.ChiNhanh CN ON TK.MACN = CN.MACN  -- JOIN bằng TAIKHOAN.MACN
    WHERE TK.SOTK = @SOTK
END
GO

-- ========== CHO NHÂN VIÊN NGÂN HÀNG ==========
-- SP này tìm kiếm ở cả site hiện tại và LINK1
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]') AND type in (N'P', N'PC'))
    DROP PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
GO

CREATE PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
    @SOTK NCHAR(9)
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Kiểm tra tài khoản ở site hiện tại
    IF EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE SOTK = @SOTK)
    BEGIN
        -- Tài khoản ở site hiện tại
        SELECT 
            KH.HO + ' ' + KH.TEN AS HOTEN,
            CN.TENCN,                    -- Chi nhánh của TÀI KHOẢN
            KH.CMND,
            TK.NGAYMOTK,
            TK.SODU
        FROM dbo.TaiKhoan TK
        INNER JOIN dbo.KhachHang KH ON TK.CMND = KH.CMND
        INNER JOIN dbo.ChiNhanh CN ON TK.MACN = CN.MACN  -- JOIN bằng TAIKHOAN.MACN
        WHERE TK.SOTK = @SOTK
    END
    ELSE IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TaiKhoan] WHERE SOTK = @SOTK)
    BEGIN
        -- Tài khoản ở LINK1
        SELECT 
            KH.HO + ' ' + KH.TEN AS HOTEN,
            CN.TENCN,                    -- Chi nhánh của TÀI KHOẢN
            KH.CMND,
            TK.NGAYMOTK,
            TK.SODU
        FROM [LINK1].[NGANHANG].[dbo].[TaiKhoan] TK
        INNER JOIN [LINK1].[NGANHANG].[dbo].[KhachHang] KH ON TK.CMND = KH.CMND
        INNER JOIN [LINK1].[NGANHANG].[dbo].[ChiNhanh] CN ON TK.MACN = CN.MACN  -- JOIN bằng TAIKHOAN.MACN
        WHERE TK.SOTK = @SOTK
    END
    ELSE
    BEGIN
        -- Không tìm thấy tài khoản
        RAISERROR(N'Tài khoản không tồn tại', 16, 1)
    END
END
GO

-- ========== TEST CASES ==========

-- Test 1: Kiểm tra SP cho nhân viên chi nhánh
PRINT N'=== Test SP_TimThongTinKhachHangTheoSTK ==='
EXEC SP_TimThongTinKhachHangTheoSTK @SOTK = '785001251'

-- Test 2: Kiểm tra SP cho nhân viên ngân hàng
PRINT N'=== Test SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh ==='
EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = '785001251'

-- Test 3: Kiểm tra trường hợp tài khoản không tồn tại
PRINT N'=== Test tài khoản không tồn tại ==='
EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = '999999999'

PRINT N'=== Hoàn thành ==='
GO
