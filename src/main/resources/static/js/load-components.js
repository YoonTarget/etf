// js/load-components.js

async function loadHTML(elementId, filePath) {
    try {
        const response = await fetch(filePath);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const html = await response.text();
        document.getElementById(elementId).innerHTML = html;
        console.log(`${filePath} loaded successfully into #${elementId}`);
    } catch (error) {
        console.error(`Error loading ${filePath}:`, error);
    }
}

// 이 함수는 모든 페이지에서 공통적으로 사용될 JavaScript 로직 (예: 다크모드)을 처리합니다.
// 헤더/푸터 로드 후에 실행되어야 합니다.
function initializeCommonFeatures() {
    const themeToggle = document.getElementById("theme-toggle");
    const body = document.body;
    const homeTitle = document.getElementById("home-title");

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

    // home-title 클릭 이벤트 (경로는 프로젝트 구조에 따라 조정)
    homeTitle.addEventListener("click", () => {
        window.location.href = "/etf-list.html";
    });
}