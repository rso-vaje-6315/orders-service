package si.rso.orders.services.impl;

import com.kumuluz.ee.grpc.client.GrpcClient;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import grpc.Customers;
import grpc.CustomersServiceGrpc;
import grpc.Invoice;
import grpc.InvoiceServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import si.rso.cart.lib.ShoppingCart;
import si.rso.orders.lib.Order;
import si.rso.orders.lib.annotations.CreateGrpcClient;
import si.rso.orders.lib.enums.OrderStatus;
import si.rso.orders.mappers.OrderMapper;
import si.rso.orders.persistence.OrderEntity;
import si.rso.orders.persistence.OrderProductEntity;
import si.rso.orders.restclients.ProductsApi;
import si.rso.orders.restclients.ShoppingCartApi;
import si.rso.orders.services.OrderService;
import si.rso.products.lib.Product;
import si.rso.rest.exceptions.NotFoundException;
import si.rso.rest.exceptions.RestException;
import si.rso.rest.services.Validator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {
    
    private Logger LOG = LogManager.getLogger(OrderService.class.getSimpleName());
    
    @PersistenceContext(unitName = "main-jpa-unit")
    private EntityManager em;
    
    /*@Inject
    private ServiceConfig serviceConfig;*/
    
    @Inject
    @CreateGrpcClient(clientName = "customers-client")
    private GrpcClient grpcCustomersClient;
    
    @Inject
    @CreateGrpcClient(clientName = "invoice-client")
    private GrpcClient grpcInvoiceClient;
    
    @Inject
    private Validator validator;
    
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
    
    // @CircuitBreaker
    // @Timeout
    @Override
    @Transactional
    public Order createOrder(Order order, String authToken, String customerId) {
    
        validator.assertNotNull(order.getAddressId());
        
        // Initial create for order
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCustomerId(customerId);
        orderEntity.setProducts(new ArrayList<>());
        orderEntity.setStatus(OrderStatus.CREATED);
    
        em.persist(orderEntity);
        
        this.handleOrderItems(orderEntity, customerId, authToken);
        
        em.flush();
    
        this.handleCustomerData(orderEntity.getId(), customerId, order.getAddressId());
        
        return OrderMapper.fromEntity(orderEntity);
    }
    
    private void handleOrderItems(OrderEntity orderEntity, String customerId, String authToken) {
        try {
            // retrieve shopping cart for user
            ShoppingCartApi shoppingCartApi = buildShoppingCartApi();
            JsonArray shoppingCartResponse = shoppingCartApi.getShoppingCartsForCustomer("Bearer " + authToken);
            List<ShoppingCart> cartItems = mapJsonResponseToCart(shoppingCartResponse);
            // Build id list to retrieve
            String idList = cartItems.stream().map(ShoppingCart::getProductId).collect(Collectors.joining(","));
            System.err.println(idList);
            // retrieve product data
            ProductsApi productsApi = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:8050"))
                .build(ProductsApi.class);
            JsonObject productsResponse = productsApi.productGraphql("{allProducts(filters: {fields: [{op: IN,field: \"id\",value:\"[" + idList + "]\"}]}){id,code,name,price}}");
            List<Product> products = mapJsonResponseToProduct(productsResponse);
            Map<String, Product> productLookup = products.stream().collect(Collectors.toMap(Product::getId, product -> product));
    
            // map products to order items
            cartItems.stream().map(item -> {
                OrderProductEntity productEntity = new OrderProductEntity();
                productEntity.setQuantity(item.getQuantity());
                productEntity.setProductId(item.getProductId());
                
                Product productDetails = productLookup.get(item.getProductId());
                
                productEntity.setCode(productDetails.getCode());
                productEntity.setName(productDetails.getName());
                productEntity.setPricePerItem(productDetails.getPrice());
                
                return productEntity;
            }).forEach(productEntity -> orderEntity.getProducts().add(productEntity));
            
            em.flush();
    
        } catch (NotFoundException e) {
            throw new RestException("Error creating order! Cart not found");
        } catch (WebApplicationException e) {
            e.printStackTrace();
            throw new RestException("Unknown error when retrieving shopping cart!");
        }
    }
    
    private List<ShoppingCart> mapJsonResponseToCart(JsonArray jsonArray) {
        return jsonArray.stream().map(jsonValue -> {
            JsonObject arrayNode = jsonValue.asJsonObject();
            
            ShoppingCart cart = new ShoppingCart();
            cart.setProductId(arrayNode.getString("productId"));
            cart.setQuantity(arrayNode.getInt("quantity"));
            
            return cart;
        }).collect(Collectors.toList());
    }
    
    private List<Product> mapJsonResponseToProduct(JsonObject jsonObject) {
        return jsonObject.getJsonObject("data")
            .getJsonArray("allProducts")
            .stream().map(jsonValue -> {
            JsonObject node = jsonValue.asJsonObject();
        
            Product product = new Product();
            product.setId(node.getString("id"));
            product.setCode(node.getString("code"));
            product.setName(node.getString("name"));
            product.setPrice((float) node.getJsonNumber("price").doubleValue());
            
            return product;
        }).collect(Collectors.toList());
    }
    
    private void handleCustomerData(String orderId, String customerId, String addressId) {
        var stub = CustomersServiceGrpc.newStub(grpcCustomersClient.getChannel());
    
        var req = Customers.CustomerRequest.newBuilder()
            .setCustomerId(customerId)
            .setAddressId(addressId)
            .build();
    
        stub.getCustomer(req, new StreamObserver<>() {
            @Override
            public void onNext(Customers.CustomerResponse customer) {
                var addr = customer.getAddress();
    
                em.getTransaction().begin();
                OrderEntity orderEntity = em.find(OrderEntity.class, orderId);
                
                orderEntity.setCustomerName(addr.getFirstName() + " " + addr.getLastName());
                orderEntity.setCustomerStreet(addr.getStreet() + " " + addr.getStreetNumber());
                orderEntity.setCustomerPost(addr.getPostalCode() + " " + addr.getPost());
                orderEntity.setCustomerCountry(addr.getCountry());
                orderEntity.setCustomerEmail(addr.getEmail());
                orderEntity.setCustomerPhone(addr.getPhoneNumber());
                
                em.getTransaction().commit();
                // em.flush();
            }
        
            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                throw new RestException("Error retrieving customer data!");
            }
    
            @Override
            public void onCompleted() {
        
            }
        });
    }
    
    private ShoppingCartApi buildShoppingCartApi() {
        return RestClientBuilder.newBuilder()
            .register(new ResponseExceptionMapper<NotFoundException>() {
                
                @Override
                public NotFoundException toThrowable(Response response) {
                    return new NotFoundException("Shopping cart for given customer doesn't exist!");
                }
                
                @Override
                public boolean handles(int status, MultivaluedMap<String, Object> headers) {
                    return status == 404;
                }
            })
            .baseUri(URI.create("http://localhost:8088/v1")).build(ShoppingCartApi.class);
    }
    
    private void createInvoice(Order order) {
        var request = Invoice.InvoiceRequest.newBuilder()
            .setOrderId("OrderID")
            .build();
        
        LOG.info("Starting GRPC connection");
        var invoiceServiceStub = InvoiceServiceGrpc.newStub(grpcInvoiceClient.getChannel());
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
