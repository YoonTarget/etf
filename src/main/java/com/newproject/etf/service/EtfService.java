package com.newproject.etf.service;

import java.util.Map;

public interface EtfService {
    String list(String endPoint, Map<String, String> queryParams);
}
