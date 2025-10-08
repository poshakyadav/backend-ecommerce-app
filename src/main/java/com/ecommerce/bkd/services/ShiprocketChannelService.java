package com.ecommerce.bkd.services;

import java.util.List;

import com.ecommerce.bkd.dtos.shiprocket.Channel;

public interface ShiprocketChannelService {

    List<Channel> getIntegratedChannels();

    /**
     * Returns the numeric ID of your default channel (by channelCode).
     */
    Long getDefaultChannelId();

}
