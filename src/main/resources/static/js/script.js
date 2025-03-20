document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll(".tab-link");
    const contents = document.querySelectorAll(".tab-content");
    const themeToggle = document.getElementById("theme-toggle");
    const body = document.body;

    // 🌙 다크모드 유지 기능 (localStorage 사용)
    if (localStorage.getItem("dark-mode") === "enabled") {
        body.classList.add("dark-mode");
        themeToggle.textContent = "☀️ 라이트모드";
    }

    themeToggle.addEventListener("click", function () {
        body.classList.toggle("dark-mode");
        if (body.classList.contains("dark-mode")) {
            localStorage.setItem("dark-mode", "enabled");
            themeToggle.textContent = "☀️ 라이트모드";
        } else {
            localStorage.setItem("dark-mode", "disabled");
            themeToggle.textContent = "🌙 다크모드";
        }
    });

    // 🟢 탭 클릭 이벤트
    tabs.forEach(tab => {
        tab.addEventListener("click", function () {
            const target = this.getAttribute("data-tab");

            tabs.forEach(t => t.classList.remove("active"));
            this.classList.add("active");

            contents.forEach(content => {
                content.classList.remove("active");
                if (content.id === target) {
                    content.classList.add("active");
                }
            });
        });
    });

    // 🔍 검색 기능
    function addSearchFunctionality(inputId, tableId) {
        document.getElementById(inputId).addEventListener("keyup", function () {
            const searchValue = this.value.toLowerCase();
            const rows = document.querySelectorAll(`#${tableId} tr`);

            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(searchValue) ? "" : "none";
            });
        });
    }

    addSearchFunctionality("search-etf", "etf-list");
    addSearchFunctionality("search-etn", "etn-list");
    addSearchFunctionality("search-elw", "elw-list");
});
