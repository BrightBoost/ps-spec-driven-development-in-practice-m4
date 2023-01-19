# Case management

Case management API for the legal team.

```bash
./mvnw test
./mvnw spring-boot:run
```

Endpoints: `GET /cases`, `GET /cases/{id}`, `POST /cases`, `DELETE /cases/{id}`, `GET /owners/{email}/cases`.

Case dates are dd-MM-yyyy. Errors are returned as {"error": "..."}.
