const tooltips = {
    "ETF": [ "(Exchange Traded Fund)", "특정 지수의 성과를 추종하는 상장지수펀드입니다." ],
    "ETN": ["(Exchange Traded Note)", "증권사가 발행하는 파생결합증권으로, 특정 지수를 따라갑니다." ],
    "ELW": [ "(Equity Linked Warrant)", "특정 주식이나 지수를 기초로 하는 금융상품입니다." ]
};

const brands = [
    "KODEX(삼성자산운용)",
    "TIGER(미래에셋자산운용)",
    "RISE(KB자산운용)",
    "ACE(한국투자신탁운용)",
    "SOL(신한자산운용)",
    "PLUS(한화자산운용)"
];

document.addEventListener("DOMContentLoaded", function () {
    let allItems = []; // 전체 데이터를 저장하는 전역 변수
    const tabs = document.querySelectorAll(".tab-link");
    let active = document.querySelector(".tab-link.active");
    const title = document.querySelector(".title");
    const tooltip = document.getElementById("tooltip");
    let pageNo = 1; // 현재 페이지
    let numOfRows = 10; // 페이지 당 노출 데이터 수
    let totalCount = 0; // 전체 데이터 수
    let totalPages = 0; // 전체 페이지 수
    let startPageNo = 0; // 시작 페이지
    let endPageNo = 0; // 마지막 페이지
    let params = new URLSearchParams(this.location.search);
//    document.getElementById("start-date").value = params.get("beginBasDt") || "";
//    document.getElementById("end-date").value = params.get("endBasDt") || "";
    document.getElementById("search-id").value = params.get("likeItmsNm") || "";

    fetchData();

    document.getElementById("search-id").addEventListener("input", function(event) {
        const searchValue = event.target.value.trim().toLowerCase();
        filterAndRenderData(searchValue);
    });

    /*
    document.getElementById("search-btn").addEventListener("click", function() {
        const searchValue = document.getElementById("search-id").value.trim().toLowerCase();
        filterAndRenderData(searchValue);
    });
    */

    // ✅ 전체 데이터를 필터링하고 렌더링하는 함수
    function filterAndRenderData(searchValue) {
        let filteredItems;
        if (searchValue === "") {
            // 검색어가 없으면 전체 데이터 사용
            filteredItems = allItems;
        } else {
            // 종목명(itmsNm)을 기준으로 필터링 (대소문자 구분 없이)
            filteredItems = allItems.filter(item => {
                return item.itmsNm.toLowerCase().includes(searchValue);
            });
        }

        // ✅ 필터링된 데이터와 함께 렌더링 함수 호출
        renderData(filteredItems);
    }

    title.addEventListener("mouseenter", function(event) {
        tooltip.innerText = tooltips["ETF"][1];
        tooltip.style.display = "block";
    });

    title.addEventListener("mousemove", function(event) {
        tooltip.style.left = event.pageX + 10 + "px";
        tooltip.style.top = event.pageY + 10 + "px";
    });

    title.addEventListener("mouseleave", function() {
        tooltip.style.display = "none";
    });

    // 🟢 탭 클릭 이벤트
    /*
    tabs.forEach(tab => {
        tab.addEventListener("click", function () {
            const target = this.textContent;

            tabs.forEach(t => t.classList.remove("active"));
            this.classList.add("active");
            title.innerText = target + tooltips[target][0];

            // 🚀 데이터 요청 함수 호출 (탭이 활성화된 후)
            params.set("tab", target);
            fetchData();
        });
    });
    */

    // API 호출 함수
    // ✅ 수정된 fetchData 함수
    function fetchData() {
        // 💡 URL 파라미터만 업데이트하고, 기본 페이지 URL은 유지합니다.
        const url = `${location.pathname}`;
        history.pushState({}, "", url); // ✅ 주소창을 /etf?param=value 형태로 유지

        // ✅ API는 /etf/recent로 호출
        fetch(`/etf/recent`)
            .then(res => {
                if (!res.ok) {
                    throw new Error("서버 응답 오류");
                }
                return res.json();
            })
            .then(data => {
                allItems = data; // 전체 데이터를 전역 변수에 저장
                filterAndRenderData(""); // ✅ 초기 로딩 후 검색 기능 활성화 (빈 검색어로 시작)
            })
            .catch(err => console.error("데이터 불러오기 실패:", err));
    };

    // 🎯 데이터를 화면에 렌더링하는 함수 (수정됨)
    function renderData(data) {
        const tableBody = document.getElementById("list");
        tableBody.innerHTML = ""; // 기존 데이터 초기화

        // 기존의 페이징 로직 및 변수들은 모두 삭제합니다.
        // data는 이미 모든 최신 데이터가 담긴 배열입니다.
        const items = data;

        // 데이터가 없는 경우를 처리합니다.
        if (!items || items.length === 0) {
            tableBody.innerHTML = "<tr><td colspan='7' style='text-align: center;'>데이터가 없습니다.</td></tr>";
            return;
        }

        // --- 현재 페이지 데이터 추출 ---
        // ✅ 필터링된 배열을 슬라이싱
//        const startIndex = (pageNo - 1) * numOfRows;
//        const endIndex = Math.min(startIndex + numOfRows, filteredItems.length);
//        const currentPageItems = filteredItems.slice(startIndex, endIndex);

        // 데이터를 순회하며 테이블에 행을 추가합니다.
        items.forEach(key => {
            const row = document.createElement("tr");
            row.classList.add("hover:bg-blue-100", "transition", "cursor-pointer");
            row.addEventListener("click", () => view(key.srtnCd));

            // 날짜 형식 변환 (YYYY-MM-DD)
            let basDt = key.basDt;
            if (basDt && basDt.length === 8) {
                basDt = basDt.substring(0, 4) + "-" + basDt.substring(4, 6) + "-" + basDt.substring(6);
            }

            // 등락률(fltRt) 포맷팅 및 색상 설정
            const fltRtValue = Number(key.fltRt);
            let fltRt = fltRtValue.toLocaleString();
            let fltRtColor = "";
            if(fltRtValue > 0) {
                fltRt = `+${fltRt}`;
                fltRtColor = "color: red;";
            }
            else if(fltRtValue === 0) {
                fltRtColor = "color: black;";
            }
            else {
                fltRtColor = "color: blue;";
            }

            row.innerHTML = `
                <td>${basDt}</td>
                <td>${key.itmsNm}</td>
                <td>${Number(key.clpr)?.toLocaleString() || "0"}원</td>
                <td style="${fltRtColor}">${fltRt}%</td>
                <td>${Number(key.trqu)?.toLocaleString() || "0"}건</td>
                <td>${Number(key.trPrc)?.toLocaleString() || "0"}원</td>
                <td>${Number(key.mrktTotAmt)?.toLocaleString() || "0"}원</td>
            `;
            tableBody.appendChild(row);
        });

        // 모든 페이징 버튼 생성 코드를 삭제합니다.
        // 필요한 경우, "pagination" 요소를 완전히 숨기거나 제거합니다.
        /*
        const pagination = document.getElementById("pagination");
        if (pagination) {
            pagination.innerHTML = '';
        }
        */
    }

    // jQuery UI Datepicker 활성화
    /*
    $("#start-date, #end-date").datepicker({
        dateFormat: "yy-mm-dd" // 날짜 포맷 지정 (예: 2025-03-20)
    });
    */

    /*
    document.getElementById("search-btn").addEventListener("click", function() {
//        const startDate = document.getElementById("start-date").value;
//        const endDate = document.getElementById("end-date").value;
        const searchValue = document.getElementById("search-id").value.trim();
        params = new URLSearchParams({
//            "beginBasDt" : startDate,
//            "endBasDt" : endDate,
            "likeItmsNm" : searchValue,
            "pageNo" : 1
        });

        fetchData();
    });
    */

    /*
    document.getElementById("search-id").addEventListener("keydown", function(event) {
        if(event.key === "Enter") {
            event.preventDefault();
            document.getElementById("search-btn").click();
        }
    });
    */

    /*
    document.getElementById("pagination").addEventListener("click", function(event) {
        if(!event.target.classList.contains("page-btn")) return;

        const page = event.target.innerHTML.trim();

        if(page === "이전") {
            pageNo = Math.max(startPageNo - numOfRows, 1);
        }
        else if(page === "다음") {
            pageNo = Math.min(endPageNo + numOfRows, totalPages);
        }
        else if(page === "처음") {
            pageNo = 1;
        }
        else if(page === "마지막") {
            pageNo = totalPages;
        }
        else {
            pageNo = parseInt(page);
        }

        params.set("pageNo", pageNo);

        fetchData();
    });
    */
});

function view(srtnCd) {
    location.href = `/etf-detail/${srtnCd}`;

    /*
    fetch(`/etf/${srtnCd}`)
        .then(res => {
            if (!res.ok) {
                throw new Error("서버 응답 오류");
            }
            return res.json();
        })
        .then(data => {
            document.querySelector("main").innerHTML = `
                <h2>${data.itmsNm} (${data.srtnCd})</h2>
                <p>기준일: ${data.basDt}</p>
                <p>현재가: ${Number(data.clpr).toLocaleString()}원</p>
                <p>등락률: ${data.fltRt}%</p>
                <p>거래량: ${Number(data.trqu).toLocaleString()}건</p>
                <p>거래대금: ${Number(data.trPrc).toLocaleString()}원</p>
                <p>시가총액: ${Number(data.mrktTotAmt).toLocaleString()}원</p>
                <button onclick="history.back()">뒤로가기</button>
            `;
            console.log(data);
//            allItems = data; // 전체 데이터를 전역 변수에 저장
//            filterAndRenderData(""); // ✅ 초기 로딩 후 검색 기능 활성화 (빈 검색어로 시작)
        })
        .catch(err => console.error("데이터 불러오기 실패:", err));
    */
}