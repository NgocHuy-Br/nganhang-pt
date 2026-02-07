USE [NGANHANG]
GO

-- =============================================
-- SCRIPT DEBUG TOÀN DIỆN: Tìm gốc rễ vấn đề
-- =============================================

DECLARE @TestSTK NCHAR(9) = '785001251';

PRINT N'=========================================='
PRINT N'🔍 DEBUG TOÀN DIỆN - STK: ' + @TestSTK
PRINT N'=========================================='
PRINT N''

-- =============================================
-- 1️⃣ KIỂM TRA DỮ LIỆU THÔ
-- =============================================
PRINT N'--- 1️⃣ KIỂM TRA DỮ LIỆU THÔ TRONG BẢNG TAIKHOAN ---'
SELECT 
    SOTK,
    '[' + SOTK + ']' AS [SOTK với brackets],
    LEN(SOTK) AS [Độ dài],
    DATALENGTH(SOTK) AS [Bytes],
    CMND,
    '[' + CMND + ']' AS [CMND với brackets],
    MACN,
    '[' + MACN + ']' AS [MACN với brackets]
FROM dbo.TAIKHOAN
WHERE SOTK LIKE '785001251%'  -- Tìm tất cả STK bắt đầu bằng 785001251
PRINT N''

-- =============================================
-- 2️⃣ KIỂM TRA VỚI RTRIM
-- =============================================
PRINT N'--- 2️⃣ KIỂM TRA SAU KHI RTRIM ---'
SELECT 
    RTRIM(SOTK) AS [SOTK_RTRIM],
    '[' + RTRIM(SOTK) + ']' AS [SOTK_RTRIM với brackets],
    LEN(RTRIM(SOTK)) AS [Độ dài sau RTRIM],
    RTRIM(CMND) AS [CMND_RTRIM],
    RTRIM(MACN) AS [MACN_RTRIM]
FROM dbo.TAIKHOAN
WHERE SOTK LIKE '785001251%'
PRINT N''

-- =============================================
-- 3️⃣ KIỂM TRA ĐIỀU KIỆN EXISTS
-- =============================================
PRINT N'--- 3️⃣ TEST ĐIỀU KIỆN EXISTS ---'

-- Test 3.1: Không có RTRIM
IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE SOTK = @TestSTK)
    PRINT N'✅ EXISTS (không RTRIM): Tìm thấy'
ELSE
    PRINT N'❌ EXISTS (không RTRIM): KHÔNG tìm thấy'

-- Test 3.2: Có RTRIM
IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@TestSTK))
    PRINT N'✅ EXISTS (có RTRIM): Tìm thấy'
ELSE
    PRINT N'❌ EXISTS (có RTRIM): KHÔNG tìm thấy'

PRINT N''

-- =============================================
-- 4️⃣ KIỂM TRA JOIN VỚI KHACHHANG
-- =============================================
PRINT N'--- 4️⃣ KIỂM TRA JOIN VỚI KHACHHANG ---'

-- Test 4.1: JOIN không có RTRIM
SELECT 
    TK.SOTK,
    TK.CMND AS [TK.CMND],
    KH.CMND AS [KH.CMND],
    CASE WHEN TK.CMND = KH.CMND THEN 'MATCH' ELSE 'NOT MATCH' END AS [So sánh trực tiếp],
    CASE WHEN RTRIM(TK.CMND) = RTRIM(KH.CMND) THEN 'MATCH' ELSE 'NOT MATCH' END AS [So sánh RTRIM]
FROM dbo.TAIKHOAN TK
LEFT JOIN dbo.KHACHHANG KH ON TK.CMND = KH.CMND  -- Không RTRIM
WHERE TK.SOTK LIKE '785001251%'

-- Test 4.2: Kiểm tra có bao nhiêu CMND match
DECLARE @TK_CMND NCHAR(10)
SELECT @TK_CMND = CMND FROM dbo.TAIKHOAN WHERE SOTK LIKE '785001251%'

PRINT N'CMND từ TAIKHOAN: [' + ISNULL(@TK_CMND, 'NULL') + ']'

SELECT 
    COUNT(*) AS [Số KH match không RTRIM]
FROM dbo.KHACHHANG
WHERE CMND = @TK_CMND

SELECT 
    COUNT(*) AS [Số KH match có RTRIM]
FROM dbo.KHACHHANG
WHERE RTRIM(CMND) = RTRIM(@TK_CMND)

PRINT N''

-- =============================================
-- 5️⃣ KIỂM TRA JOIN VỚI CHINHANH
-- =============================================
PRINT N'--- 5️⃣ KIỂM TRA JOIN VỚI CHINHANH ---'

-- Lấy MACN từ TAIKHOAN
DECLARE @TK_MACN NCHAR(10)
SELECT @TK_MACN = MACN FROM dbo.TAIKHOAN WHERE SOTK LIKE '785001251%'

PRINT N'MACN từ TAIKHOAN: [' + ISNULL(@TK_MACN, 'NULL') + ']'
PRINT N'MACN sau RTRIM: [' + RTRIM(@TK_MACN) + ']'

-- Kiểm tra chi nhánh match
SELECT 
    MACN,
    '[' + MACN + ']' AS [MACN với brackets],
    TENCN,
    CASE WHEN MACN = @TK_MACN THEN 'MATCH' ELSE 'NOT MATCH' END AS [So sánh trực tiếp],
    CASE WHEN RTRIM(MACN) = RTRIM(@TK_MACN) THEN 'MATCH' ELSE 'NOT MATCH' END AS [So sánh RTRIM]
FROM dbo.CHINHANH

PRINT N''

-- =============================================
-- 6️⃣ TEST FULL JOIN (như trong SP)
-- =============================================
PRINT N'--- 6️⃣ TEST FULL JOIN (KHÔNG RTRIM) ---'
SELECT 
    KH.HO + ' ' + KH.TEN AS HOTEN,
    KH.CMND,
    CN.TENCN,
    TK.NGAYMOTK
FROM dbo.TAIKHOAN TK
JOIN dbo.KHACHHANG KH ON TK.CMND = KH.CMND  -- Không RTRIM
JOIN dbo.CHINHANH CN ON TK.MACN = CN.MACN   -- Không RTRIM
WHERE TK.SOTK = @TestSTK

PRINT N''
PRINT N'--- 6️⃣ TEST FULL JOIN (CÓ RTRIM) ---'
SELECT 
    KH.HO + ' ' + KH.TEN AS HOTEN,
    KH.CMND,
    CN.TENCN,
    TK.NGAYMOTK
FROM dbo.TAIKHOAN TK
JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)  -- Có RTRIM
JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)   -- Có RTRIM
WHERE RTRIM(TK.SOTK) = RTRIM(@TestSTK)

PRINT N''

-- =============================================
-- 7️⃣ SO SÁNH HEX/BINARY
-- =============================================
PRINT N'--- 7️⃣ SO SÁNH HEX/BINARY (tìm khoảng trắng ẩn) ---'
SELECT 
    SOTK,
    CONVERT(VARBINARY(20), SOTK) AS [SOTK_HEX],
    CMND,
    CONVERT(VARBINARY(20), CMND) AS [CMND_HEX],
    MACN,
    CONVERT(VARBINARY(20), MACN) AS [MACN_HEX]
FROM dbo.TAIKHOAN
WHERE SOTK LIKE '785001251%'

-- So sánh với parameter
SELECT 
    @TestSTK AS [Parameter],
    CONVERT(VARBINARY(20), @TestSTK) AS [Parameter_HEX],
    LEN(@TestSTK) AS [Độ dài],
    DATALENGTH(@TestSTK) AS [Bytes]

PRINT N''

-- =============================================
-- 8️⃣ KIỂM TRA LINK1
-- =============================================
PRINT N'--- 8️⃣ KIỂM TRA LINK1 ---'
BEGIN TRY
    IF EXISTS (SELECT 1 FROM sys.servers WHERE name = 'LINK1')
    BEGIN
        PRINT N'✅ LINK1 đã được cấu hình'
        
        -- Test kết nối
        DECLARE @LinkCount INT
        SELECT @LinkCount = COUNT(*) FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN]
        PRINT N'✅ LINK1 hoạt động. Tổng số TK ở LINK1: ' + CAST(@LinkCount AS NVARCHAR(10))
        
        -- Test STK ở LINK1
        IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] WHERE RTRIM(SOTK) = RTRIM(@TestSTK))
            PRINT N'✅ STK tồn tại ở LINK1'
        ELSE
            PRINT N'❌ STK KHÔNG tồn tại ở LINK1'
    END
    ELSE
    BEGIN
        PRINT N'❌ LINK1 CHƯA được cấu hình'
    END
END TRY
BEGIN CATCH
    PRINT N'❌ LỖI khi truy cập LINK1: ' + ERROR_MESSAGE()
END CATCH

PRINT N''

-- =============================================
-- 9️⃣ TÓM TẮT VẤN ĐỀ
-- =============================================
PRINT N'=========================================='
PRINT N'📊 TÓM TẮT PHÂN TÍCH'
PRINT N'=========================================='
PRINT N''
PRINT N'Hãy kiểm tra các điểm sau:'
PRINT N'1. Bước 1: STK có tồn tại trong bảng không?'
PRINT N'2. Bước 3: EXISTS nào hoạt động (có RTRIM hay không)?'
PRINT N'3. Bước 4: JOIN với KHACHHANG có match không?'
PRINT N'4. Bước 5: JOIN với CHINHANH có match không?'
PRINT N'5. Bước 6: Query FULL JOIN nào trả về kết quả?'
PRINT N'6. Bước 7: Có khoảng trắng ẩn nào trong HEX không?'
PRINT N''
PRINT N'=========================================='
PRINT N'KẾT LUẬN:'
PRINT N'- Nếu bước 6 (KHÔNG RTRIM) trả về 0 rows → PHẢI DÙNG RTRIM'
PRINT N'- Nếu bước 6 (CÓ RTRIM) trả về kết quả → SP đã đúng, check cache'
PRINT N'- Nếu cả 2 đều 0 rows → Vấn đề ở dữ liệu hoặc JOIN'
PRINT N'=========================================='
GO
