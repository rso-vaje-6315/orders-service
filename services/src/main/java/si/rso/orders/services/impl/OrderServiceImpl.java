package si.rso.orders.services.impl;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
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
import si.rso.orders.lib.annotations.DiscoverGrpcClient;
import si.rso.orders.lib.enums.OrderStatus;
import si.rso.orders.mappers.OrderMapper;
import si.rso.orders.persistence.OrderEntity;
import si.rso.orders.persistence.OrderProductEntity;
import si.rso.orders.producers.KafkaProducer;
import si.rso.orders.restclients.ProductsApi;
import si.rso.orders.restclients.ShoppingCartApi;
import si.rso.orders.restclients.StockApi;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {
    
    private Logger LOG = LogManager.getLogger(OrderService.class.getSimpleName());

    @PersistenceContext(unitName = "main-jpa-unit")
    private EntityManager em;

    @Inject
    @DiscoverGrpcClient(clientName = "customers-service")
    private Optional<GrpcClient> grpcCustomersClient;

    @Inject
    @DiscoverGrpcClient(clientName = "invoice-service")
    private Optional<GrpcClient> grpcInvoiceClient;

    @Inject
    private KafkaProducer kafkaProducer;

    @Inject
    @DiscoverService("products-service")
    private Optional<String> productsBaseUrl;

    @Inject
    @DiscoverService("shopping-cart-service")
    private Optional<String> shoppingCartBaseUrl;

    @Inject
    @DiscoverService("stock-service")
    private Optional<String> stockBaseUrl;

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
    public Order cancelOrder(String orderId) {
        OrderEntity orderEntity = em.find(OrderEntity.class, orderId);
        orderEntity.setStatus(OrderStatus.CANCELLED);
        OrderEntity updated = em.merge(orderEntity);
        return OrderMapper.fromEntity(updated);
    }

    @CircuitBreaker
    @Timeout
    @Override
    public Order closeOrder(String orderId) {
        OrderEntity orderEntity = em.find(OrderEntity.class, orderId);
        orderEntity.setStatus(OrderStatus.CLOSED);
        OrderEntity updated = em.merge(orderEntity);
        return OrderMapper.fromEntity(updated);
    }

    // @CircuitBreaker
    // @Timeout
    @Override
    public Order createOrder(Order order, String authToken, String customerId) {

        validator.assertNotNull(order.getAddressId());

        // Initial create for order
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCustomerId(customerId);
        orderEntity.setProducts(new ArrayList<>());
        orderEntity.setStatus(OrderStatus.PLACED);
        
        try {
            em.getTransaction().begin();
            
            em.persist(orderEntity);
            
            em.getTransaction().commit();
            
            em.getTransaction().begin();
            this.handleOrderItems(orderEntity, authToken);
            em.merge(orderEntity);
            em.getTransaction().commit();

        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }

        this.handleCustomerData(orderEntity.getId(), customerId, order.getAddressId());

        // posli na analytics
        // TODO A je uredu mesto kjer se zgodi posiljanje obvestila na analytics?
        for (OrderProductEntity orderProductEntity : orderEntity.getProducts()) {
            kafkaProducer.sendToAnalytics(orderProductEntity);
        }

        return OrderMapper.fromEntity(orderEntity);
    }
    
    @Override
    public void fulfillOrder(String orderId) {
        OrderEntity orderEntity = em.find(OrderEntity.class, orderId);
        if (orderEntity == null) {
            throw new NotFoundException(OrderEntity.class, orderId);
        }
        
        if (grpcInvoiceClient.isEmpty()) {
            throw new RestException("Invoice service not discovered!");
        }
        
        var invoiceStub = InvoiceServiceGrpc.newStub(grpcInvoiceClient.get().getChannel());
        
        var customerRequestBuilder = Invoice.Customer.newBuilder()
            .setCountry(orderEntity.getCustomerCountry())
            .setEmail(orderEntity.getCustomerEmail())
            .setPhone(orderEntity.getCustomerPhone())
            .setStreet(orderEntity.getCustomerStreet())
            .setPost(orderEntity.getCustomerPost())
            .setName(orderEntity.getCustomerName())
            .setId(orderEntity.getCustomerId());
        
        var invoiceRequestBuilder = Invoice.InvoiceRequest.newBuilder()
            .setOrderId(orderId)
            .setCustomer(customerRequestBuilder);
        
        invoiceRequestBuilder.addAllItems(
            orderEntity.getProducts()
                .stream()
                .map(product -> {
                    var singleItemBuilder = Invoice.OrderItem.newBuilder();
                    singleItemBuilder.setCode(product.getCode());
                    singleItemBuilder.setName(product.getName());
                    singleItemBuilder.setPrice(product.getPricePerItem());
                    singleItemBuilder.setQuantity(product.getQuantity());
                    return singleItemBuilder.build();
                })
                .collect(Collectors.toList()));
        
        invoiceStub.createInvoice(invoiceRequestBuilder.build(), new StreamObserver<>() {
            @Override
            public void onNext(Invoice.InvoiceResponse value) {
                try {
                    em.getTransaction().begin();
                    OrderEntity fulfilledOrder = em.find(OrderEntity.class, orderId);
                    fulfilledOrder.setStatus(OrderStatus.FULFILLED);
                    em.getTransaction().commit();
                } catch (Exception e) {
                    em.getTransaction().rollback();
                }
                
            }
            
            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                LOG.info("gRPC call to invoice-service completed!");
            }
        });
    }

    private void checkIfEnoughStock(String productId, int quantity) {
        if (stockBaseUrl.isEmpty()) {
            throw new RestException("Cannot find the url for the stock-service");
        }
        // preveri in popravi quantity glede na stock
        StockApi stockApi = RestClientBuilder.newBuilder()
                .baseUri(URI.create(stockBaseUrl.get()))
                .build(StockApi.class);
        
        try {
            ShoppingCart temp = stockApi.getNumberOfAllProducts(productId);
            
            if (quantity > temp.getQuantity()) {
                throw new RestException("Not enough stock for product" + productId + ". " +
                    quantity + " requested but only available " + temp.getQuantity() + ".");
            }
            
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                throw new RestException("Not enough stock for product" + productId + ". " +
                    quantity + " requested but only available 0.");
            } else {
                e.printStackTrace();
                throw new RestException(e.getMessage());
            }
        }
    }

    private void handleOrderItems(OrderEntity orderEntity, String authToken) {
        try {
            // retrieve shopping cart for user
            LOG.info("Building shopping cart rest client...");
            ShoppingCartApi shoppingCartApi = buildShoppingCartApi();
            LOG.info("Calling shopping cart rest client...");
            JsonArray shoppingCartResponse = shoppingCartApi.getShoppingCartsForCustomer("Bearer " + authToken);
            List<ShoppingCart> cartItems = mapJsonResponseToCart(shoppingCartResponse);
            // check stock
            for (ShoppingCart cartItem : cartItems) {
                // TODO dalo bi se zoptimizirat, da ne pošlje cartItems zahtevkov ampak enega vecjega
                checkIfEnoughStock(cartItem.getProductId(), cartItem.getQuantity());
            }
            // Build id list to retrieve
            String idList = cartItems.stream().map(ShoppingCart::getProductId).collect(Collectors.joining(","));
            // retrieve product data
            if (productsBaseUrl.isEmpty()) {
                throw new RestException("Cannot find the url for the products-service");
            }
            LOG.info("Building product rest client...");
            ProductsApi productsApi = RestClientBuilder.newBuilder()
                    .baseUri(URI.create(productsBaseUrl.get()))
                    .build(ProductsApi.class);
            String filterQuery = "id:IN:[" + idList + "]";
            LOG.info("Calling product rest client...");
            JsonArray productsArray = productsApi.getProducts(filterQuery);
            List<Product> products = mapJsonResponseToProduct(productsArray);
            Map<String, Product> productLookup = products.stream().collect(Collectors.toMap(Product::getId, product -> product));
            
            // map products to order items
            List<OrderProductEntity> productEntities = cartItems.stream().map(item -> {
                OrderProductEntity productEntity = new OrderProductEntity();
                productEntity.setQuantity(item.getQuantity());
                productEntity.setProductId(item.getProductId());
                
                Product productDetails = productLookup.get(item.getProductId());
                
                productEntity.setCode(productDetails.getCode());
                productEntity.setName(productDetails.getName());
                productEntity.setPricePerItem(productDetails.getPrice());
                
                return productEntity;
            }).collect(Collectors.toList());
            
            productEntities.forEach(productEntity -> {
                orderEntity.getProducts().add(productEntity);
                productEntity.setOrder(orderEntity);
            });
            double totalPrice = productEntities.stream()
                .mapToDouble(item -> item.getQuantity() * item.getPricePerItem())
                .reduce(0.0, Double::sum);
            orderEntity.setTotalPrice(totalPrice);
            LOG.info("Finished processing order items...");
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RestException("Error creating order! Cart not found");
        } catch (WebApplicationException e) {
            e.printStackTrace();
            throw new RestException("Unknown error when retrieving shopping cart!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RestException("Unknown error!");
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
    
    private List<Product> mapJsonResponseToProduct(JsonArray jsonArray) {
        return jsonArray.stream().map(jsonValue -> {
            JsonObject arrayNode = jsonValue.asJsonObject();
            
            Product product = new Product();
            product.setId(arrayNode.getString("id"));
            product.setCode(arrayNode.getString("code"));
            product.setName(arrayNode.getString("name"));
            product.setPrice((float) arrayNode.getJsonNumber("price").doubleValue());
            return product;
        }).collect(Collectors.toList());
    }
    
    private void handleCustomerData(String orderId, String customerId, String addressId) {
        
        if (grpcCustomersClient.isEmpty()) {
            throw new RestException("Customers service not discovered!");
        }
        
        var stub = CustomersServiceGrpc.newStub(grpcCustomersClient.get().getChannel());
        
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
            }
            
            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                throw new RestException("Error retrieving customer data!");
            }
            
            @Override
            public void onCompleted() {
                LOG.info("gRPC call to customers-service completed!");
            }
        });
    }
    
    private ShoppingCartApi buildShoppingCartApi() {
        if (shoppingCartBaseUrl.isEmpty()) {
            throw new RestException("Cannot find the url for the products-service");
        }
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
            .baseUri(URI.create(shoppingCartBaseUrl.get())).build(ShoppingCartApi.class);
    }
    
}
