package com.newproject.etf.batch;

import com.newproject.etf.dto.EtfDto; // API 응답 DTO
import com.newproject.etf.service.EtfApiService; // API 호출 서비스
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader; // ItemStreamReader 사용
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader; // ItemCountingItemStreamItemReader로 변경

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger; // 페이지 번호를 위한 AtomicInteger
import java.util.stream.Collectors; // List 조작을 위해 추가

// AbstractPagingItemReader 대신 ItemCountingItemStreamItemReader를 직접 구현하여 페이지네이션 로직을 더 명시적으로 제어
public class EtfApiPagingReader implements ItemStreamReader<EtfDto> {

    private final EtfApiService etfApiService;
    private final int pageSize; // 한 번에 조회할 최대 갯수 (예: 10000)

    private AtomicInteger currentPage = new AtomicInteger(0); // 현재 페이지 번호 (1부터 시작)
    private Iterator<EtfDto> currentDataIterator; // 현재 페이지의 데이터를 담을 이터레이터

    // 생성자를 통해 필요한 의존성 주입
    public EtfApiPagingReader(EtfApiService etfApiService, int pageSize) {
        this.etfApiService = etfApiService;
        this.pageSize = pageSize;
        System.out.println("[EtfApiPagingReader] Initialized for pageSize: " + pageSize);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // Job 재시작 시점에 이전 페이지 정보를 로드하거나, 처음 시작 시 초기화
        if (executionContext.containsKey("etfApiPagingReader.page")) {
            currentPage.set(executionContext.getInt("etfApiPagingReader.page"));
            System.out.println("[EtfApiPagingReader] Resuming from page: " + currentPage.get());
        } else {
            /*
            * todo
            *  - 100 페이지부터 배치 돌리기
            *  - 배치 구조 변경 필요
            * */
            currentPage.set(1); // 첫 시작 페이지
            System.out.println("[EtfApiPagingReader] Starting from page 1.");
        }
        // 초기 데이터 로드 (첫 페이지)
        loadPageData();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // 현재 페이지 정보를 ExecutionContext에 저장하여 재시작 시 활용
        executionContext.putInt("etfApiPagingReader.page", currentPage.get());
        System.out.println("[EtfApiPagingReader] Current page saved: " + currentPage.get());
    }

    @Override
    public void close() throws ItemStreamException {
        // 리소스 정리 (필요시)
        currentDataIterator = null;
        System.out.println("[EtfApiPagingReader] Reader closed.");
    }

    @Override
    public EtfDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (currentDataIterator == null || !currentDataIterator.hasNext()) {
            // 현재 페이지의 데이터가 모두 소진되었거나, 초기 로드가 안된 경우 다음 페이지 로드 시도
            currentPage.incrementAndGet(); // 다음 페이지로 이동
            loadPageData(); // 다음 페이지 데이터 로드

            // 만약 다음 페이지에 데이터가 없으면 (API가 빈 리스트 반환) null 반환하여 종료
            if (currentDataIterator == null || !currentDataIterator.hasNext()) {
                System.out.println("[EtfApiPagingReader] No more data from API. Ending read.");
                return null;
            }
        }
        return currentDataIterator.next();
    }

    private void loadPageData() {
        System.out.println("[EtfApiPagingReader] Calling API for page " + currentPage.get() + ", size " + pageSize);
        // EtfApiService를 통해 실제 API 호출을 수행
        // 예: etfApiService.fetchEtfData(targetDate, currentPage.get(), pageSize)
        List<EtfDto> pageData = etfApiService.fetchEtfData(currentPage.get(), pageSize)
                .collectList()
                .block(); // Flux를 List로 변환하고 블로킹

        if (pageData == null || pageData.isEmpty()) {
            System.out.println("[EtfApiPagingReader] API returned no data for page " + currentPage.get() + ".");
            currentDataIterator = null; // 더 이상 읽을 데이터가 없음을 표시
        } else {
            System.out.println("[EtfApiPagingReader] API returned " + pageData.size() + " items for page " + currentPage.get() + ".");
            currentDataIterator = pageData.iterator();
        }
    }
}