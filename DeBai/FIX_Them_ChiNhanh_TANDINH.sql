USE [NGANHANG]
GO

-- =============================================
-- FIX: Thêm chi nhánh TANDINH vào bảng CHINHANH
-- =============================================

PRINT N'=========================================='
PRINT N'🔧 SỬA LỖI: Thêm chi nhánh TANDINH'
PRINT N'=========================================='
PRINT N''

-- Kiểm tra xem TANDINH đã tồn tại chưa
IF NOT EXISTS (SELECT 1 FROM dbo.CHINHANH WHERE RTRIM(MACN) = 'TANDINH')
BEGIN
    PRINT N'Đang thêm chi nhánh TANDINH...'
    
    INSERT INTO dbo.CHINHANH (MACN, TENCN, DIACHI, SoDT)
    VALUES (
        N'TANDINH   ',  -- NCHAR(10) - thêm spaces để đủ 10 ký tự
        N'Chi nhánh Tân Định',
        N'234 Hai Bà Trưng, phường Đakao, Quận 1, TPHCM',
        N'...'
    )
    
    PRINT N'✅ Đã thêm chi nhánh TANDINH thành công!'
END
ELSE
BEGIN
    PRINT N'⚠️ Chi nhánh TANDINH đã tồn tại'
END
GO

PRINT N''
PRINT N'--- Kiểm tra lại bảng CHINHANH ---'
SELECT 
    MACN,
    '[' + MACN + ']' AS [MACN với brackets],
    TENCN,
    DIACHI
FROM dbo.CHINHANH
ORDER BY MACN
GO

PRINT N''
PRINT N'=========================================='
PRINT N'TEST LẠI SAU KHI FIX'
PRINT N'=========================================='

-- Test lại JOIN
PRINT N'--- Test JOIN sau khi thêm TANDINH ---'
SELECT 
    TK.SOTK,
    TK.MACN AS [MACN từ TK],
    CN.MACN AS [MACN từ CN],
    KH.HO + ' ' + KH.TEN AS HOTEN,
    CN.TENCN
FROM dbo.TAIKHOAN TK
INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
WHERE RTRIM(TK.SOTK) = '785001251'
GO

PRINT N''
PRINT N'--- Test SP sau khi fix ---'
EXEC SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh @SOTK = '785001251'
GO

PRINT N''
PRINT N'=========================================='
PRINT N'✅ HOÀN THÀNH!'
PRINT N'Nếu thấy kết quả bên trên → Vấn đề đã được giải quyết'
PRINT N'=========================================='
GO
