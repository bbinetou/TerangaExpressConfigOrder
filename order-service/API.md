# Order Service API Documentation

## Base URL
```
http://localhost:8084/orders
```

## Table of Contents
- [Create Order](#create-order)
- [Get Order by ID](#get-order-by-id)
- [Get Orders by Client](#get-orders-by-client)
- [Update Order Status](#update-order-status)
- [Confirm Order](#confirm-order)
- [Cancel Order](#cancel-order)
- [Assign Driver](#assign-driver)
- [Add Tracking Event](#add-tracking-event)
- [Get Tracking History](#get-tracking-history)
- [Create Payment](#create-payment)
- [Error Codes](#error-codes)

---

## Create Order

Creates a new delivery order.

**Endpoint:** `POST /orders`

**Request Body:**
```json
{
  "clientId": 1,
  "parcelId": 5,
  "transportType": "STANDARD",
  "totalPrice": 25.50,
  "scheduledAt": "2026-04-15T10:00:00"
}
```

**Validation Rules:**
- `clientId`: Required, must not be null
- `parcelId`: Required, must not be null, parcel must exist with status CREATED
- `transportType`: Required, must not be blank
- `totalPrice`: Required, must be positive, must match calculated parcel tariff
- `scheduledAt`: Required, must not be null

**Success Response:**
- **Code:** 201 CREATED
- **Content:**
```json
{
  "id": 10,
  "clientId": 1,
  "parcelId": 5,
  "driverId": null,
  "status": "PENDING",
  "transportType": "STANDARD",
  "totalPrice": 25.50,
  "scheduledAt": "2026-04-15T10:00:00",
  "deliveredAt": null
}
```

**Error Responses:**

- **Code:** 400 BAD REQUEST
  - **Reason:** Validation failed (missing required fields, invalid values)
  - **Content:**
  ```json
  {
    "timestamp": "2026-04-13T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Total price must be positive",
    "path": "/orders"
  }
  ```

- **Code:** 404 NOT FOUND
  - **Reason:** Parcel not found
  - **Content:**
  ```json
  {
    "timestamp": "2026-04-13T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Parcel with id 5 not found",
    "path": "/orders"
  }
  ```

- **Code:** 409 CONFLICT
  - **Reason:** Parcel status is not CREATED or totalPrice doesn't match tariff
  - **Content:**
  ```json
  {
    "timestamp": "2026-04-13T10:30:00",
    "status": 409,
    "error": "Conflict",
    "message": "Parcel must have status CREATED",
    "path": "/orders"
  }
  ```

**Requirements:** 4.1, 4.2, 17.4

---

## Get Order by ID

Retrieves a specific order by its ID.

**Endpoint:** `GET /orders/{id}`

**URL Parameters:**
- `id` (Long): Order ID

**Success Response:**
- **Code:** 200 OK
- **Content:**
```json
{
  "id": 10,
  "clientId": 1,
  "parcelId": 5,
  "driverId": 3,
  "status": "ASSIGNED",
  "transportType": "STANDARD",
  "totalPrice": 25.50,
  "scheduledAt": "2026-04-15T10:00:00",
  "deliveredAt": null
}
```

**Error Responses:**

- **Code:** 404 NOT FOUND
  - **Reason:** Order not found
  - **Content:**
  ```json
  {
    "timestamp": "2026-04-13T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Order with id 10 not found",
    "path": "/orders/10"
  }
  ```

---

## Get Orders by Client

Retrieves all orders for a specific client with pagination support.

**Endpoint:** `GET /orders/client/{clientId}`

**URL Parameters:**
- `clientId` (Long): Client ID

**Query Parameters:**
- `page` (int, optional): Page number (default: 0)
- `size` (int, optional): Page size (default: 20)
- `sort` (string, optional): Sort field and direction (e.g., "scheduledAt,desc")

**Example Request:**
```
GET /orders/client/1?page=0&size=10&sort=scheduledAt,desc
```

**Success Response:**
- **Code:** 200 OK
- **Content:**
```json
{
  "content": [
    {
      "id": 10,
      "clientId": 1,
      "parcelId": 5,
      "driverId": 3,
      "status": "DELIVERED",
      "transportType": "STANDARD",
      "totalPrice": 25.50,
      "scheduledAt": "2026-04-15T10:00:00",
      "deliveredAt": "2026-04-15T14:30:00"
    },
    {
      "id": 8,
      "clientId": 1,
      "parcelId": 3,
      "driverId": 2,
      "status": "IN_TRANSIT",
      "transportType": "EXPRESS",
      "totalPrice": 45.00,
      "scheduledAt": "2026-04-14T09:00:00",
      "deliveredAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    }
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true,
  "number": 0,
  "size": 10,
  "numberOfElements": 2,
  "empty": false
}
```

**Requirements:** 2.3

---

## Update Order Status

Updates the status of an existing order.

**Endpoint:** `PUT /orders/{id}/status`

**URL Parameters:**
- `id` (Long): Order ID

**Request Body:**
```json
{
  "status": "IN_TRANSIT"
}
```

**Valid Status Values:**
- `PENDING`
- `CONFIRMED`
- `ASSIGNED`
- `IN_TRANSIT`
- `DELIVERED`
- `CANCELLED`

**Success Response:**
- **Code:** 200 OK
- **Content:**
```json
{
  "id": 10,
  "clientId": 1,
  "parcelId": 5,
  "driverId": 3,
  "status": "IN_TRANSIT",
  "transportType": "STANDARD",
  "totalPrice": 25.50,
  "scheduledAt": "2026-04-15T10:00:00",
  "deliveredAt": null
}
```

**Error Responses:**

- **Code:** 404 NOT FOUND
  - **Reason:** Order not found

- **Code:** 400 BAD REQUEST
  - **Reason:** Invalid status value

---

## Confirm Order

Confirms an order after payment is completed.

**Endpoint:** `POST /orders/{id}/confirm`

**URL Parameters:**
- `id` (Long): Order ID

**Preconditions:**
- Order must exist
- A payment with status COMPLETED must exist for this order

**Success Response:**
- **Code:** 200 OK
- **Content:**
```json
{
  "id": 10,
  "clientId": 1,
  "parcelId": 5,
  "driverId": null,
  "status": "CONFIRMED",
  "transportType": "STANDARD",
  "totalPrice": 25.50,
  "scheduledAt": "2026-04-15T10:00:00",
  "deliveredAt": null
}
```

**Error Responses:**

- **Code:** 404 NOT FOUND
  - **Reason:** Order not found

- **Code:** 409 CONFLICT
  - **Reason:** No completed payment found for this order
  - **Content:**
  ```json
  {
    "timestamp": "2026-04-13T10:30:00",
    "status": 409,
    "error": "Conflict",
    "message": "Order cannot be confirmed without a completed payment",
    "path": "/orders/10/confirm"
  }
  ```

**Requirements:** 4.3, 16.1

---

## Cancel Order

Cancels an order and triggers refund if payment was completed.

**Endpoint:** `POST /orders/{id}/cancel`

**URL Parameters:**
- `id` (Long): Order ID

**Request Body:**
```json
{
  "reason": "Customer requested cancellation"
}
```

**Validation Rules:**
- `reason`: Required, must not be blank

**Success Response:**
- **Code:** 200 OK
- **Content:**
```json
{
  "id": 10,
  "clientId": 1,
  "parcelId": 5,
  "driverId": null,
  "status": "CANCELLED",
  "transportType": "STANDARD",
  "totalPrice": 25.50,
  "scheduledAt": "2026-04-15T10:00:00",
  "deliveredAt": null
}
```

**Side Effects:**
- If a completed payment exists, a refund will be triggered
- Publishes `order.cancelled` event to Kafka

**Error Responses:**

- **Code:** 404 NOT FOUND
  - **Reason:** Order not found

- **Code:** 400 BAD REQUEST
  - **Reason:** Reason is missing or blank

**Requirements:** 4.6, 15.2

---

## Assign Driver

Assigns a driver to an order.

**Endpoint:** `PUT /orders/{id}/assign-driver`

**URL Parameters:**
- `id` (Long): Order ID

**Request Body:**
```json
{
  "driverId": 3
}
```

**Validation Rules:**
- `driverId`: Required, must not be null
- Driver must be available (available=true)

**Preconditions:**
- Order must exist
- Driver must exist and be available

**Success Response:**
- **Code:** 200 OK
- **Content:**
```json
{
  "id": 10,
  "clientId": 1,
  "parcelId": 5,
  "driverId": 3,
  "status": "ASSIGNED",
  "transportType": "STANDARD",
  "totalPrice": 25.50,
  "scheduledAt": "2026-04-15T10:00:00",
  "deliveredAt": null
}
```

**Side Effects:**
- Driver availability is set to false
- Order status is updated to ASSIGNED
- Publishes `order.assigned` event to Kafka

**Error Responses:**

- **Code:** 404 NOT FOUND
  - **Reason:** Order or driver not found

- **Code:** 409 CONFLICT
  - **Reason:** Driver is not available
  - **Content:**
  ```json
  {
    "timestamp": "2026-04-13T10:30:00",
    "status": 409,
    "error": "Conflict",
    "message": "Driver with id 3 is not available",
    "path": "/orders/10/assign-driver"
  }
  ```

**Requirements:** 4.4, 4.5, 17.2

---

## Add Tracking Event

Adds a tracking event to an order's delivery history.

**Endpoint:** `POST /orders/{id}/tracking`

**URL Parameters:**
- `id` (Long): Order ID

**Request Body:**
```json
{
  "city": "Dakar",
  "lat": 14.6928,
  "lng": -17.4467,
  "status": "IN_TRANSIT",
  "timestamp": "2026-04-15T11:30:00"
}
```

**Validation Rules:**
- `city`: Required, must not be blank
- `lat`: Required, must not be null (latitude)
- `lng`: Required, must not be null (longitude)
- `status`: Required, must not be blank
- `timestamp`: Required, must not be null

**Success Response:**
- **Code:** 201 CREATED
- **Content:** Empty body

**Error Responses:**

- **Code:** 404 NOT FOUND
  - **Reason:** Order not found

- **Code:** 400 BAD REQUEST
  - **Reason:** Missing required fields

**Requirements:** 4.8

---

## Get Tracking History

Retrieves the complete tracking history for an order.

**Endpoint:** `GET /orders/{id}/tracking-history`

**URL Parameters:**
- `id` (Long): Order ID

**Success Response:**
- **Code:** 200 OK
- **Content:**
```json
[
  {
    "id": 15,
    "orderId": 10,
    "city": "Dakar",
    "lat": 14.6928,
    "lng": -17.4467,
    "status": "DELIVERED",
    "timestamp": "2026-04-15T14:30:00"
  },
  {
    "id": 14,
    "orderId": 10,
    "city": "Thiès",
    "lat": 14.7886,
    "lng": -16.9260,
    "status": "IN_TRANSIT",
    "timestamp": "2026-04-15T12:00:00"
  },
  {
    "id": 13,
    "orderId": 10,
    "city": "Rufisque",
    "lat": 14.7167,
    "lng": -17.2667,
    "status": "IN_TRANSIT",
    "timestamp": "2026-04-15T11:30:00"
  }
]
```

**Notes:**
- Events are sorted by timestamp in descending order (most recent first)
- Returns empty array if no tracking events exist

**Error Responses:**

- **Code:** 404 NOT FOUND
  - **Reason:** Order not found

**Requirements:** 4.8

---

## Create Payment

Creates a payment for an order.

**Endpoint:** `POST /orders/{id}/payment`

**URL Parameters:**
- `id` (Long): Order ID

**Request Body:**
```json
{
  "amount": 25.50,
  "method": "CARD"
}
```

**Validation Rules:**
- `amount`: Required, must be positive, must match order's totalPrice
- `method`: Required, must be one of: CARD, MOBILE_MONEY, CASH

**Preconditions:**
- Order must exist
- No completed payment must already exist for this order

**Success Response:**
- **Code:** 201 CREATED
- **Content:**
```json
{
  "id": 7,
  "orderId": 10,
  "amount": 25.50,
  "method": "CARD",
  "status": "PENDING",
  "paidAt": null
}
```

**Error Responses:**

- **Code:** 404 NOT FOUND
  - **Reason:** Order not found

- **Code:** 400 BAD REQUEST
  - **Reason:** Amount doesn't match order totalPrice
  - **Content:**
  ```json
  {
    "timestamp": "2026-04-13T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Payment amount must match order total price",
    "path": "/orders/10/payment"
  }
  ```

- **Code:** 409 CONFLICT
  - **Reason:** A completed payment already exists for this order
  - **Content:**
  ```json
  {
    "timestamp": "2026-04-13T10:30:00",
    "status": 409,
    "error": "Conflict",
    "message": "Order already has a completed payment",
    "path": "/orders/10/payment"
  }
  ```

**Requirements:** 6.1, 6.2, 17.1

---

## Error Codes

### Standard HTTP Status Codes

| Code | Description | When It Occurs |
|------|-------------|----------------|
| 200 | OK | Successful GET, PUT requests |
| 201 | Created | Successful POST requests (resource created) |
| 400 | Bad Request | Validation failed, invalid input data |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | User doesn't have permission for this action |
| 404 | Not Found | Requested resource doesn't exist |
| 409 | Conflict | Business rule violation, data conflict |
| 500 | Internal Server Error | Unexpected server error |
| 503 | Service Unavailable | Service is temporarily unavailable |

### Error Response Format

All errors follow this standardized JSON format:

```json
{
  "timestamp": "2026-04-13T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order with id 10 not found",
  "path": "/orders/10"
}
```

**Fields:**
- `timestamp`: ISO 8601 timestamp when the error occurred
- `status`: HTTP status code
- `error`: HTTP status text
- `message`: Human-readable error description
- `path`: Request path that caused the error

**Requirements:** 13.1-13.7

---

## Common Validation Errors

### Missing Required Fields
```json
{
  "timestamp": "2026-04-13T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Client ID is required",
  "path": "/orders"
}
```

### Invalid Data Type
```json
{
  "timestamp": "2026-04-13T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Total price must be positive",
  "path": "/orders"
}
```

### Resource Not Found
```json
{
  "timestamp": "2026-04-13T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order with id 999 not found",
  "path": "/orders/999"
}
```

### Business Rule Violation
```json
{
  "timestamp": "2026-04-13T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Driver with id 3 is not available",
  "path": "/orders/10/assign-driver"
}
```

---

## Authentication

All endpoints (except health checks) require JWT authentication.

**Header:**
```
Authorization: Bearer <jwt_token>
```

**Example:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

If the token is missing or invalid, the API Gateway will return:
```json
{
  "timestamp": "2026-04-13T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing authentication token",
  "path": "/orders"
}
```

---

## Rate Limiting

The API Gateway implements rate limiting to prevent abuse:
- **Limit:** 100 requests per minute per client
- **Response when exceeded:**
```json
{
  "timestamp": "2026-04-13T10:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "path": "/orders"
}
```

---

## Pagination

Endpoints that return lists support pagination using Spring Data's Pageable interface.

**Query Parameters:**
- `page`: Page number (0-indexed, default: 0)
- `size`: Number of items per page (default: 20, max: 100)
- `sort`: Sort criteria in format `field,direction` (e.g., `scheduledAt,desc`)

**Example:**
```
GET /orders/client/1?page=0&size=10&sort=scheduledAt,desc
```

**Response includes pagination metadata:**
```json
{
  "content": [...],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## Event Publishing

Several endpoints publish events to Kafka for asynchronous processing:

| Endpoint | Event Topic | Event Payload |
|----------|-------------|---------------|
| POST /orders | order.created | OrderCreatedEvent |
| POST /orders/{id}/confirm | order.confirmed | OrderConfirmedEvent |
| PUT /orders/{id}/assign-driver | order.assigned | OrderAssignedEvent |
| PUT /orders/{id}/status (IN_TRANSIT) | order.in-transit | OrderInTransitEvent |
| PUT /orders/{id}/status (DELIVERED) | order.delivered | OrderDeliveredEvent |
| POST /orders/{id}/cancel | order.cancelled | OrderCancelledEvent |

These events are consumed by other services (notification-service, tracking-service, grouping-service) for further processing.

---

## Health Check

**Endpoint:** `GET /actuator/health`

**Success Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## Service Information

**Endpoint:** `GET /actuator/info`

**Success Response:**
```json
{
  "app": {
    "name": "order-service",
    "version": "1.0.0",
    "description": "Order orchestration service for delivery system"
  }
}
```
