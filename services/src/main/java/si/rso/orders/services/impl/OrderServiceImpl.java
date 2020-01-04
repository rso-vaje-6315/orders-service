package si.rso.orders.services.impl;

import com.kumuluz.ee.grpc.client.GrpcChannelConfig;
import com.kumuluz.ee.grpc.client.GrpcChannels;
import com.kumuluz.ee.grpc.client.GrpcClient;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import grpc.Invoice;
import grpc.InvoiceServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import si.rso.orders.lib.Order;
import si.rso.orders.mappers.OrderMapper;
import si.rso.orders.persistence.OrderEntity;
import si.rso.orders.restclients.ShoppingCartApi;
import si.rso.orders.services.OrderService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    @PersistenceContext(unitName = "main-jpa-unit")
    private EntityManager em;

    @RestClient
    @Inject
    private ShoppingCartApi shoppingCartApi;

    private Logger LOG = LogManager.getLogger(OrderService.class.getSimpleName());

    private InvoiceServiceGrpc.InvoiceServiceStub invoiceServiceStub;

    //    @PostConstruct
    private void initGrpc() {
        try {
            GrpcChannels clientPool = GrpcChannels.getInstance();
            GrpcChannelConfig channelConfig = clientPool.getGrpcClientConfig("invoice-client");
            GrpcClient client = new GrpcClient(channelConfig);

            invoiceServiceStub = InvoiceServiceGrpc.newStub(client.getChannel());
        } catch (SSLException e) {
            e.printStackTrace();
        }
    }

    @CircuitBreaker
    @Timeout
    @Override
    public List<Order> getOrdersByCustomer(String customerId) {
        TypedQuery<OrderEntity> query = em.createNamedQuery(OrderEntity.FIND_BY_CUSTOMER, OrderEntity.class);
        query.setParameter("customerId", customerId);

        return query.getResultStream()
                .map(OrderMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @CircuitBreaker
    @Timeout
    @Override
    public Order getOrder(String orderId) {
        OrderEntity orderEntity = em.find(OrderEntity.class, orderId);
        return OrderMapper.fromEntity(orderEntity);
    }

    @CircuitBreaker
    @Timeout
    @Override
    public List<Order> getOrders(QueryParameters queryParameters) {
        return JPAUtils.queryEntities(em, OrderEntity.class, queryParameters).stream()
                .map(OrderMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @CircuitBreaker
    @Timeout
    @Override
    @Transactional
    public Order updateOrder(Order order) {
        // TODO
        return null;
    }

    @CircuitBreaker
    @Timeout
    @Override
    @Transactional
    public Order createOrder(String authToken) throws MalformedURLException {
    
        ShoppingCartApi shoppingCartApi = RestClientBuilder.newBuilder()
            .baseUri(URI.create("http://localhost:8088/v1")).build(ShoppingCartApi.class);
    
        Response response;
        try {
            response = shoppingCartApi.getShoppingCartsForCustomer("Bearer " + authToken);
        } catch (WebApplicationException e) {
            System.err.println(e.getMessage());
            response = e.getResponse();
        }
    
        System.err.println(response.getStatus());
        if (response.hasEntity()) {
            String responseBody = response.readEntity(String.class);
            System.err.println(responseBody);
        } else {
            System.err.println("Response has no entity!");
        }
        
        return null;
    }

    private void createInvoice(Order order) {
        var request = Invoice.InvoiceRequest.newBuilder()
                .setOrderId("OrderID")
                .build();

        LOG.info("Starting GRPC connection");

        invoiceServiceStub.createInvoice(request, new StreamObserver<Invoice.InvoiceResponse>() {
            @Override
            public void onNext(Invoice.InvoiceResponse invoiceResponse) {
                LOG.info("Connection succeeded! Returned status: " + invoiceResponse.getStatus());
                LOG.info("Generated invoice id: " + invoiceResponse.getInvoiceId());
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                LOG.info("GRPC connection completed!");
            }
        });
    }
}
