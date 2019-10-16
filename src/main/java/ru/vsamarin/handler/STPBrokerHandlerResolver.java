package ru.vsamarin.handler;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.Collections;
import java.util.List;

/**
 * Класс формирует цепочку обработчиков клиента сервиса STPBroker
 *
 * @author v.samarin
 * @since 10.10.2019
 */
public class STPBrokerHandlerResolver implements HandlerResolver {

    @Override
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        return Collections.singletonList(new STPBrokerLogMessageHandler());
    }
}
