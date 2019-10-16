package ru.vsamarin.handler;

import lombok.extern.slf4j.Slf4j;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

/**
 * Обработчик логирующий запросы и ответы клиента сервиса STPBroker
 *
 * @author v.samarin
 * @since 10.10.2019
 */
@Slf4j
public class STPBrokerLogMessageHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SOAPMessage message = context.getMessage();
            message.writeTo(outputStream);
            log.info(outputStream.toString(StandardCharsets.UTF_8.name()));
        } catch (SOAPException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public void close(MessageContext context) {
    }
}
