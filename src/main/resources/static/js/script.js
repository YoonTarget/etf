const tooltips = {
    "ETF": [ "(Exchange Traded Fund)", "특정 지수의 성과를 추종하는 상장지수펀드입니다." ],
    "ETN": ["(Exchange Traded Note)", "증권사가 발행하는 파생결합증권으로, 특정 지수를 따라갑니다." ],
    "ELW": [ "(Equity Linked Warrant)", "특정 주식이나 지수를 기초로 하는 금융상품입니다." ]
};

document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll(".tab-link");
    const active = document.querySelector(".active");
    const themeToggle = document.getElementById("theme-toggle");
    const body = document.body;
    const title = document.querySelector(".title");
    const tooltip = document.getElementById("tooltip");
    let pageNo = 1;
    let numOfRows = 10;
    let totalCount = 0;
    let totalPages = 0;

    fetchData(active.innerText);

    title.addEventListener("mouseenter", function(event) {
        tooltip.innerText = tooltips[document.querySelector(".active").textContent][1];
        tooltip.style.display = "block";
    });

    title.addEventListener("mousemove", function(event) {
        tooltip.style.left = event.pageX + 10 + "px";
        tooltip.style.top = event.pageY + 10 + "px";
    });

    title.addEventListener("mouseleave", function() {
        tooltip.style.display = "none";
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
            const target = this.textContent;

            tabs.forEach(t => t.classList.remove("active"));
            this.classList.add("active");
            title.innerText = target + tooltips[target][0];

            // 🚀 데이터 요청 함수 호출 (탭이 활성화된 후)
            fetchData(target);
        });
    });

    // API 호출 함수
    function fetchData(target, params) {
        fetch(`/openDataApi/get${target}PriceInfo?${params}`)
            .then(res => {
                if (!res.ok) {
                    throw new Error("서버 응답 오류");
                }
                return res.json(); // JSON 데이터로 변환
            })
            .then(data => {
                console.log("받은 데이터:", data);
                renderData(target, data); // 렌더링 함수 호출
            })
            .catch(err => console.error("데이터 불러오기 실패:", err));
    };

    // 🎯 데이터를 화면에 렌더링하는 함수
    function renderData(target, data) {
        const tableBody = document.getElementById("list");
        tableBody.innerHTML = ""; // 기존 데이터 초기화

        const body = data?.response?.body;

        pageNo = body?.pageNo;
        numOfRows = body?.numOfRows;
        totalCount = body?.totalCount;
        totalPages = Math.ceil(totalCount / numOfRows);

        const item = body?.items?.item;

        let pagination = document.getElementById("pagination");

        pagination.innerHTML = `
            <button class="page-btn">&laquo;</button>
        `;

        for(let i = 0; i < 10; i++) {
            pagination.innerHTML += `
                <button class="page-btn">${pageNo + i}</button>
            `;
        }

        pagination.innerHTML = `
            <button class="page-btn">&raquo;</button>
        `;

        if (Array.isArray(item)) {
            item.forEach(key => {
                console.log(key);
                const row = document.createElement("tr");
                let basDt = key.basDt;
                basDt = basDt.substring(0, 4) + "-" + basDt.substring(4, 6) + "-" + basDt.substring(6);

                let fltRt = key.fltRt;
                if(fltRt.startsWith(".")) {
                    fltRt = "0" + fltRt;
                }
                else if(fltRt.startsWith("-.")) {
                    fltRt = "-0" + fltRt.substring(fltRt.indexOf("."));
                }
                row.innerHTML = `
                    <td>${basDt}</td>
                    <td>${key.itmsNm}</td>
                    <td>${Number(key.clpr)?.toLocaleString() || "0"}원</td>
                    <td>${fltRt}%</td>
                    <td>${Number(key.trqu)?.toLocaleString() || "0"}건</td>
                    <td>${Number(key.trPrc)?.toLocaleString() || "0"}원</td>
                    <td>${Number(key.mrktTotAmt)?.toLocaleString() || "0"}원</td>
                `;
                tableBody.appendChild(row);
            });
        } else {
            console.error("❌ 'items'가 배열이 아닙니다:", item);
        }
    }

    // jQuery UI Datepicker 활성화
    $("#start-date, #end-date").datepicker({
        dateFormat: "yy-mm-dd" // 날짜 포맷 지정 (예: 2025-03-20)
    });

    document.getElementById("search-btn").addEventListener("click", function() {
        const startDate = document.getElementById("start-date").value;
        const endDate = document.getElementById("end-date").value;
        const searchValue = document.getElementById("search-id").value;
        const params = new URLSearchParams({
            "startBasDt" : startDate,
            "endBasDt" : endDate,
            "likeItmsNm" : searchValue,
            "pageNo" : pageNo
        });

        fetchData(document.querySelector(".active").innerText, params)
    });

    document.getElementById("search-id").addEventListener("keydown", function(event) {
        if(event.key === "Enter") {
            event.preventDefault();
            document.getElementById("search-btn").click();
        }
    });

    document.getElementById("pagination").addEventListener("click", function(event) {
        const page = event.target.innerHTML.trim();
        console.log(page);

        if(page === "«") {
            pageNo -= 1;
        }
        else if(page === "»") {
            pageNo += 1;
        }
        else {
            pageNo = page;
        }

        document.getElementById("search-btn").click();
    });
});