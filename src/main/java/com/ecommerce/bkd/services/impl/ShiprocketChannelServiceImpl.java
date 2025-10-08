package com.ecommerce.bkd.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.ecommerce.bkd.config.ShiprocketConfig;
import com.ecommerce.bkd.dtos.shiprocket.*;
import com.ecommerce.bkd.exceptions.ShiprocketApiException;
import com.ecommerce.bkd.services.ShiprocketChannelService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiprocketChannelServiceImpl implements ShiprocketChannelService {

    private final ShiprocketApiService apiService;
    private final ShiprocketConfig config;

    /**
     * Get details of all integrated channels
     * API: GET /channels
     */
    @Cacheable(value = "channels")
    @Override
    /**
     * Fetches the list of integrated channels from Shiprocket.
     *
     * @return List of Channel objects representing the integrated channels.
     * @throws ShiprocketApiException if there is an error while fetching the
     *                                channels.
     */
    public List<Channel> getIntegratedChannels() {
        try {
            return apiService.get("/channels",
                    new ParameterizedTypeReference<List<Channel>>() {
                    });
        } catch (Exception e) {
            log.error("Error fetching integrated channels: ", e);
            throw new ShiprocketApiException("Failed to fetch channels", e);
        }
    }

    /**
     * Finds the numeric ID of the channel whose `channelCode` matches
     * the configured default (e.g. "CUSTOM"). Throws if not found.
     */
    @Override
    @Cacheable(value = "defaultChannelId")
    public Long getDefaultChannelId() {
        String code = config.getChannelDefaultCode();
        return getIntegratedChannels().stream()
                .filter(ch -> code.equalsIgnoreCase(ch.getChannelCode()))
                .findFirst()
                .map(Channel::getId)
                .orElseThrow(() -> new ShiprocketApiException(
                        "Default channelCode '" + code + "' not found in Shiprocket"));
    }
}
