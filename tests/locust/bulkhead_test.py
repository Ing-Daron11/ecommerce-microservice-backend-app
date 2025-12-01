from locust import HttpUser, task, constant

class BulkheadUser(HttpUser):
    # Sin tiempo de espera entre tareas para maximizar la concurrencia
    wait_time = constant(0)
    
    # Host base del proxy-client (Directo al contenedor, no Gateway)
    host = "http://localhost:8900"

    @task
    def get_products(self):
        # Llamamos al endpoint protegido por Bulkhead (con context-path /app)
        with self.client.get("/app/api/products", catch_response=True) as response:
            if response.status_code == 200:
                # Si recibimos 200, es porque entró al Bulkhead y el Fallback manejó el timeout
                response.success()
            elif response.status_code == 503 or response.status_code == 500:
                # Si recibimos 503 (o 500), es el rechazo del Bulkhead
                response.failure(f"Bulkhead Rejection: {response.status_code}")
            else:
                response.failure(f"Unexpected status: {response.status_code}")
