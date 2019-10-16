package ru.vsamarin.soap;

import lombok.Getter;
import ru.voskhod.sc.soap_adapter.service.generated.basic.v1_0.*;
import ru.voskhod.sc.soap_adapter.service.generated.service.v1_0.STPBroker;
import ru.voskhod.sc.soap_adapter.service.generated.service.v1_0.STPBrokerException;
import ru.voskhod.sc.soap_adapter.service.generated.service.v1_0.STPBrokerPortType;
import ru.voskhod.sc.soap_adapter.service.generated.service.v1_0.SendEventsException;
import ru.voskhod.sc.soap_adapter.service.generated.types.v1_0.*;
import ru.vsamarin.handler.STPBrokerHandlerResolver;

import javax.xml.ws.BindingProvider;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class STPBrokerSoapClient {

    private STPBrokerPortType client;

    public STPBrokerSoapClient(String serviceUrl) {
        try {
            STPBroker service = new STPBroker(new URL(serviceUrl));
            service.setHandlerResolver(new STPBrokerHandlerResolver());
            client = service.getSTPBrokerEndpoint();
            BindingProvider bp = (BindingProvider) client;
            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceUrl);
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("Ошибка при инициализации клиента Ситуационного Центра \"%s\"", serviceUrl)
            );
        }
    }

    public ResponseWrapper<GetTasksListResponseType> getTasksList() {
        GetTasksListRequestType request = new GetTasksListRequestType();
        request.setHeader(buildRequestHeader());
        request.setRole(RoleType.ALL.name());
        request.setChanged(String.valueOf(Boolean.TRUE));
        try {
            GetTasksListResponseType response = client.getTasksList(request);
            return new ResponseWrapper<>(response);
        } catch (STPBrokerException e) {
            return new ResponseWrapper<>(getSoapFault(e));
        }
    }

    public ResponseWrapper<CreateTasksResponseType> createTasks() {
        TaskDescriptorType.Attributes attributes = new TaskDescriptorType.Attributes();
        attributes.getAttribute().add(buildAttribute("agreement", "agreement$106028301"));
        attributes.getAttribute().add(buildAttribute("service", "slmService$106028501"));
        attributes.getAttribute().add(buildAttribute("metaClass", "serviceCall$ReglProcedure"));
        attributes.getAttribute().add(buildAttribute("CategoryOfSC", "category$104739367"));
        attributes.getAttribute().add(buildAttribute("topic", "Тестовый запрос номер 5"));
        attributes.getAttribute().add(buildAttribute("descriptionInRTF", "&lt;u>тест&lt;/u>"));
        attributes.getAttribute().add(buildAttribute("SubIS", "units$78478022"));
        attributes.getAttribute().add(buildAttribute("clientName", "Самарин Владислав Сергеевич"));
        attributes.getAttribute().add(buildAttribute("clientEmail", "v.samarin@voskhod.ru"));
        attributes.getAttribute().add(buildAttribute("clientPhone", "+79060948706"));

        TaskDescriptorType task = new TaskDescriptorType();
        task.setId("105");
        task.setAttributes(attributes);

        CreateTasksRequestType.Tasks taskList = new CreateTasksRequestType.Tasks();
        taskList.getTask().add(task);

        CreateTasksRequestType request = new CreateTasksRequestType();
        request.setHeader(buildRequestHeader());
        request.setTasks(taskList);

        try {
            CreateTasksResponseType response = client.createTasks(request);
            return new ResponseWrapper<>(response);
        } catch (STPBrokerException e) {
            return new ResponseWrapper<>(getSoapFault(e));
        }
    }

    public ResponseWrapper<GetCurrentTaskStatusesResponseType> getCurrentTaskStatuses(Collection<String> ids) {
        TaskIdsListType taskIdsList = new TaskIdsListType();
        taskIdsList.getTasks().addAll(ids);

        TaskListRequestType request = new TaskListRequestType();
        request.setHeader(buildRequestHeader());
        request.setTasks(taskIdsList);

        try {
            GetCurrentTaskStatusesResponseType response = client.getCurrentTaskStatuses(request);
            return new ResponseWrapper<>(response);
        } catch (STPBrokerException e) {
            return new ResponseWrapper<>(getSoapFault(e));
        }
    }

    public ResponseWrapper<GetTaskEventListResponseType> getTaskEventLists(Collection<String> ids) {
        TaskIdsListType taskIdsList = new TaskIdsListType();
        taskIdsList.getTasks().addAll(ids);

        GetTaskEventListRequestType request = new GetTaskEventListRequestType();
        request.setHeader(buildRequestHeader());
        request.setTasks(taskIdsList);

        try {
            GetTaskEventListResponseType response = client.getTaskEventLists(request);
            return new ResponseWrapper<>(response);
        } catch (STPBrokerException e) {
            return new ResponseWrapper<>(getSoapFault(e));
        }
    }

    public ResponseWrapper<AckEventPackageResponseType> ackEventPackage(String packageId) {
        AckEventPackageRequestType request = new AckEventPackageRequestType();
        request.setHeader(buildRequestHeader());
        request.setPackageId(packageId);

        try {
            AckEventPackageResponseType response = client.ackEventPackage(request);
            return new ResponseWrapper<>(response);
        } catch (STPBrokerException e) {
            return new ResponseWrapper<>(getSoapFault(e));
        }
    }

    public ResponseWrapper<SendEventsResponseType> sendEvents(String taskId,
                                                              Map<String, String> attributes,
                                                              Collection<String> comments,
                                                              Collection<String> files) {
        List<EventCreateType.AddFileEvent> addFileEventList = convertAddFileEvents(files);
        List<EventCreateType.AddCommentEvent> addCommentEventList = convertAddCommentEvents(comments);
        List<EventCreateType.ChangeAttributeEvent> changeAttributeEventList = convertChangeAttributeEvents(attributes);

        EventCreateType eventType = new EventCreateType();
        eventType.setId(taskId);
        addFileEventList.forEach(eventType.getChangeAttributeEventOrAddFileEventOrAddCommentEvent()::add);
        addCommentEventList.forEach(eventType.getChangeAttributeEventOrAddFileEventOrAddCommentEvent()::add);
        changeAttributeEventList.forEach(eventType.getChangeAttributeEventOrAddFileEventOrAddCommentEvent()::add);

        SendEventsRequestType.Tasks tasks = new SendEventsRequestType.Tasks();
        tasks.getTask().add(eventType);

        SendEventsRequestType request = new SendEventsRequestType();
        request.setHeader(buildRequestHeader());
        request.setTasks(tasks);

        try {
            SendEventsResponseType response = client.sendEvents(request);
            return new ResponseWrapper<>(response);
        } catch (STPBrokerException e) {
            return new ResponseWrapper<>(getSoapFault(e));
        } catch (SendEventsException e) {
            return new ResponseWrapper<>(getSoapFault(e), e.getFaultInfo());
        }
    }

    public boolean isInitialized() {
        return client != null;
    }

    private List<EventCreateType.ChangeAttributeEvent> convertChangeAttributeEvents(Map<String, String> attributes) {
        return attributes == null ?
                Collections.emptyList() :
                attributes.entrySet().stream()
                        .map(this::convertChangeAttributeEvent)
                        .collect(Collectors.toList());
    }

    private EventCreateType.ChangeAttributeEvent convertChangeAttributeEvent(Map.Entry<String, String> entry) {
        EventCreateType.ChangeAttributeEvent event = new EventCreateType.ChangeAttributeEvent();
        event.setId(UUID.randomUUID().toString());
        event.setDate(ZonedDateTime.now());
        event.setName(entry.getKey());
        event.setValue(entry.getValue());
        return event;
    }

    private List<EventCreateType.AddFileEvent> convertAddFileEvents(Collection<String> files) {
        return files == null ?
                Collections.emptyList() :
                files.stream()
                        .map(this::convertAddFileEvent)
                        .collect(Collectors.toList());
    }

    private EventCreateType.AddFileEvent convertAddFileEvent(String file) {
        EventCreateType.AddFileEvent event = new EventCreateType.AddFileEvent();
        event.setId(UUID.randomUUID().toString());
        event.setDate(ZonedDateTime.now());
        event.setBinaryContentLink(file);
        //event.setMimeType();
        //event.setTitle();
        return event;
    }

    private EventCreateType.AddCommentEvent convertAddCommentEvent(String comment) {
        EventCreateType.AddCommentEvent event = new EventCreateType.AddCommentEvent();
        event.setId(UUID.randomUUID().toString());
        event.setDate(ZonedDateTime.now());
        event.setText(comment);
        event.setPrivate(String.valueOf(Boolean.FALSE));
        return event;
    }

    private List<EventCreateType.AddCommentEvent> convertAddCommentEvents(Collection<String> comments) {
        return comments == null ?
                Collections.emptyList() :
                comments.stream()
                        .map(this::convertAddCommentEvent)
                        .collect(Collectors.toList());
    }

    private TaskDescriptorType.Attributes.Attribute buildAttribute(String name, String value) {
        TaskDescriptorType.Attributes.Attribute attribute = new TaskDescriptorType.Attributes.Attribute();
        attribute.setName(name);
        attribute.setValue(value);
        return attribute;
    }

    private RequestType.Header buildRequestHeader() {
        RequestType.Header requestHeader = new RequestType.Header();
        requestHeader.setAccessKey("3d0e3219-6eae-4081-b561-760e11c7a7ec");
        requestHeader.setVisitorId("employee$108924101");
        //requestHeader.setAccessKey("66123118-c367-4464-ad01-63d265c218db");
        //requestHeader.setVisitorId("employee$193407608");
        return requestHeader;
    }

    private STPBrokerFaultType getSoapFault(STPBrokerException e) {
        if (e.getFaultInfo() != null) {
            return e.getFaultInfo();
        }
        STPBrokerFaultType soapFault = new STPBrokerFaultType();
        soapFault.setFaultString(e.getMessage());
        return soapFault;
    }

    private STPBrokerFaultType getSoapFault(SendEventsException e) {
        STPBrokerFaultType soapFault = new STPBrokerFaultType();
        soapFault.setFaultString(e.getMessage());
        return soapFault;
    }

    public enum RoleType {
        ALL,
        INITIATOR,
        EXECUTOR
    }

    @Getter
    public static class ResponseWrapper<T> {
        private final STPBrokerFaultType soapFault;
        private final T response;

        ResponseWrapper(final STPBrokerFaultType soapFault) {
            this(soapFault, null);
        }

        ResponseWrapper(final T responseEntity) {
            this(null, responseEntity);
        }

        ResponseWrapper(final STPBrokerFaultType soapFault, final T response) {
            this.soapFault = soapFault;
            this.response = response;
        }

        public boolean isSuccessful() {
            return soapFault == null;
        }

    }

}
