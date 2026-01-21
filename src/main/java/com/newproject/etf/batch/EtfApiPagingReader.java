package com.newproject.etf.batch;

import com.newproject.etf.dto.EtfDto; // API 응답 DTO
import com.newproject.etf.service.EtfApiService; // API 호출 서비스
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader; // ItemStreamReader 사용
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger; // 페이지 번호를 위한 AtomicInteger

// AbstractPagingItemReader 대신 ItemCountingItemStreamItemReader를 직접 구현하여 페이지네이션 로직을 더 명시적으로 제어
@Slf4j
public class EtfApiPagingReader implements ItemStreamReader<EtfDto> {

    private final EtfApiService etfApiService;
    private final int pageSize; // 한 번에 조회할 최대 갯수 (예: 10000)

    private final AtomicInteger currentPage = new AtomicInteger(1); // 페이지 번호는 1부터 시작하는 것이 일반적입니다.
    private Iterator<EtfDto> currentDataIterator; // 현재 페이지의 데이터를 담을 이터레이터

    // 생성자를 통해 필요한 의존성 주입
    public EtfApiPagingReader(EtfApiService etfApiService, int pageSize) {
        this.etfApiService = etfApiService;
        this.pageSize = pageSize;
        log.info("Initialized for pageSize: {}", pageSize);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // Job 재시작 시점에 이전 페이지 정보를 로드하거나, 처음 시작 시 초기화
        if (executionContext.containsKey("etfApiPagingReader.page")) {
            currentPage.set(executionContext.getInt("etfApiPagingReader.page"));
            log.info("Resuming from page: {}", currentPage.get());
        } else {
            // 특정 페이지부터 시작해야 한다면 JobParameter 등을 통해 외부에서 주입받는 것이 좋습니다.
            // 여기서는 일반적인 시작 페이지인 1로 설정합니다.
            currentPage.set(1);
            log.info("Starting from page 1.");
        }
        // 초기 데이터 로드 (첫 페이지)
        loadPageData();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // 현재 페이지 정보를 ExecutionContext에 저장하여 재시작 시 활용
        executionContext.putInt("etfApiPagingReader.page", currentPage.get());
        log.info("Current page saved: {}", currentPage.get());
    }

    @Override
    public void close() throws ItemStreamException {
        // 리소스 정리 (필요시)
        currentDataIterator = null;
        log.info("Reader closed.");
    }

    @Override
    public EtfDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (currentDataIterator == null || !currentDataIterator.hasNext()) {
            // open()에서 첫 페이지를 로드한 후, 데이터가 소진되면 다음 페이지를 로드합니다.
            currentPage.incrementAndGet();
            loadPageData(); // 다음 페이지 데이터 로드

            // 다음 페이지를 로드했음에도 데이터가 없다면, 모든 페이지를 다 읽은 것이므로 종료합니다.
            if (currentDataIterator == null || !currentDataIterator.hasNext()) {
                log.info("No more data from API. Ending read.");
                return null;
            }
        }
        return currentDataIterator.next();
    }

    private void loadPageData() {
        log.info("Calling API for page {}, size {}", currentPage.get(), pageSize);

        // EtfApiService를 통해 실제 API 호출을 수행
        // API 호출이 무한정 대기하는 것을 방지하기 위해 타임아웃을 설정하는 것이 안전합니다.
        List<EtfDto> pageData = etfApiService.fetchEtfData(currentPage.get(), pageSize)
                .collectList()
                .block(Duration.ofSeconds(30)); // 30초 타임아웃 설정

        if (pageData == null || pageData.isEmpty()) {
            log.info("API returned no data for page {}.", currentPage.get());
            currentDataIterator = null; // 더 이상 읽을 데이터가 없음을 표시
        } else {
            log.info("API returned {} items for page {}.", pageData.size(), currentPage.get());
            currentDataIterator = pageData.iterator();
        }
    }
}