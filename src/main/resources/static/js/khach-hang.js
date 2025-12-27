// ==================== QUẢN LÝ KHÁCH HÀNG ====================
let selectedKH = null;
let allKhachHang = [];

// Load danh sách khách hàng
function loadKhachHang() {
    fetch('/staff/khach-hang')
        .then(res => res.json())
        .then(data => {
            allKhachHang = data;
            renderKhachHang(data);
        })
        .catch(err => {
            console.error('Lỗi:', err);
            alert('Lỗi khi tải danh sách khách hàng');
        });
}

// Hiển thị khách hàng ra table
function renderKhachHang(data) {
    const tbody = document.getElementById('tableKH');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Không có dữ liệu</td></tr>';
        return;
    }

    tbody.innerHTML = data.map((kh, index) => `
        <tr onclick="selectKhachHang('${kh.cmnd}')">
            <td>${index + 1}</td>
            <td>${kh.cmnd}</td>
            <td>${kh.hoten}</td>
            <td>${kh.phai}</td>
            <td>${kh.soDT || ''}</td>
            <td>${kh.diaChi || ''}</td>
            <td>${kh.tenChiNhanh || ''}</td>
        </tr>
    `).join('');
}

// Chọn khách hàng
function selectKhachHang(cmnd) {
    selectedKH = allKhachHang.find(kh => kh.cmnd === cmnd);

    // Highlight row
    document.querySelectorAll('#tableKH tr').forEach(tr => {
        tr.classList.remove('table-active');
    });
    event.target.closest('tr').classList.add('table-active');

    // Enable buttons
    document.getElementById('btnSuaKH').disabled = false;
    document.getElementById('btnXoaKH').disabled = false;
    document.getElementById('btnXemTaiKhoan').disabled = false;
    document.getElementById('btnMoTaiKhoan').disabled = false;
}

// Tìm kiếm khách hàng
document.getElementById('searchKH').addEventListener('input', function () {
    const keyword = this.value.toLowerCase();
    const filtered = allKhachHang.filter(kh =>
        kh.cmnd.toLowerCase().includes(keyword) ||
        kh.hoten.toLowerCase().includes(keyword) ||
        (kh.soDT && kh.soDT.toLowerCase().includes(keyword))
    );
    renderKhachHang(filtered);

    // Show/hide clear button
    document.getElementById('btnClearSearchKH').style.display = keyword ? 'inline-block' : 'none';
});

// Nút Clear search
document.getElementById('btnClearSearchKH').addEventListener('click', function () {
    document.getElementById('searchKH').value = '';
    this.style.display = 'none';
    renderKhachHang(allKhachHang);
});

// Nút Thêm khách hàng
document.getElementById('btnThemKH').addEventListener('click', function () {
    document.getElementById('modalKHTitle').textContent = 'Thêm khách hàng mới';
    document.getElementById('formKhachHang').reset();
    document.getElementById('inputCMND').disabled = false;
    document.getElementById('inputKHPhai').disabled = false;
    document.getElementById('inputKHMaCN').disabled = false;

    new bootstrap.Modal(document.getElementById('modalKhachHang')).show();
});

// Nút Sửa khách hàng
document.getElementById('btnSuaKH').addEventListener('click', function () {
    if (!selectedKH) {
        alert('Vui lòng chọn khách hàng để sửa');
        return;
    }

    document.getElementById('modalKHTitle').textContent = 'Cập nhật thông tin khách hàng';
    document.getElementById('inputCMND').value = selectedKH.cmnd;
    document.getElementById('inputCMND').disabled = true;
    document.getElementById('inputKHHo').value = selectedKH.ho;
    document.getElementById('inputKHTen').value = selectedKH.ten;
    document.getElementById('inputKHDiaChi').value = selectedKH.diaChi || '';
    document.getElementById('inputNgayCap').value = selectedKH.ngayCap || '';
    document.getElementById('inputKHSDT').value = selectedKH.soDT || '';
    document.getElementById('inputKHPhai').value = selectedKH.phai;
    document.getElementById('inputKHPhai').disabled = true;
    document.getElementById('inputKHMaCN').value = selectedKH.maCN;
    document.getElementById('inputKHMaCN').disabled = true;

    new bootstrap.Modal(document.getElementById('modalKhachHang')).show();
});

// Nút Lưu khách hàng
document.getElementById('btnGhiKH').addEventListener('click', function () {
    const cmnd = document.getElementById('inputCMND').value.trim();
    const ho = document.getElementById('inputKHHo').value.trim();
    const ten = document.getElementById('inputKHTen').value.trim();
    const diaChi = document.getElementById('inputKHDiaChi').value.trim();
    const ngayCap = document.getElementById('inputNgayCap').value;
    const soDT = document.getElementById('inputKHSDT').value.trim();
    const phai = document.getElementById('inputKHPhai').value;
    const maCN = document.getElementById('inputKHMaCN').value;

    if (!cmnd || !ho || !ten || !ngayCap || !soDT || !phai || !maCN) {
        alert('Vui lòng điền đầy đủ thông tin');
        return;
    }

    const isEdit = document.getElementById('inputCMND').disabled;

    if (isEdit) {
        // Cập nhật
        fetch(`/staff/khach-hang/${cmnd}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ho, ten, diaChi, soDT })
        })
            .then(res => res.json())
            .then(result => {
                alert(result.message);
                if (result.result === 1) {
                    bootstrap.Modal.getInstance(document.getElementById('modalKhachHang')).hide();
                    loadKhachHang();
                    selectedKH = null;
                }
            })
            .catch(err => {
                console.error('Lỗi:', err);
                alert('Lỗi khi cập nhật khách hàng');
            });
    } else {
        // Thêm mới
        fetch('/staff/khach-hang', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ cmnd, ho, ten, diaChi, ngayCap, soDT, phai, maCN })
        })
            .then(res => res.json())
            .then(result => {
                alert(result.message);
                if (result.result === 1) {
                    bootstrap.Modal.getInstance(document.getElementById('modalKhachHang')).hide();
                    loadKhachHang();
                }
            })
            .catch(err => {
                console.error('Lỗi:', err);
                alert('Lỗi khi thêm khách hàng');
            });
    }
});

// Nút Xóa khách hàng
document.getElementById('btnXoaKH').addEventListener('click', function () {
    alert('Chức năng đang phát triển');
});

// Nút Xem tài khoản
document.getElementById('btnXemTaiKhoan').addEventListener('click', function () {
    if (!selectedKH) {
        alert('Vui lòng chọn khách hàng để xem tài khoản');
        return;
    }

    document.getElementById('lblKHTaiKhoan').textContent = `${selectedKH.cmnd} - ${selectedKH.hoten}`;
    document.getElementById('tableTaiKhoan').innerHTML = '<tr><td colspan="5" class="text-center">Đang tải...</td></tr>';

    fetch(`/staff/khach-hang/${selectedKH.cmnd}/tai-khoan`)
        .then(res => res.json())
        .then(data => {
            if (data.length === 0) {
                document.getElementById('tableTaiKhoan').innerHTML = '<tr><td colspan="5" class="text-center">Chưa có tài khoản</td></tr>';
            } else {
                document.getElementById('tableTaiKhoan').innerHTML = data.map(tk => `
                    <tr>
                        <td>${tk.soTK}</td>
                        <td>${new Intl.NumberFormat('vi-VN').format(tk.soDu)} VNĐ</td>
                        <td>${tk.ngayMoTK ? new Date(tk.ngayMoTK).toLocaleDateString('vi-VN') : ''}</td>
                        <td>${tk.tenCN || ''}</td>
                        <td>${tk.site || ''}</td>
                    </tr>
                `).join('');
            }
        })
        .catch(err => {
            console.error('Lỗi:', err);
            document.getElementById('tableTaiKhoan').innerHTML = '<tr><td colspan="5" class="text-center text-danger">Lỗi khi tải dữ liệu</td></tr>';
        });

    new bootstrap.Modal(document.getElementById('modalXemTaiKhoan')).show();
});

// Nút Mở tài khoản
document.getElementById('btnMoTaiKhoan').addEventListener('click', function () {
    if (!selectedKH) {
        alert('Vui lòng chọn khách hàng để mở tài khoản');
        return;
    }

    document.getElementById('lblKHMoTK').textContent = `${selectedKH.cmnd} - ${selectedKH.hoten}`;
    document.getElementById('formMoTaiKhoan').reset();

    new bootstrap.Modal(document.getElementById('modalMoTaiKhoan')).show();
});

// Xác nhận mở tài khoản
document.getElementById('btnXacNhanMoTK').addEventListener('click', function () {
    const soTK = document.getElementById('inputSoTK').value.trim();
    const maCN = document.getElementById('inputTKMaCN').value;

    if (!soTK || !maCN) {
        alert('Vui lòng điền đầy đủ thông tin');
        return;
    }

    if (soTK.length !== 9) {
        alert('Số tài khoản phải có 9 ký tự');
        return;
    }

    if (!confirm(`Xác nhận mở tài khoản ${soTK} với số dư ban đầu 100.000.000 VNĐ?`)) {
        return;
    }

    fetch(`/staff/khach-hang/${selectedKH.cmnd}/mo-tai-khoan`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ soTK, maCN })
    })
        .then(res => res.json())
        .then(result => {
            alert(result.message);
            if (result.result === 1) {
                bootstrap.Modal.getInstance(document.getElementById('modalMoTaiKhoan')).hide();
            }
        })
        .catch(err => {
            console.error('Lỗi:', err);
            alert('Lỗi khi mở tài khoản');
        });
});

// Khi load trang, kiểm tra tab nào đang active
document.querySelector('a[data-bs-toggle="tab"]').addEventListener('shown.bs.tab', function (event) {
    if (event.target.getAttribute('data-bs-target') === '#customers') {
        loadKhachHang();
    }
});
