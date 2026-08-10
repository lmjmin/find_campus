document.addEventListener("DOMContentLoaded", function () {
    initCategorySelect();
    initImagePreview();
    initFilterSubmit();
    initSortSubmit();
    initItemCardClick();
});

/* 카테고리 라디오 선택 시 선택 효과 */
function initCategorySelect() {
    const categoryRadios = document.querySelectorAll(".category-list input[type='radio']");

    categoryRadios.forEach(function (radio) {
        radio.addEventListener("change", function () {
            const labels = document.querySelectorAll(".category-list label");

            labels.forEach(function (label) {
                label.classList.remove("selected");
            });

            const selectedLabel = radio.closest("label");

            if (selectedLabel) {
                selectedLabel.classList.add("selected");
            }
        });
    });
}

/* 이미지 업로드 미리보기 */
function initImagePreview() {
    const fileInputs = document.querySelectorAll("input[type='file'][name='images']");

    fileInputs.forEach(function (input) {
        input.addEventListener("change", function () {
            const files = Array.from(input.files);

            if (files.length === 0) {
                return;
            }

            const uploadBox = input.closest(".upload-box");

            if (!uploadBox) {
                return;
            }

            let previewWrap = uploadBox.parentElement.querySelector(".preview-wrap");

            if (!previewWrap) {
                previewWrap = document.createElement("div");
                previewWrap.className = "preview-wrap";
                previewWrap.style.display = "grid";
                previewWrap.style.gridTemplateColumns = "repeat(auto-fill, minmax(90px, 1fr))";
                previewWrap.style.gap = "10px";
                previewWrap.style.marginTop = "12px";

                uploadBox.parentElement.appendChild(previewWrap);
            }

            previewWrap.innerHTML = "";

            files.forEach(function (file) {
                if (!file.type.startsWith("image/")) {
                    return;
                }

                const reader = new FileReader();

                reader.onload = function (event) {
                    const img = document.createElement("img");

                    img.src = event.target.result;
                    img.alt = file.name;

                    img.style.width = "100%";
                    img.style.height = "90px";
                    img.style.objectFit = "cover";
                    img.style.borderRadius = "14px";
                    img.style.border = "1px solid #e5e7eb";

                    previewWrap.appendChild(img);
                };

                reader.readAsDataURL(file);
            });
        });
    });
}

/* 필터 체크 시 자동 검색 */
function initFilterSubmit() {
    const filter = document.querySelector(".filter");

    if (!filter) {
        return;
    }

    const checkboxes = filter.querySelectorAll("input[type='checkbox']");

    checkboxes.forEach(function (checkbox) {
        checkbox.addEventListener("change", function () {
            const form = filter.closest("form");

            if (form) {
                form.submit();
            }
        });
    });
}

/* 정렬 select 변경 시 자동 submit */
function initSortSubmit() {
    const sortBoxes = document.querySelectorAll(".sort-box");

    sortBoxes.forEach(function (select) {
        select.addEventListener("change", function () {
            const form = select.closest("form");

            if (form) {
                form.submit();
                return;
            }

            const url = new URL(window.location.href);
            url.searchParams.set("sort", select.value);
            window.location.href = url.toString();
        });
    });
}

/* 카드 전체 클릭 처리 */
function initItemCardClick() {
    const cards = document.querySelectorAll(".item-card[data-url]");

    cards.forEach(function (card) {
        card.addEventListener("click", function () {
            const url = card.dataset.url;

            if (url) {
                window.location.href = url;
            }
        });
    });
}

/* 등록 폼 기본 검증 */
function validateItemForm() {
    const title = document.querySelector("input[name='title']");
    const itemName = document.querySelector("input[name='itemName']");
    const description = document.querySelector("textarea[name='description']");

    if (title && title.value.trim() === "") {
        alert("제목을 입력해주세요.");
        title.focus();
        return false;
    }

    if (itemName && itemName.value.trim() === "") {
        alert("물건명을 입력해주세요.");
        itemName.focus();
        return false;
    }

    if (description && description.value.trim() === "") {
        alert("상세 설명을 입력해주세요.");
        description.focus();
        return false;
    }

    return true;
}