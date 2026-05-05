// ============================================================
// Smart Parking System - Complete Script
// Features: Auth, Booking, AI Recommendation, History,
//           Search/Filter, Payment & Fine Calculator
// ============================================================

// ── Toast Notification System ──────────────────────────────
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    const icons = { success: '✓', error: '✕', info: 'ℹ' };
    toast.innerHTML = `<span class="toast-icon">${icons[type]}</span><span class="toast-message">${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('hide');
        setTimeout(() => toast.remove(), 400);
    }, 3500);
}

function setButtonLoading(button, isLoading) {
    if (isLoading) { button.classList.add('loading'); button.disabled = true; }
    else { button.classList.remove('loading'); button.disabled = false; }
}

function validateInput(input, message) {
    if (!input.value.trim()) {
        input.classList.add('shake');
        showToast(message, 'error');
        setTimeout(() => input.classList.remove('shake'), 500);
        return false;
    }
    return true;
}

// ── Registration ────────────────────────────────────────────
function registerUser() {
    const name = document.getElementById("name");
    const email = document.getElementById("email");
    const vehicle = document.getElementById("vehicle");
    const password = document.getElementById("password");
    const btn = document.querySelector("button");

    if (!validateInput(name, "Please enter your full name!")) return;
    if (!validateInput(email, "Please enter your email!")) return;
    if (!validateInput(vehicle, "Please enter your vehicle number!")) return;
    if (!validateInput(password, "Please enter a password!")) return;

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email.value)) {
        email.classList.add('shake');
        showToast("Please enter a valid email address!", 'error');
        setTimeout(() => email.classList.remove('shake'), 500);
        return;
    }

    const passwordValidation = validatePassword(password.value);
    if (!passwordValidation.isValid) {
        password.classList.add('shake');
        showToast(passwordValidation.message, 'error');
        setTimeout(() => password.classList.remove('shake'), 500);
        return;
    }

    setButtonLoading(btn, true);
    setTimeout(() => {
        localStorage.setItem("userEmail", email.value);
        localStorage.setItem("userPassword", password.value);
        localStorage.setItem("userName", name.value);
        localStorage.setItem("userVehicle", vehicle.value);
        showToast("Registration successful! Redirecting...", 'success');
        setTimeout(() => { window.location.href = "login.html"; }, 1500);
    }, 1000);
}

function validatePassword(password) {
    if (password.length < 8) return { isValid: false, message: "Password must be at least 8 characters long!" };
    if (!/[A-Z]/.test(password)) return { isValid: false, message: "Password must contain at least 1 uppercase letter!" };
    if (!/[a-z]/.test(password)) return { isValid: false, message: "Password must contain at least 1 lowercase letter!" };
    if (!/[0-9]/.test(password)) return { isValid: false, message: "Password must contain at least 1 number!" };
    if (!/[@#$!%*?&]/.test(password)) return { isValid: false, message: "Password must contain at least 1 special character (@#$!%*?&)!" };
    return { isValid: true, message: "Password is valid!" };
}

function checkPasswordStrength(password) {
    const requirements = {
        length: password.length >= 8,
        uppercase: /[A-Z]/.test(password),
        lowercase: /[a-z]/.test(password),
        number: /[0-9]/.test(password),
        special: /[@#$!%*?&]/.test(password)
    };
    const reqs = {
        length: document.getElementById('req-length'),
        uppercase: document.getElementById('req-uppercase'),
        lowercase: document.getElementById('req-lowercase'),
        number: document.getElementById('req-number'),
        special: document.getElementById('req-special')
    };
    for (const [key, element] of Object.entries(reqs)) {
        if (element) {
            if (requirements[key]) {
                element.classList.add('valid');
                element.innerHTML = '✓ ' + element.textContent.substring(2);
            } else {
                element.classList.remove('valid');
                element.innerHTML = '○ ' + element.textContent.substring(2);
            }
        }
    }
    return Object.values(requirements).every(req => req);
}

// ── Login ───────────────────────────────────────────────────
function loginUser() {
    const email = document.getElementById("loginEmail");
    const password = document.getElementById("loginPassword");
    const btn = document.querySelector("button");

    if (!validateInput(email, "Please enter your email!")) return;
    if (!validateInput(password, "Please enter your password!")) return;

    setButtonLoading(btn, true);
    setTimeout(() => {
        const storedEmail = localStorage.getItem("userEmail");
        const storedPassword = localStorage.getItem("userPassword");
        if (email.value === storedEmail && password.value === storedPassword) {
            localStorage.setItem("loggedIn", "true");
            showToast("Login successful! Redirecting...", 'success');
            setTimeout(() => { window.location.href = "home.html"; }, 1500);
        } else {
            setButtonLoading(btn, false);
            email.classList.add('shake');
            password.classList.add('shake');
            showToast("Invalid email or password!", 'error');
            setTimeout(() => { email.classList.remove('shake'); password.classList.remove('shake'); }, 500);
        }
    }, 1000);
}

function checkLogin() {
    if (localStorage.getItem("loggedIn") !== "true") {
        window.location.href = "login.html";
    } else {
        const dashboard = document.querySelector('.dashboard') || document.querySelector('.booking-form');
        if (dashboard) {
            dashboard.style.opacity = '0';
            setTimeout(() => { dashboard.style.opacity = '1'; }, 100);
        }
    }
}

function logout() {
    showToast("Logging out...", 'info');
    setTimeout(() => {
        localStorage.removeItem("loggedIn");
        window.location.href = "login.html";
    }, 1000);
}

// ── Slot Selection (Home Page) ──────────────────────────────
function selectSlot(slotElement, slotName) {
    document.querySelectorAll('.slot.selected').forEach(s => s.classList.remove('selected'));
    if (!slotElement.classList.contains('occupied')) {
        slotElement.classList.add('selected');
        showToast(`Selected ${slotName}. Click "Book Parking Slot" to continue!`, 'info');
    }
}

// ── AI Slot Recommendation ──────────────────────────────────
// Scores each available slot using: booking history, time of day, slot proximity
function getAIRecommendedSlot() {
    const bookings = getBookingsFromStorage();
    const slotScores = {};
    const allSlots = ['A1','A2','A3','B1','B2','B3','C1','C2','C3'];
    const occupiedSlots = ['B1','B2']; // from your current HTML

    const hour = new Date().getHours();
    const isPeakHour = (hour >= 9 && hour <= 11) || (hour >= 17 && hour <= 19);

    allSlots.forEach(slot => {
        if (occupiedSlots.includes(slot)) return;
        let score = 0;
        // +3 if user has booked this slot before (familiarity preference)
        const timesBooked = bookings.filter(b => b.slot === slot).length;
        score += timesBooked * 3;
        // +2 for near-entrance slots (A row) during peak hours
        if (isPeakHour && slot.startsWith('A')) score += 2;
        // +1 for corner slots (easier to park)
        if (['A1','A3','C1','C3'].includes(slot)) score += 1;
        // +2 for middle row during off-peak (quieter area)
        if (!isPeakHour && slot.startsWith('B') && !occupiedSlots.includes(slot)) score += 2;
        slotScores[slot] = score;
    });

    // Return the slot with highest score
    const available = Object.entries(slotScores);
    if (available.length === 0) return null;
    available.sort((a, b) => b[1] - a[1]);
    return available[0][0];
}

function applyAIRecommendation() {
    const recommended = getAIRecommendedSlot();
    if (!recommended) return;

    // Highlight on booking page grid
    document.querySelectorAll('.slot-item').forEach(el => {
        el.classList.remove('ai-recommended');
        const badge = el.querySelector('.ai-badge');
        if (badge) badge.remove();
    });

    const slotEls = document.querySelectorAll('.slot-item');
    slotEls.forEach(el => {
        if (el.textContent.trim() === recommended && !el.classList.contains('occupied')) {
            el.classList.add('ai-recommended');
            const badge = document.createElement('div');
            badge.className = 'ai-badge';
            badge.textContent = '⭐ AI Pick';
            el.appendChild(badge);
        }
    });

    showToast(`🤖 AI recommends Slot ${recommended} based on your history!`, 'info');
}

// ── Book Slot ───────────────────────────────────────────────
function bookSlot() {
    const slotInput = document.getElementById("slot");
    const date = document.getElementById("date");
    const time = document.getElementById("time");
    const btn = document.querySelector("button");

    const selectedSlot = document.querySelector('.slot-item.selected');
    const slotValue = selectedSlot ? selectedSlot.textContent.replace('⭐ AI Pick','').trim() : slotInput.value;

    if (!slotValue) { showToast("Please select a parking slot!", 'error'); return; }
    if (!validateInput(date, "Please select a date!")) return;
    if (!validateInput(time, "Please select a time!")) return;

    setButtonLoading(btn, true);
    setTimeout(() => {
        const bookingId = 'BK' + Date.now();
        const newBooking = {
            id: bookingId,
            slot: slotValue,
            date: date.value,
            time: time.value,
            status: 'upcoming',
            createdAt: new Date().toISOString()
        };

        // Add to bookings history
        const bookings = getBookingsFromStorage();
        bookings.push(newBooking);
        localStorage.setItem("allBookings", JSON.stringify(bookings));
        localStorage.setItem("lastBooking", JSON.stringify(newBooking));

        showToast(`Parking Slot ${slotValue} booked successfully! ID: ${bookingId}`, 'success');
        setTimeout(() => { window.location.href = "history.html"; }, 1500);
    }, 1000);
}

function selectSlotFromGrid(slotElement, slotName) {
    if (slotElement.classList.contains('occupied')) {
        showToast("This slot is already occupied!", 'error');
        return;
    }
    document.querySelectorAll('.slot-item.selected').forEach(s => s.classList.remove('selected'));
    slotElement.classList.add('selected');
    document.getElementById("slot").value = slotName;
    showToast(`Slot ${slotName} selected!`, 'success');
}

// ── Booking History & Storage ───────────────────────────────
function getBookingsFromStorage() {
    try {
        return JSON.parse(localStorage.getItem("allBookings") || "[]");
    } catch { return []; }
}

let allBookings = [];

function loadBookingHistory() {
    if (localStorage.getItem("loggedIn") !== "true") {
        window.location.href = "login.html"; return;
    }
    allBookings = getBookingsFromStorage();

    // Update statuses based on current date
    const today = new Date();
    allBookings = allBookings.map(b => {
        if (b.status === 'cancelled') return b;
        const bDate = new Date(b.date + 'T' + b.time);
        b.status = bDate < today ? 'past' : 'upcoming';
        return b;
    });
    localStorage.setItem("allBookings", JSON.stringify(allBookings));

    updateSummaryCards(allBookings);
    renderBookingCards(allBookings);
}

function updateSummaryCards(bookings) {
    document.getElementById('totalBookings').textContent = bookings.length;
    document.getElementById('upcomingCount').textContent = bookings.filter(b => b.status === 'upcoming').length;
    document.getElementById('pastCount').textContent = bookings.filter(b => b.status === 'past').length;
    document.getElementById('cancelledCount').textContent = bookings.filter(b => b.status === 'cancelled').length;
}

function renderBookingCards(bookings) {
    const list = document.getElementById('bookingList');
    const empty = document.getElementById('emptyState');
    if (!list) return;

    if (bookings.length === 0) {
        list.innerHTML = '';
        if (empty) empty.style.display = 'flex';
        return;
    }
    if (empty) empty.style.display = 'none';

    list.innerHTML = bookings.map(b => `
        <div class="booking-card status-${b.status}" data-id="${b.id}">
            <div class="booking-card-header">
                <div class="booking-slot-badge">Slot ${b.slot}</div>
                <span class="status-badge status-${b.status}">${b.status.charAt(0).toUpperCase() + b.status.slice(1)}</span>
            </div>
            <div class="booking-card-body">
                <div class="booking-detail"><span>📅</span> ${formatDate(b.date)}</div>
                <div class="booking-detail"><span>⏰</span> ${formatTime(b.time)}</div>
                <div class="booking-detail"><span>🎫</span> ${b.id}</div>
            </div>
            <div class="booking-card-actions">
                ${b.status === 'upcoming' ? `
                    <button class="action-btn pay-btn" onclick="goToPayment('${b.id}')">💳 Pay</button>
                    <button class="action-btn cancel-btn" onclick="cancelBooking('${b.id}')">✕ Cancel</button>
                ` : b.status === 'past' ? `
                    <button class="action-btn pay-btn" onclick="goToPayment('${b.id}')">💳 Pay Fine</button>
                ` : `<span class="cancelled-label">Booking cancelled</span>`}
            </div>
        </div>
    `).join('');
}

function filterBookings() {
    const search = (document.getElementById('searchInput')?.value || '').toLowerCase();
    const statusFilter = document.getElementById('statusFilter')?.value || 'all';
    const slotFilter = document.getElementById('slotFilter')?.value || 'all';

    const filtered = allBookings.filter(b => {
        const matchSearch = b.slot.toLowerCase().includes(search) || b.date.includes(search) || b.id.toLowerCase().includes(search);
        const matchStatus = statusFilter === 'all' || b.status === statusFilter;
        const matchSlot = slotFilter === 'all' || b.slot.startsWith(slotFilter);
        return matchSearch && matchStatus && matchSlot;
    });

    updateSummaryCards(filtered);
    renderBookingCards(filtered);
}

function cancelBooking(bookingId) {
    const bookings = getBookingsFromStorage();
    const idx = bookings.findIndex(b => b.id === bookingId);
    if (idx !== -1) {
        bookings[idx].status = 'cancelled';
        localStorage.setItem("allBookings", JSON.stringify(bookings));
        allBookings = bookings;
        showToast("Booking cancelled successfully.", 'success');
        loadBookingHistory();
    }
}

function goToPayment(bookingId) {
    localStorage.setItem("paymentBookingId", bookingId);
    window.location.href = "payment.html";
}

// ── Payment & Fine Calculator ───────────────────────────────
function loadPaymentPage() {
    const bookingId = localStorage.getItem("paymentBookingId");
    if (!bookingId) {
        // No booking selected — show generic calculator
        return;
    }
    const bookings = getBookingsFromStorage();
    const booking = bookings.find(b => b.id === bookingId);
    if (!booking) return;

    if (document.getElementById('paySlot')) document.getElementById('paySlot').textContent = booking.slot;
    if (document.getElementById('payDate')) document.getElementById('payDate').textContent = formatDate(booking.date);
    if (document.getElementById('payTime')) document.getElementById('payTime').textContent = formatTime(booking.time);
    if (document.getElementById('payDuration')) document.getElementById('payDuration').textContent = '1 Hour (default)';

    calculateFee();
}

function calculateFee() {
    const duration = parseInt(document.getElementById('durationHours')?.value || 1);
    const overstay = parseInt(document.getElementById('overstayHours')?.value || 0);
    const baseRate = 30;
    const fineRate = 50;
    const parkingFee = duration * baseRate;
    const fineFee = overstay > 0 ? overstay * fineRate : 0;
    const total = parkingFee + fineFee;

    if (document.getElementById('feeHours')) document.getElementById('feeHours').textContent = duration;
    if (document.getElementById('parkingFee')) document.getElementById('parkingFee').textContent = '₹' + parkingFee;
    if (document.getElementById('fineHours')) document.getElementById('fineHours').textContent = overstay;
    if (document.getElementById('fineFee')) document.getElementById('fineFee').textContent = '₹' + fineFee;
    if (document.getElementById('totalFee')) document.getElementById('totalFee').innerHTML = '<strong>₹' + total + '</strong>';
    if (document.getElementById('payDuration')) document.getElementById('payDuration').textContent = duration + ' Hour(s)';

    const fineRow = document.getElementById('fineRow');
    if (fineRow) fineRow.style.display = overstay > 0 ? 'flex' : 'none';

    return total;
}

function selectPayment(el, method) {
    document.querySelectorAll('.payment-option').forEach(o => o.classList.remove('selected'));
    el.classList.add('selected');
    document.getElementById('selectedPayment').value = method;
}

function processPayment() {
    const total = calculateFee();
    const method = document.getElementById('selectedPayment').value;
    const bookingId = localStorage.getItem("paymentBookingId") || ('BK' + Date.now());
    const btn = document.getElementById('payBtn');

    setButtonLoading(btn, true);
    setTimeout(() => {
        setButtonLoading(btn, false);

        // Mark booking as paid
        const bookings = getBookingsFromStorage();
        const idx = bookings.findIndex(b => b.id === bookingId);
        if (idx !== -1) {
            bookings[idx].paid = true;
            bookings[idx].amountPaid = total;
            bookings[idx].paymentMethod = method;
            localStorage.setItem("allBookings", JSON.stringify(bookings));
        }

        // Save payment record
        const payments = JSON.parse(localStorage.getItem("payments") || "[]");
        const txnId = 'TXN' + Date.now();
        payments.push({ txnId, bookingId, amount: total, method, date: new Date().toISOString() });
        localStorage.setItem("payments", JSON.stringify(payments));

        // Show modal
        const modal = document.getElementById('paymentModal');
        if (modal) {
            document.getElementById('modalMessage').textContent = `₹${total} paid via ${method.toUpperCase()}`;
            document.getElementById('modalReceipt').innerHTML = `
                <div class="receipt-row"><span>Transaction ID:</span><span>${txnId}</span></div>
                <div class="receipt-row"><span>Booking ID:</span><span>${bookingId}</span></div>
                <div class="receipt-row"><span>Amount:</span><span>₹${total}</span></div>
                <div class="receipt-row"><span>Method:</span><span>${method.toUpperCase()}</span></div>
            `;
            modal.style.display = 'flex';
        }
    }, 1500);
}

function closeModal() {
    document.getElementById('paymentModal').style.display = 'none';
    window.location.href = "history.html";
}

// ── Utility Helpers ─────────────────────────────────────────
function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

function formatTime(timeStr) {
    if (!timeStr) return '-';
    const [h, m] = timeStr.split(':');
    const hr = parseInt(h);
    return `${hr > 12 ? hr - 12 : hr}:${m} ${hr >= 12 ? 'PM' : 'AM'}`;
}

// ── Keyboard Enter Support ──────────────────────────────────
document.addEventListener('DOMContentLoaded', function() {
    const inputs = document.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                const button = document.querySelector('button');
                if (button) button.click();
            }
        });
    });

    // Apply AI recommendation on booking page
    if (document.querySelector('.slot-grid')) {
        applyAIRecommendation();
    }
});
