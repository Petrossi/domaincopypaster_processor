package com.domainsurvey.crawler.service.dao.impl;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Service;
import com.domainsurvey.crawler.model.domain.DomainCrawlingInfo;
import com.domainsurvey.crawler.service.dao.DomainCrawlingInfoService;
import com.domainsurvey.crawler.service.dao.repository.DomainCrawlingInfoRepository;

@Service
@RequiredArgsConstructor
public class DomainCrawlingInfoServiceImpl implements DomainCrawlingInfoService {

    private final DomainCrawlingInfoRepository domainCrawlingInfoRepository;

    @Override
    public Optional<DomainCrawlingInfo> find(Long id) {
        return domainCrawlingInfoRepository.findById(id);
    }

    @Override
    public void save(DomainCrawlingInfo domainInfo) {
        domainCrawlingInfoRepository.save(domainInfo);
    }
}