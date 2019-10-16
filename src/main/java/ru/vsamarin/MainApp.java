package ru.vsamarin;

import lombok.extern.slf4j.Slf4j;
import ru.voskhod.sc.soap_adapter.service.generated.types.v1_0.GetTasksListResponseType;
import ru.vsamarin.soap.STPBrokerSoapClient;

@Slf4j
public class MainApp {

    public static void main(String... args) {
        final String serviceUrl = "https://sc-int.minsvyaz.ru/STPBroker/1.0?wsdl";
        STPBrokerSoapClient client = new STPBrokerSoapClient(serviceUrl);

        STPBrokerSoapClient.ResponseWrapper<GetTasksListResponseType> responseWrapper = client.getTasksList();
        if (responseWrapper.isSuccessful()) {
            if (responseWrapper.getResponse().getTasks() != null) {
                responseWrapper.getResponse().getTasks().getTasks().forEach(log::info);
            }
            if (responseWrapper.getResponse().getInitiator() != null) {
                responseWrapper.getResponse().getInitiator().getTasks().forEach(log::info);
            }
            if (responseWrapper.getResponse().getExecutor() != null) {
                responseWrapper.getResponse().getExecutor().getTasks().forEach(log::info);
            }
        } else {
            log.error(responseWrapper.getSoapFault().getFaultString());
        }

    }

}
