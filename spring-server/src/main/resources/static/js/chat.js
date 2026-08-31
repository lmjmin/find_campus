document.addEventListener("DOMContentLoaded", () => {
    const messageArea = document.getElementById("messageArea");
    if (messageArea) {
        messageArea.scrollTop = messageArea.scrollHeight;
    }

    const selectedRoomListItem = document.querySelector(".chat-room-item.active");
    if (selectedRoomListItem) {
        selectedRoomListItem.scrollIntoView({ block: "nearest" });
    }

    const searchInput = document.getElementById("chatRoomSearchInput") || document.getElementById("chatSearchInput");
    const roomItems = Array.from(document.querySelectorAll(".chat-room-item, .room-item"));
    if (searchInput && roomItems.length > 0) {
        searchInput.addEventListener("input", () => {
            const keyword = searchInput.value.trim().toLowerCase();
            roomItems.forEach((item) => {
                const text = (item.dataset.searchText || item.textContent || "").toLowerCase();
                item.hidden = keyword.length > 0 && !text.includes(keyword);
            });
        });
    }

    const moreButton = document.getElementById("moreMenuButton");
    const moreMenu = document.getElementById("moreMenu");
    if (moreButton && moreMenu) {
        moreButton.setAttribute("aria-haspopup", "true");
        moreButton.setAttribute("aria-expanded", "false");

        moreButton.addEventListener("click", (event) => {
            event.preventDefault();
            event.stopPropagation();
            const nextVisible = !moreMenu.classList.contains("show");
            moreMenu.classList.toggle("show", nextVisible);
            moreButton.setAttribute("aria-expanded", String(nextVisible));
        });

        moreMenu.addEventListener("click", (event) => {
            event.stopPropagation();
        });

        document.addEventListener("click", () => {
            moreMenu.classList.remove("show");
            moreButton.setAttribute("aria-expanded", "false");
        });

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                moreMenu.classList.remove("show");
                moreButton.setAttribute("aria-expanded", "false");
            }
        });
    }

    const messageForm = document.querySelector(".chat-input-area, .message-form");
    if (messageForm) {
        const messageInput = messageForm.querySelector("textarea[name='messageContent'], input[name='messageContent']");

        const resizeMessageInput = () => {
            if (!messageInput || messageInput.tagName.toLowerCase() !== "textarea") {
                return;
            }
            messageInput.style.height = "46px";
            const nextHeight = Math.min(messageInput.scrollHeight, 104);
            messageInput.style.height = `${Math.max(46, nextHeight)}px`;
        };

        if (messageInput) {
            messageInput.addEventListener("input", resizeMessageInput);
            messageInput.addEventListener("keydown", (event) => {
                if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
                    event.preventDefault();
                    if (typeof messageForm.requestSubmit === "function") {
                        messageForm.requestSubmit();
                    } else {
                        messageForm.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
                    }
                }
            });
            resizeMessageInput();
        }

        messageForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            if (!messageInput || !messageInput.value.trim()) {
                alert("메시지를 입력해 주세요.");
                return;
            }

            const formData = new FormData(messageForm);
            const messageContent = String(formData.get("messageContent") || "").trim();
            formData.set("messageContent", messageContent);

            const action = messageForm.getAttribute("action");
            if (!action) {
                alert("채팅 전송 주소를 찾을 수 없습니다.");
                return;
            }

            try {
                const response = await fetch(action, {
                    method: "POST",
                    body: formData,
                    headers: { "X-Requested-With": "XMLHttpRequest" },
                    credentials: "same-origin"
                });

                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }

                messageInput.value = "";
                resizeMessageInput();
                window.location.reload();
            } catch (error) {
                console.error(error);
                alert("메시지를 보내지 못했습니다. 잠시 후 다시 시도해 주세요.");
            }
        });
    }
});
