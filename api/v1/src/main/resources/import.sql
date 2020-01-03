INSERT INTO orders(id, timestamp, customer_id, price, status) VALUES ('520cb7af-b986-4415-ba90-ba3c1a589a40', NOW(), '9349cf54-1946-4915-be7e-7decb9090e8e', 9.2, 0);
INSERT INTO orders(id, timestamp, customer_id, price, status) VALUES ('71f36a65-604d-49df-ab44-1ae710f943ed', NOW(), 'f1d93df1-eb38-4249-9818-cc5b76b67eaa', 5.16, 0);

INSERT INTO order_products(id, timestamp, order_id, product_id, quantity, price_per_item) VALUES ('b70bea9c-8d55-4fb4-86aa-6c72b04a4fb5', NOW(), '520cb7af-b986-4415-ba90-ba3c1a589a40', 'fbace5c1-653c-42c0-aa02-78cc4ea4fac1', 5, 1.84);
INSERT INTO order_products(id, timestamp, order_id, product_id, quantity, price_per_item) VALUES ('06852980-51d1-4ed2-bbb5-d5c866514d32', NOW(), '71f36a65-604d-49df-ab44-1ae710f943ed', '66100dac-ff08-4ac2-9c1a-fa5120ff4838', 1, 1.98);
INSERT INTO order_products(id, timestamp, order_id, product_id, quantity, price_per_item) VALUES ('4f1a1f39-3bd5-4c3f-b1e0-97433d149843', NOW(), '71f36a65-604d-49df-ab44-1ae710f943ed', 'd519b320-80a3-40e2-ad96-7edd8c878630', 1, 1.34);
INSERT INTO order_products(id, timestamp, order_id, product_id, quantity, price_per_item) VALUES ('d91dd454-2556-4d31-93c7-c6a94f5e6e05', NOW(), '71f36a65-604d-49df-ab44-1ae710f943ed', '18c18291-6541-4317-a374-b9a03b65b90a', 1, 0.89);
