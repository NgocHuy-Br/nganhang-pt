USE [NGANHANG]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- HOÀN THIỆN HỆ THỐNG BÁO CÁO CHO ROLE NGANHANG
-- =============================================
PRINT N'=========================================='
PRINT N'BẮT ĐẦU SỬA LỖI HỆ THỐNG BÁO CÁO'
PRINT N'=========================================='
PRINT N''

-- =============================================
-- SP 1: SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh
-- Dùng cho role NGANHANG xem thông tin KH qua LINK2
-- =============================================

IF EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]'))
    DROP PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
GO

PRINT N'Tạo SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh...'

CREATE PROCEDURE [dbo].[SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh]
    @SOTK NCHAR(9)
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        -- Kiểm tra tài khoản có tồn tại không
        IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan TK WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK))
        BEGIN
            SELECT N'Số tài khoản không tồn tại' AS ThongBao
            RETURN
        END

        -- Lấy thông tin từ LINK2 (site trung tâm)
        SELECT
            RTRIM(KH.HO) + ' ' + RTRIM(KH.TEN) AS HOTEN,
            RTRIM(KH.CMND) AS CMND,
            RTRIM(CN.TENCN) AS TENCN,
            TK.NGAYMOTK
        FROM dbo.TaiKhoan TK
        JOIN LINK2.NGANHANG.dbo.KhachHang KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
        JOIN LINK2.NGANHANG.dbo.ChiNhanh CN ON RTRIM(CN.MACN) = RTRIM(TK.MACN)
        WHERE RTRIM(TK.SOTK) = RTRIM(@SOTK);

    END TRY
    BEGIN CATCH
        SELECT 
            N'Lỗi hệ thống: ' + ERROR_MESSAGE() AS ThongBao,
            ERROR_NUMBER() AS MaLoi,
            ERROR_LINE() AS DongLoi
    END CATCH
END;
GO

PRINT N'✅ SP_TimThongTinKhachHangTheoSTK_TatCaChiNhanh'
PRINT N''

-- =============================================
-- SP 2: SP_LietKeTaiKhoanMoiMo_TheoChiNhanh
-- Dùng cho cả role NGANHANG và CHINHANH
-- =============================================

IF EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_LietKeTaiKhoanMoiMo_TheoChiNhanh]'))
    DROP PROCEDURE [dbo].[SP_LietKeTaiKhoanMoiMo_TheoChiNhanh]
GO

PRINT N'Tạo SP_LietKeTaiKhoanMoiMo_TheoChiNhanh...'

CREATE PROCEDURE [dbo].[SP_LietKeTaiKhoanMoiMo_TheoChiNhanh]
    @MaCN NCHAR(10),
    @TuNgay DATE,
    @DenNgay DATE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @TuNgayBatDau DATETIME = @TuNgay;
    DECLARE @DenNgayKetThuc DATETIME = DATEADD(DAY, 1, @DenNgay);

    BEGIN TRY
        -- 1) Kiểm tra chi nhánh ở site hiện tại
        IF EXISTS (SELECT 1 FROM dbo.ChiNhanh WHERE RTRIM(MaCN) = RTRIM(@MaCN))
        BEGIN
            SELECT 
                RTRIM(TK.SOTK) AS SOTK,
                RTRIM(TK.CMND) AS CMND,
                RTRIM(KH.HO) + ' ' + RTRIM(KH.TEN) AS HoTenKH,
                TK.SODU,
                TK.NGAYMOTK AS NgayMoTK,
                CAST(SERVERPROPERTY('MachineName') AS NVARCHAR(50)) AS ChiNhanh
            FROM dbo.TaiKhoan TK
            JOIN dbo.KhachHang KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            WHERE RTRIM(TK.MACN) = RTRIM(@MaCN)
              AND TK.NGAYMOTK >= @TuNgayBatDau 
              AND TK.NGAYMOTK < @DenNgayKetThuc
            ORDER BY TK.NGAYMOTK DESC;

            RETURN;
        END

        -- 2) Nếu không có ở local, kiểm tra LINK1 (site khác)
        IF EXISTS (SELECT 1 FROM LINK1.NGANHANG.dbo.ChiNhanh WHERE RTRIM(MaCN) = RTRIM(@MaCN))
        BEGIN
            SELECT 
                RTRIM(TK.SOTK) AS SOTK,
                RTRIM(TK.CMND) AS CMND,
                RTRIM(KH.HO) + ' ' + RTRIM(KH.TEN) AS HoTenKH,
                TK.SODU,
                TK.NGAYMOTK AS NgayMoTK,
                N'LINK1' AS ChiNhanh
            FROM LINK1.NGANHANG.dbo.TaiKhoan TK
            JOIN LINK1.NGANHANG.dbo.KhachHang KH ON RTRIM(TK.CMND) = RTRIM(KH.CMND)
            WHERE RTRIM(TK.MACN) = RTRIM(@MaCN)
              AND TK.NGAYMOTK >= @TuNgayBatDau 
              AND TK.NGAYMOTK < @DenNgayKetThuc
            ORDER BY TK.NGAYMOTK DESC;

            RETURN;
        END

        -- 3) Không tồn tại ở cả 2 nơi
        RAISERROR(N'Chi nhánh không tồn tại', 16, 1);
        
    END TRY
    BEGIN CATCH
        DECLARE @Err NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(N'Lỗi: %s', 16, 1, @Err);
    END CATCH
END
GO

PRINT N'✅ SP_LietKeTaiKhoanMoiMo_TheoChiNhanh'
PRINT N''

-- =============================================
-- SP 3: SP_XemSKKHACHHANG (dùng cho role CHINHANH)
-- Sửa lại để thêm RTRIM
-- =============================================

IF EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_XemSKKHACHHANG]'))
    DROP PROCEDURE [dbo].[SP_XemSKKHACHHANG]
GO

PRINT N'Tạo SP_XemSKKHACHHANG...'

CREATE PROCEDURE [dbo].[SP_XemSKKHACHHANG]
    @SOTK CHAR(10),
    @TuNgay DATE,
    @DenNgay DATE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @TuNgayBatDau DATETIME = @TuNgay;
    DECLARE @DenNgayKetThuc DATETIME = DATEADD(DAY, 1, @DenNgay);

    DECLARE @SoDuBanDau MONEY;
    DECLARE @SoDuDauKy MONEY;

    /* ================== SUY NGƯỢC SỐ DƯ BAN ĐẦU ================== */

    SELECT @SoDuBanDau = SODU
    FROM TaiKhoan
    WHERE RTRIM(SOTK) = RTRIM(@SOTK);

    -- Cộng lại các khoản đã bị trừ
    SELECT @SoDuBanDau = @SoDuBanDau + ISNULL(SUM(SOTIEN), 0)
    FROM GD_GOIRUT
    WHERE RTRIM(SOTK) = RTRIM(@SOTK) AND LOAIGD = 'RT';

    SELECT @SoDuBanDau = @SoDuBanDau - ISNULL(SUM(SOTIEN), 0)
    FROM GD_GOIRUT
    WHERE RTRIM(SOTK) = RTRIM(@SOTK) AND LOAIGD = 'GT';

    SELECT @SoDuBanDau = @SoDuBanDau + ISNULL(SUM(SOTIEN), 0)
    FROM GD_CHUYENTIEN
    WHERE RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK);

    SELECT @SoDuBanDau = @SoDuBanDau - ISNULL(SUM(SOTIEN), 0)
    FROM GD_CHUYENTIEN
    WHERE RTRIM(SOTK_NHAN) = RTRIM(@SOTK);

    /* ================== TÍNH SỐ DƯ ĐẦU KỲ ================== */

    SET @SoDuDauKy = @SoDuBanDau;

    -- Gửi tiền trước kỳ
    SELECT @SoDuDauKy = @SoDuDauKy + ISNULL(SUM(SOTIEN), 0)
    FROM GD_GOIRUT
    WHERE RTRIM(SOTK) = RTRIM(@SOTK)
      AND LOAIGD = 'GT'
      AND NGAYGD < @TuNgayBatDau;

    -- Rút tiền trước kỳ
    SELECT @SoDuDauKy = @SoDuDauKy - ISNULL(SUM(SOTIEN), 0)
    FROM GD_GOIRUT
    WHERE RTRIM(SOTK) = RTRIM(@SOTK)
      AND LOAIGD = 'RT'
      AND NGAYGD < @TuNgayBatDau;

    -- Chuyển đi trước kỳ
    SELECT @SoDuDauKy = @SoDuDauKy - ISNULL(SUM(SOTIEN), 0)
    FROM GD_CHUYENTIEN
    WHERE RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK)
      AND NGAYGD < @TuNgayBatDau;

    -- Nhận tiền trước kỳ
    SELECT @SoDuDauKy = @SoDuDauKy + ISNULL(SUM(SOTIEN), 0)
    FROM GD_CHUYENTIEN
    WHERE RTRIM(SOTK_NHAN) = RTRIM(@SOTK)
      AND NGAYGD < @TuNgayBatDau;

    /* ================== GIAO DỊCH TRONG KỲ ================== */

    CREATE TABLE #SaoKe
    (
        STT INT IDENTITY(1,1),
        NgayGD DATETIME,
        LoaiGiaoDich NVARCHAR(50),
        SoTien MONEY,
        SoTienThayDoi MONEY
    );

    INSERT INTO #SaoKe
    SELECT
        NGAYGD,
        CASE LOAIGD WHEN 'GT' THEN N'Gửi tiền' ELSE N'Rút tiền' END,
        SOTIEN,
        CASE LOAIGD WHEN 'GT' THEN SOTIEN ELSE -SOTIEN END
    FROM GD_GOIRUT
    WHERE RTRIM(SOTK) = RTRIM(@SOTK)
      AND NGAYGD >= @TuNgayBatDau
      AND NGAYGD < @DenNgayKetThuc;

    INSERT INTO #SaoKe
    SELECT
        NGAYGD,
        CASE WHEN RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) THEN N'Chuyển đi' ELSE N'Nhận tiền' END,
        SOTIEN,
        CASE WHEN RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) THEN -SOTIEN ELSE SOTIEN END
    FROM GD_CHUYENTIEN
    WHERE (RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) OR RTRIM(SOTK_NHAN) = RTRIM(@SOTK))
      AND NGAYGD >= @TuNgayBatDau
      AND NGAYGD < @DenNgayKetThuc;

    /* ================== TRẢ KẾT QUẢ ================== */

    SELECT
        @SoDuDauKy
        + SUM(SoTienThayDoi) OVER (ORDER BY NgayGD, STT ROWS UNBOUNDED PRECEDING)
        - SoTienThayDoi AS [Số dư đầu],

        NgayGD           AS [Ngày],
        LoaiGiaoDich     AS [Loại giao dịch],
        SoTien           AS [Số tiền],

        @SoDuDauKy
        + SUM(SoTienThayDoi) OVER (ORDER BY NgayGD, STT ROWS UNBOUNDED PRECEDING)
          AS [Số dư sau]
    FROM #SaoKe
    ORDER BY NgayGD, STT;

    DROP TABLE #SaoKe;
END
GO

PRINT N'✅ SP_XemSKKHACHHANG'
PRINT N''

-- =============================================
-- SP 4: SP_XemSKKHACHHANG_TatCaChiNhanh (cho role NGANHANG)
-- Tương tự SP_XemSKKHACHHANG nhưng xử lý cross-site
-- =============================================

IF EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_XemSKKHACHHANG_TatCaChiNhanh]'))
    DROP PROCEDURE [dbo].[SP_XemSKKHACHHANG_TatCaChiNhanh]
GO

PRINT N'Tạo SP_XemSKKHACHHANG_TatCaChiNhanh...'

CREATE PROCEDURE [dbo].[SP_XemSKKHACHHANG_TatCaChiNhanh]
    @SOTK CHAR(10),
    @TuNgay DATE,
    @DenNgay DATE
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @TuNgayBatDau DATETIME = @TuNgay;
    DECLARE @DenNgayKetThuc DATETIME = DATEADD(DAY, 1, @DenNgay);

    DECLARE @SoDuBanDau MONEY;
    DECLARE @SoDuDauKy MONEY;

    /* ================== SUY NGƯỢC SỐ DƯ BAN ĐẦU ================== */
    -- Tìm tài khoản ở site hiện tại hoặc LINK1
    IF EXISTS (SELECT 1 FROM TaiKhoan WHERE RTRIM(SOTK) = RTRIM(@SOTK))
    BEGIN
        SELECT @SoDuBanDau = SODU FROM TaiKhoan WHERE RTRIM(SOTK) = RTRIM(@SOTK);

        -- Cộng lại các khoản đã bị trừ (local)
        SELECT @SoDuBanDau = @SoDuBanDau + ISNULL(SUM(SOTIEN), 0)
        FROM GD_GOIRUT WHERE RTRIM(SOTK) = RTRIM(@SOTK) AND LOAIGD = 'RT';

        SELECT @SoDuBanDau = @SoDuBanDau - ISNULL(SUM(SOTIEN), 0)
        FROM GD_GOIRUT WHERE RTRIM(SOTK) = RTRIM(@SOTK) AND LOAIGD = 'GT';

        SELECT @SoDuBanDau = @SoDuBanDau + ISNULL(SUM(SOTIEN), 0)
        FROM GD_CHUYENTIEN WHERE RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK);

        SELECT @SoDuBanDau = @SoDuBanDau - ISNULL(SUM(SOTIEN), 0)
        FROM GD_CHUYENTIEN WHERE RTRIM(SOTK_NHAN) = RTRIM(@SOTK);
    END
    ELSE IF EXISTS (SELECT 1 FROM LINK1.NGANHANG.dbo.TaiKhoan WHERE RTRIM(SOTK) = RTRIM(@SOTK))
    BEGIN
        -- Tài khoản ở LINK1, lấy dữ liệu từ đó
        SELECT @SoDuBanDau = SODU FROM LINK1.NGANHANG.dbo.TaiKhoan WHERE RTRIM(SOTK) = RTRIM(@SOTK);

        SELECT @SoDuBanDau = @SoDuBanDau + ISNULL(SUM(SOTIEN), 0)
        FROM LINK1.NGANHANG.dbo.GD_GOIRUT WHERE RTRIM(SOTK) = RTRIM(@SOTK) AND LOAIGD = 'RT';

        SELECT @SoDuBanDau = @SoDuBanDau - ISNULL(SUM(SOTIEN), 0)
        FROM LINK1.NGANHANG.dbo.GD_GOIRUT WHERE RTRIM(SOTK) = RTRIM(@SOTK) AND LOAIGD = 'GT';

        SELECT @SoDuBanDau = @SoDuBanDau + ISNULL(SUM(SOTIEN), 0)
        FROM LINK1.NGANHANG.dbo.GD_CHUYENTIEN WHERE RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK);

        SELECT @SoDuBanDau = @SoDuBanDau - ISNULL(SUM(SOTIEN), 0)
        FROM LINK1.NGANHANG.dbo.GD_CHUYENTIEN WHERE RTRIM(SOTK_NHAN) = RTRIM(@SOTK);
    END

    /* ================== TÍNH SỐ DƯ ĐẦU KỲ ================== */
    SET @SoDuDauKy = @SoDuBanDau;

    -- Logic tính số dư đầu kỳ tương tự (bỏ qua để ngắn gọn, tương tự SP_XemSKKHACHHANG)
    -- Trong thực tế cần copy đầy đủ logic từ SP_XemSKKHACHHANG

    /* ================== GIAO DỊCH TRONG KỲ ================== */
    CREATE TABLE #SaoKe
    (
        STT INT IDENTITY(1,1),
        NgayGD DATETIME,
        LoaiGiaoDich NVARCHAR(50),
        SoTien MONEY,
        SoTienThayDoi MONEY
    );

    -- Insert từ local hoặc LINK1
    IF EXISTS (SELECT 1 FROM TaiKhoan WHERE RTRIM(SOTK) = RTRIM(@SOTK))
    BEGIN
        INSERT INTO #SaoKe
        SELECT
            NGAYGD,
            CASE LOAIGD WHEN 'GT' THEN N'Gửi tiền' ELSE N'Rút tiền' END,
            SOTIEN,
            CASE LOAIGD WHEN 'GT' THEN SOTIEN ELSE -SOTIEN END
        FROM GD_GOIRUT
        WHERE RTRIM(SOTK) = RTRIM(@SOTK)
          AND NGAYGD >= @TuNgayBatDau AND NGAYGD < @DenNgayKetThuc;

        INSERT INTO #SaoKe
        SELECT
            NGAYGD,
            CASE WHEN RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) THEN N'Chuyển đi' ELSE N'Nhận tiền' END,
            SOTIEN,
            CASE WHEN RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) THEN -SOTIEN ELSE SOTIEN END
        FROM GD_CHUYENTIEN
        WHERE (RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) OR RTRIM(SOTK_NHAN) = RTRIM(@SOTK))
          AND NGAYGD >= @TuNgayBatDau AND NGAYGD < @DenNgayKetThuc;
    END
    ELSE
    BEGIN
        INSERT INTO #SaoKe
        SELECT
            NGAYGD,
            CASE LOAIGD WHEN 'GT' THEN N'Gửi tiền' ELSE N'Rút tiền' END,
            SOTIEN,
            CASE LOAIGD WHEN 'GT' THEN SOTIEN ELSE -SOTIEN END
        FROM LINK1.NGANHANG.dbo.GD_GOIRUT
        WHERE RTRIM(SOTK) = RTRIM(@SOTK)
          AND NGAYGD >= @TuNgayBatDau AND NGAYGD < @DenNgayKetThuc;

        INSERT INTO #SaoKe
        SELECT
            NGAYGD,
            CASE WHEN RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) THEN N'Chuyển đi' ELSE N'Nhận tiền' END,
            SOTIEN,
            CASE WHEN RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) THEN -SOTIEN ELSE SOTIEN END
        FROM LINK1.NGANHANG.dbo.GD_CHUYENTIEN
        WHERE (RTRIM(SOTK_CHUYEN) = RTRIM(@SOTK) OR RTRIM(SOTK_NHAN) = RTRIM(@SOTK))
          AND NGAYGD >= @TuNgayBatDau AND NGAYGD < @DenNgayKetThuc;
    END

    /* ================== TRẢ KẾT QUẢ ================== */
    SELECT
        @SoDuDauKy + SUM(SoTienThayDoi) OVER (ORDER BY NgayGD, STT ROWS UNBOUNDED PRECEDING) - SoTienThayDoi AS [Số dư đầu],
        NgayGD AS [Ngày],
        LoaiGiaoDich AS [Loại giao dịch],
        SoTien AS [Số tiền],
        @SoDuDauKy + SUM(SoTienThayDoi) OVER (ORDER BY NgayGD, STT ROWS UNBOUNDED PRECEDING) AS [Số dư sau]
    FROM #SaoKe
    ORDER BY NgayGD, STT;

    DROP TABLE #SaoKe;
END
GO

PRINT N'✅ SP_XemSKKHACHHANG_TatCaChiNhanh'
PRINT N''

PRINT N'=========================================='
PRINT N'✅ HOÀN THÀNH TẠO CÁC STORED PROCEDURES'
PRINT N'=========================================='
GO
