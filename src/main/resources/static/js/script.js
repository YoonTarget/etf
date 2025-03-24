document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll(".tab-link");
    const contents = document.querySelectorAll(".tab-content");
    const themeToggle = document.getElementById("theme-toggle");
    const body = document.body;
    const sections = document.querySelectorAll(".title");
    const tooltip = document.getElementById("tooltip");

    sections.forEach(section => {
        section.addEventListener("mouseenter", function(event) {
            tooltip.innerText = section.getAttribute("data-tooltip");
            tooltip.style.display = "block";
        });

        section.addEventListener("mousemove", function(event) {
            tooltip.style.left = event.pageX + 10 + "px";
            tooltip.style.top = event.pageY + 10 + "px";
        });

        section.addEventListener("mouseleave", function() {
            tooltip.style.display = "none";
        });
    });

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

                    // 🚀 데이터 요청 함수 호출 (탭이 활성화된 후)
                    fetchTabData(target);
                }
            });
        });
    });

    function fetchTabData(target) {
        fetch(`/openDataApi/get${target}PriceInfo`)
            .then(res => {
                if (!res.ok) {
                    throw new Error("서버 응답 오류");
                }
                return res.json(); // JSON 데이터로 변환
            })
            .then(data => {
                console.log("받은 데이터:", data);
                renderTabData(target, data); // 렌더링 함수 호출
            })
            .catch(err => console.error("데이터 불러오기 실패:", err));
    };

    // 🎯 데이터를 화면에 렌더링하는 함수
    function renderTabData(target, data) {
        const tableBody = document.getElementById(`${target}-list`);
        tableBody.innerHTML = ""; // 기존 데이터 초기화

        data.forEach(item => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${item.itmsNm}</td>
                <td>${item.srtnCd}</td>
            `;
            tableBody.appendChild(row);
        });
    }

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
