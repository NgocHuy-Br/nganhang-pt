USE [NGANHANG]
GO

-- =============================================
-- DEBUG: Kiểm tra STK tồn tại ở site nào và MACN của nó
-- =============================================

DECLARE @TestSTK NCHAR(9) = '785001251';

PRINT N'=========================================='
PRINT N'🔍 KIỂM TRA STK Ở SITE NÀO VÀ MACN CỦA NÓ'
PRINT N'=========================================='
PRINT N''

-- 1. Kiểm tra server hiện tại
PRINT N'--- Server hiện tại ---'
SELECT @@SERVERNAME AS [Server Name]
PRINT N''

-- 2. Kiểm tra STK ở site hiện tại
PRINT N'--- STK ở site hiện tại ---'
IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@TestSTK))
BEGIN
    PRINT N'✅ STK TỒN TẠI ở site hiện tại'
    
    SELECT 
        SOTK,
        CMND,
        MACN AS [MACN của TK],
        '[' + MACN + ']' AS [MACN brackets],
        SODU,
        NGAYMOTK
    FROM dbo.TAIKHOAN
    WHERE RTRIM(SOTK) = RTRIM(@TestSTK)
END
ELSE
BEGIN
    PRINT N'❌ STK KHÔNG TỒN TẠI ở site hiện tại'
END
PRINT N''

-- 3. Kiểm tra STK ở LINK1
PRINT N'--- STK ở LINK1 ---'
BEGIN TRY
    IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] WHERE RTRIM(SOTK) = RTRIM(@TestSTK))
    BEGIN
        PRINT N'✅ STK TỒN TẠI ở LINK1'
        
        SELECT 
            SOTK,
            CMND,
            MACN AS [MACN của TK],
            '[' + MACN + ']' AS [MACN brackets],
            SODU,
            NGAYMOTK
        FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN]
        WHERE RTRIM(SOTK) = RTRIM(@TestSTK)
    END
    ELSE
    BEGIN
        PRINT N'❌ STK KHÔNG TỒN TẠI ở LINK1'
    END
END TRY
BEGIN CATCH
    PRINT N'❌ LỖI khi truy cập LINK1: ' + ERROR_MESSAGE()
END CATCH
PRINT N''

-- 4. Kiểm tra CHINHANH ở site hiện tại
PRINT N'--- CHINHANH ở site hiện tại ---'
SELECT 
    MACN,
    '[' + MACN + ']' AS [MACN brackets],
    RTRIM(MACN) AS [MACN_RTRIM],
    TENCN
FROM dbo.CHINHANH
ORDER BY MACN
PRINT N''

-- 5. Kiểm tra CHINHANH ở LINK1
PRINT N'--- CHINHANH ở LINK1 ---'
BEGIN TRY
    SELECT 
        MACN,
        '[' + MACN + ']' AS [MACN brackets],
        RTRIM(MACN) AS [MACN_RTRIM],
        TENCN
    FROM [LINK1].[NGANHANG].[dbo].[CHINHANH]
    ORDER BY MACN
END TRY
BEGIN CATCH
    PRINT N'❌ LỖI: ' + ERROR_MESSAGE()
END CATCH
PRINT N''

-- 6. Thử JOIN ĐÚNG theo logic phân tán
PRINT N'=========================================='
PRINT N'TEST LOGIC ĐÚNG: JOIN THEO SITE CỦA STK'
PRINT N'=========================================='
PRINT N''

-- 6.1: Nếu STK ở site hiện tại
PRINT N'--- Nếu STK ở site hiện tại → JOIN với CHINHANH site hiện tại ---'
IF EXISTS (SELECT 1 FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@TestSTK))
BEGIN
    -- Lấy MACN từ TK
    DECLARE @MACN_Local NCHAR(10)
    SELECT @MACN_Local = MACN FROM dbo.TAIKHOAN WHERE RTRIM(SOTK) = RTRIM(@TestSTK)
    
    PRINT N'MACN của TK: [' + @MACN_Local + ']'
    
    -- Kiểm tra MACN này có trong CHINHANH site hiện tại không
    IF EXISTS (SELECT 1 FROM dbo.CHINHANH WHERE RTRIM(MACN) = RTRIM(@MACN_Local))
    BEGIN
        PRINT N'✅ MACN tồn tại trong CHINHANH site hiện tại'
        
        SELECT 
            KH.HO + ' ' + KH.TEN AS HOTEN,
            KH.CMND,
            CN.TENCN,
            TK.NGAYMOTK
        FROM dbo.TAIKHOAN TK
        INNER JOIN dbo.KHACHHANG KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
        INNER JOIN dbo.CHINHANH CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
        WHERE RTRIM(TK.SOTK) = RTRIM(@TestSTK)
    END
    ELSE
    BEGIN
        PRINT N'❌ MACN KHÔNG tồn tại trong CHINHANH site hiện tại'
        PRINT N'   → Có thể MACN này thuộc về LINK1'
        PRINT N'   → Đây là DATA ISSUE: STK ở site này nhưng MACN trỏ đến chi nhánh ở site khác!'
        
        -- Kiểm tra MACN có trong CHINHANH của LINK1 không
        BEGIN TRY
            IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[CHINHANH] WHERE RTRIM(MACN) = RTRIM(@MACN_Local))
            BEGIN
                PRINT N'   → ✅ MACN tồn tại trong CHINHANH của LINK1'
                PRINT N'   → CẦN FIX: Hoặc thêm CHINHANH vào site này, hoặc chuyển STK sang LINK1'
            END
        END TRY
        BEGIN CATCH
            PRINT N'   → Không kiểm tra được LINK1'
        END CATCH
    END
END
PRINT N''

-- 6.2: Nếu STK ở LINK1
PRINT N'--- Nếu STK ở LINK1 → JOIN với CHINHANH LINK1 ---'
BEGIN TRY
    IF EXISTS (SELECT 1 FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] WHERE RTRIM(SOTK) = RTRIM(@TestSTK))
    BEGIN
        SELECT 
            KH.HO + ' ' + KH.TEN AS HOTEN,
            KH.CMND,
            CN.TENCN,
            TK.NGAYMOTK
        FROM [LINK1].[NGANHANG].[dbo].[TAIKHOAN] TK
        INNER JOIN [LINK1].[NGANHANG].[dbo].[KHACHHANG] KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
        INNER JOIN [LINK1].[NGANHANG].[dbo].[CHINHANH] CN ON RTRIM(TK.MACN) = RTRIM(CN.MACN)
        WHERE RTRIM(TK.SOTK) = RTRIM(@TestSTK)
    END
END TRY
BEGIN CATCH
    PRINT N'❌ LỖI: ' + ERROR_MESSAGE()
END CATCH
PRINT N''

PRINT N'=========================================='
PRINT N'📊 KẾT LUẬN'
PRINT N'=========================================='
PRINT N'1. Kiểm tra STK tồn tại ở đâu (bước 2, 3)'
PRINT N'2. Kiểm tra MACN của STK có trong CHINHANH cùng site không'
PRINT N'3. Nếu STK ở site A nhưng MACN thuộc site B → DATA ISSUE'
PRINT N'4. Giải pháp:'
PRINT N'   a) Thêm CHINHANH vào site có STK'
PRINT N'   b) Hoặc di chuyển STK sang đúng site của MACN'
PRINT N'=========================================='
GO
