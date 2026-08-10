document.addEventListener("DOMContentLoaded", function () {
    initConfirmButtons();
    initActiveMenu();
    initSearchEnter();
});

/* 삭제, 숨김, 완료 같은 버튼 확인 */
function initConfirmButtons() {
    const dangerButtons = document.querySelectorAll(
        ".delete, .hide, .btn-danger, .admin-btn.delete, .admin-btn.hide"
    );

    dangerButtons.forEach(function (button) {
        button.addEventListener("click", function (event) {
            const result = confirm("정말 처리하시겠습니까?");

            if (!result) {
                event.preventDefault();
            }
        });
    });
}

/* 현재 주소 기준 메뉴 active 처리 */
function initActiveMenu() {
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll(".nav a, .admin-menu a, .menu a");

    menuLinks.forEach(function (link) {
        const href = link.getAttribute("href");

        if (!href) {
            return;
        }

        if (currentPath === href || currentPath.startsWith(href)) {
            link.classList.add("active");
        }
    });
}

/* 검색 input에서 Enter 입력 시 form submit */
function initSearchEnter() {
    const searchInputs = document.querySelectorAll(
        "input[name='keyword'], input[type='search']"
    );

    searchInputs.forEach(function (input) {
        input.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                const form = input.closest("form");

                if (form) {
                    form.submit();
                }
            }
        });
    });
}

/* 공통 토스트 메시지 */
function showToast(message) {
    const oldToast = document.querySelector(".toast-message");

    if (oldToast) {
        oldToast.remove();
    }

    const toast = document.createElement("div");
    toast.className = "toast-message";
    toast.innerText = message;

    toast.style.position = "fixed";
    toast.style.bottom = "30px";
    toast.style.left = "50%";
    toast.style.transform = "translateX(-50%)";
    toast.style.background = "#111827";
    toast.style.color = "#ffffff";
    toast.style.padding = "14px 20px";
    toast.style.borderRadius = "14px";
    toast.style.fontSize = "14px";
    toast.style.fontWeight = "700";
    toast.style.zIndex = "9999";
    toast.style.boxShadow = "0 8px 24px rgba(0,0,0,0.18)";

    document.body.appendChild(toast);

    setTimeout(function () {
        toast.remove();
    }, 2000);
}