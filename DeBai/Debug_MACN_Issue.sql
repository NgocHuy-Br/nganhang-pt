USE [NGANHANG]
GO

-- =============================================
-- KIỂM TRA VẤN ĐỀ MACN KHÔNG MATCH
-- =============================================

DECLARE @TestSTK NCHAR(9) = '785001251';

PRINT N'=========================================='
PRINT N'🔍 KIỂM TRA VẤN ĐỀ MACN'
PRINT N'=========================================='
PRINT N''

-- 1. Lấy MACN từ TAIKHOAN
PRINT N'--- 1. MACN từ TAIKHOAN ---'
SELECT 
    SOTK,
    MACN AS [MACN gốc],
    '[' + MACN + ']' AS [MACN với brackets],
    RTRIM(MACN) AS [MACN sau RTRIM],
    '[' + RTRIM(MACN) + ']' AS [MACN RTRIM với brackets],
    LEN(MACN) AS [Độ dài],
    LEN(RTRIM(MACN)) AS [Độ dài sau RTRIM],
    DATALENGTH(MACN) AS [Bytes],
    CONVERT(VARBINARY(20), MACN) AS [MACN_HEX]
FROM dbo.TAIKHOAN
WHERE RTRIM(SOTK) = RTRIM(@TestSTK)
PRINT N''

-- 2. Liệt kê TẤT CẢ MACN trong CHINHANH
PRINT N'--- 2. TẤT CẢ MACN trong bảng CHINHANH ---'
SELECT 
    MACN AS [MACN gốc],
    '[' + MACN + ']' AS [MACN với brackets],
    RTRIM(MACN) AS [MACN sau RTRIM],
    '[' + RTRIM(MACN) + ']' AS [MACN RTRIM với brackets],
    TENCN,
    LEN(MACN) AS [Độ dài],
    LEN(RTRIM(MACN)) AS [Độ dài sau RTRIM],
    DATALENGTH(MACN) AS [Bytes],
    CONVERT(VARBINARY(20), MACN) AS [MACN_HEX]
FROM dbo.CHINHANH
ORDER BY MACN
PRINT N''

-- 3. So sánh HEX giữa TAIKHOAN.MACN và các MACN trong CHINHANH
PRINT N'--- 3. SO SÁNH HEX ---'
DECLARE @TK_MACN NCHAR(10)
SELECT @TK_MACN = MACN FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@TestSTK)

PRINT N'MACN từ TAIKHOAN:'
PRINT N'  Giá trị: [' + @TK_MACN + ']'
PRINT N'  RTRIM: [' + RTRIM(@TK_MACN) + ']'
PRINT N'  HEX: ' + CONVERT(NVARCHAR(100), CONVERT(VARBINARY(20), @TK_MACN), 1)
PRINT N''

SELECT 
    MACN,
    TENCN,
    CASE 
        WHEN MACN = @TK_MACN THEN '✅ MATCH (trực tiếp)'
        WHEN RTRIM(MACN) = RTRIM(@TK_MACN) THEN '✅ MATCH (RTRIM)'
        ELSE '❌ NOT MATCH'
    END AS [Kết quả],
    CONVERT(VARBINARY(20), MACN) AS [MACN_HEX],
    CASE 
        WHEN CONVERT(VARBINARY(20), MACN) = CONVERT(VARBINARY(20), @TK_MACN) THEN 'HEX MATCH'
        ELSE 'HEX NOT MATCH'
    END AS [So sánh HEX]
FROM dbo.CHINHANH
PRINT N''

-- 4. Test JOIN trực tiếp
PRINT N'--- 4. TEST JOIN TRỰC TIẾP ---'
SELECT 
    TK.SOTK,
    TK.MACN AS [TK.MACN],
    CN.MACN AS [CN.MACN],
    CN.TENCN
FROM dbo.TAIKHOAN TK
LEFT JOIN dbo.CHINHANH CN ON TK.MACN = CN.MACN
WHERE RTRIM(TK.SOTK) = RTRIM(@TestSTK)

PRINT N''
PRINT N'--- 5. TEST JOIN VỚI RTRIM ---'
SELECT 
    TK.SOTK,
    RTRIM(TK.MACN) AS [TK.MACN_RTRIM],
    RTRIM(CN.MACN) AS [CN.MACN_RTRIM],
    CN.TENCN
FROM dbo.TAIKHOAN TK
LEFT JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
WHERE RTRIM(TK.SOTK) = RTRIM(@TestSTK)

PRINT N''

-- 6. Kiểm tra collation
PRINT N'--- 6. KIỂM TRA COLLATION ---'
SELECT 
    'TAIKHOAN.MACN' AS [Bảng.Cột],
    DATA_TYPE AS [Kiểu dữ liệu],
    CHARACTER_MAXIMUM_LENGTH AS [Độ dài],
    COLLATION_NAME AS [Collation]
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'TAIKHOAN' AND COLUMN_NAME = 'MACN'

UNION ALL

SELECT 
    'CHINHANH.MACN',
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    COLLATION_NAME
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'CHINHANH' AND COLUMN_NAME = 'MACN'

PRINT N''

-- 7. Thử UPDATE MACN nếu bị lỗi
PRINT N'--- 7. KIẾN NGHỊ SỬA LỖI ---'
PRINT N'Nếu MACN trong TAIKHOAN không match với CHINHANH:'
PRINT N'Có thể MACN bị sai hoặc không tồn tại trong bảng CHINHANH'
PRINT N''
PRINT N'Giải pháp:'
PRINT N'1. Kiểm tra MACN trong TAIKHOAN có đúng không'
PRINT N'2. Nếu sai, cần UPDATE lại MACN cho đúng'
PRINT N'3. Hoặc thêm MACN mới vào bảng CHINHANH'
PRINT N''

-- Kiểm tra MACN nào không tồn tại
PRINT N'--- MACN trong TAIKHOAN không tồn tại trong CHINHANH ---'
SELECT DISTINCT
    TK.MACN AS [MACN lỗi],
    '[' + TK.MACN + ']' AS [MACN với brackets],
    COUNT(*) AS [Số lượng TK bị lỗi]
FROM dbo.TAIKHOAN TK
LEFT JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
WHERE CN.MACN IS NULL
GROUP BY TK.MACN

PRINT N''
PRINT N'=========================================='
PRINT N'KẾT LUẬN'
PRINT N'=========================================='
GO
