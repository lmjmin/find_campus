document.addEventListener("DOMContentLoaded", function () {
    initChatScroll();
    initMessageForm();
    initRoomSearch();
});

/* 채팅 화면 아래로 자동 스크롤 */
function initChatScroll() {
    const messages = document.querySelector(".messages");

    if (!messages) {
        return;
    }

    messages.scrollTop = messages.scrollHeight;
}

/* 메시지 입력 */
function initMessageForm() {
    const form = document.querySelector(".message-form");

    if (!form) {
        return;
    }

    const input = form.querySelector("input[name='messageContent']");

    form.addEventListener("submit", function (event) {
        if (!input || input.value.trim() === "") {
            event.preventDefault();
            alert("메시지를 입력해주세요.");
            return;
        }

        /*
            실제 DB 저장은 Spring Boot Controller에서 처리.
            화면에서만 바로 보이게 하고 싶으면 아래 코드 사용 가능.
        */
    });
}

/* 채팅방 검색 */
function initRoomSearch() {
    const searchInput = document.querySelector(".chat-search-box input");

    if (!searchInput) {
        return;
    }

    const roomItems = document.querySelectorAll(".room-item");

    searchInput.addEventListener("input", function () {
        const keyword = searchInput.value.trim().toLowerCase();

        roomItems.forEach(function (room) {
            const text = room.innerText.toLowerCase();

            if (text.includes(keyword)) {
                room.style.display = "flex";
            } else {
                room.style.display = "none";
            }
        });
    });
}

/* 프론트 화면에서 임시 메시지 추가용 */
function appendMessage(content, isMine) {
    const messages = document.querySelector(".messages");

    if (!messages) {
        return;
    }

    const row = document.createElement("div");
    row.className = isMine ? "message-row me" : "message-row other";

    const bubbleArea = document.createElement("div");
    bubbleArea.className = "bubble-area";

    const bubble = document.createElement("div");
    bubble.className = "bubble";
    bubble.innerText = content;

    const time = document.createElement("div");
    time.className = "time";
    time.innerText = getCurrentTimeText();

    bubbleArea.appendChild(bubble);
    bubbleArea.appendChild(time);

    row.appendChild(bubbleArea);
    messages.appendChild(row);

    messages.scrollTop = messages.scrollHeight;
}

function getCurrentTimeText() {
    const now = new Date();
    const hour = now.getHours();
    const minute = now.getMinutes();

    const period = hour < 12 ? "오전" : "오후";
    const displayHour = hour % 12 === 0 ? 12 : hour % 12;
    const displayMinute = String(minute).padStart(2, "0");

    return period + " " + displayHour + ":" + displayMinute;
}