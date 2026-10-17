package Modelo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.UUID;

public class tpvSmartPoint2MPago {

    // ============================================================
    // CONFIGURACION
    // ============================================================

    private static final String ACCESS_TOKEN = "aqui va el tocken";


    private static final String BASE_URL =
            "https://api.mercadopago.com";

    // Orden que quedo creada y esta bloqueando la terminal
    private static final String ORDER_ID =
            "ORD01M2D13ZBBA8VADGDSHW2YGBS0";

    private static final String TERMINAL_ID =
            "NEWLAND_N950__N950NCB700996754";

    private static final OkHttpClient CLIENT =
            new OkHttpClient();

    private static final Gson GSON =
            new Gson();

    private static final MediaType JSON =
            MediaType.parse("application/json; charset=utf-8");


    // ============================================================
    // MAIN
    // ============================================================

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println(" MERCADO PAGO POINT - CANCELAR ORDER");
        System.out.println("==============================================");

        System.out.println();
        System.out.println("Terminal:");
        System.out.println(TERMINAL_ID);

        System.out.println();
        System.out.println("Order a cancelar:");
        System.out.println(ORDER_ID);

        try {

            // ----------------------------------------------------
            // 1. CONSULTAR ORDEN ANTES DE CANCELAR
            // ----------------------------------------------------

            System.out.println();
            System.out.println("==============================================");
            System.out.println("1. CONSULTANDO ORDER");
            System.out.println("==============================================");

            JsonObject orderAntes =
                    consultarOrder(ORDER_ID);

            if (orderAntes == null) {
                System.out.println(
                        "No fue posible consultar la order."
                );
                return;
            }

            String statusAntes =
                    obtenerStatus(orderAntes);

            System.out.println();
            System.out.println("Status actual:");
            System.out.println(statusAntes);


            // ----------------------------------------------------
            // 2. VALIDAR SI YA ESTA CANCELADA
            // ----------------------------------------------------

            if ("canceled".equalsIgnoreCase(statusAntes)) {

                System.out.println();
                System.out.println(
                        "La order YA estaba cancelada."
                );

                return;
            }


            // ----------------------------------------------------
            // 3. SOLO PODEMOS CANCELAR SI ESTA EN CREATED
            // ----------------------------------------------------

            if (!"created".equalsIgnoreCase(statusAntes)) {

                System.out.println();
                System.out.println(
                        "La order no esta en status=created."
                );

                System.out.println(
                        "No se intentara cancelar."
                );

                System.out.println(
                        "Status encontrado: " + statusAntes
                );

                return;
            }


            // ----------------------------------------------------
            // 4. CANCELAR ORDER
            // ----------------------------------------------------

            System.out.println();
            System.out.println("==============================================");
            System.out.println("2. CANCELANDO ORDER");
            System.out.println("==============================================");

            JsonObject respuestaCancelacion =
                    cancelarOrder(ORDER_ID);

            if (respuestaCancelacion == null) {

                System.out.println();
                System.out.println(
                        "La cancelacion fallo."
                );

                return;
            }


            // ----------------------------------------------------
            // 5. MOSTRAR RESPUESTA
            // ----------------------------------------------------

            System.out.println();
            System.out.println("==============================================");
            System.out.println("RESPUESTA DE CANCELACION");
            System.out.println("==============================================");

            System.out.println(
                    GSON.toJson(respuestaCancelacion)
            );


            // ----------------------------------------------------
            // 6. VERIFICAR AUTOMATICAMENTE
            // ----------------------------------------------------

            System.out.println();
            System.out.println("==============================================");
            System.out.println("3. VERIFICANDO CANCELACION");
            System.out.println("==============================================");

            // Esperamos un momento para que Mercado Pago
            // actualice completamente el estado.
            Thread.sleep(1500);

            JsonObject orderDespues =
                    consultarOrder(ORDER_ID);

            if (orderDespues == null) {

                System.out.println(
                        "No fue posible verificar la order."
                );

                return;
            }

            String statusDespues =
                    obtenerStatus(orderDespues);

            System.out.println();
            System.out.println(
                    "Status despues de cancelar:"
            );

            System.out.println(statusDespues);


            // ----------------------------------------------------
            // 7. RESULTADO FINAL
            // ----------------------------------------------------

            System.out.println();
            System.out.println("==============================================");

            if ("canceled".equalsIgnoreCase(statusDespues)) {

                System.out.println(
                        "      CANCELACION EXITOSA"
                );

                System.out.println();
                System.out.println(
                        "La order fue cancelada correctamente."
                );

                System.out.println(
                        "La terminal deberia quedar libre."
                );

                System.out.println();
                System.out.println(
                        "Ya puedes probar una nueva order."
                );

            } else {

                System.out.println(
                        "      CANCELACION NO CONFIRMADA"
                );

                System.out.println();
                System.out.println(
                        "El status actual es: "
                                + statusDespues
                );
            }

            System.out.println(
                    "=============================================="
            );


        } catch (Exception e) {

            System.out.println();
            System.out.println("==============================================");
            System.out.println("ERROR");
            System.out.println("==============================================");

            e.printStackTrace();
        }
    }


    // ============================================================
    // CONSULTAR ORDER
    // ============================================================

    private static JsonObject consultarOrder(
            String orderId) throws IOException {

        String url =
                BASE_URL
                        + "/v1/orders/"
                        + orderId;

        System.out.println();
        System.out.println("GET:");
        System.out.println(url);

        Request request =
                new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader(
                                "Authorization",
                                "Bearer " + ACCESS_TOKEN
                        )
                        .addHeader(
                                "Accept",
                                "application/json"
                        )
                        .build();

        Response response =
                CLIENT.newCall(request).execute();

        String responseBody =
                response.body() != null
                        ? response.body().string()
                        : "";

        System.out.println();
        System.out.println(
                "HTTP: "
                        + response.code()
                        + " "
                        + response.message()
        );

        System.out.println();
        System.out.println("BODY:");
        System.out.println(responseBody);

        if (!response.isSuccessful()) {

            System.out.println();
            System.out.println(
                    "ERROR AL CONSULTAR ORDER"
            );

            return null;
        }

        return new JsonParser()
        .parse(responseBody)
        .getAsJsonObject();
    }


    // ============================================================
    // CANCELAR ORDER
    // ============================================================

    private static JsonObject cancelarOrder(
            String orderId) throws IOException {

        String url =
                BASE_URL
                        + "/v1/orders/"
                        + orderId
                        + "/cancel";

        // Mercado Pago requiere un X-Idempotency-Key
        // diferente para cada nueva operacion.
        String idempotencyKey =
                UUID.randomUUID().toString();

        System.out.println();
        System.out.println("POST:");
        System.out.println(url);

        System.out.println();
        System.out.println(
                "X-Idempotency-Key:"
        );

        System.out.println(idempotencyKey);

        // El endpoint de cancelacion no necesita
        // informacion en el body.
        RequestBody body =
                RequestBody.create(
                        JSON,
                        "{}"
                );

        Request request =
                new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader(
                                "Authorization",
                                "Bearer " + ACCESS_TOKEN
                        )
                        .addHeader(
                                "Content-Type",
                                "application/json"
                        )
                        .addHeader(
                                "X-Idempotency-Key",
                                idempotencyKey
                        )
                        .build();

        Response response =
                CLIENT.newCall(request).execute();

        String responseBody =
                response.body() != null
                        ? response.body().string()
                        : "";

        System.out.println();
        System.out.println(
                "HTTP: "
                        + response.code()
                        + " "
                        + response.message()
        );

        System.out.println();
        System.out.println("BODY:");
        System.out.println(responseBody);

        if (!response.isSuccessful()) {

            System.out.println();
            System.out.println(
                    "=============================================="
            );

            System.out.println(
                    "ERROR AL CANCELAR ORDER"
            );

            System.out.println(
                    "HTTP: "
                            + response.code()
            );

            System.out.println(
                    "=============================================="
            );

            return null;
        }

        return new JsonParser()
        .parse(responseBody)
        .getAsJsonObject();
    }


    // ============================================================
    // OBTENER STATUS
    // ============================================================

    private static String obtenerStatus(
            JsonObject order) {

        if (order == null) {
            return "";
        }

        if (!order.has("status")) {
            return "";
        }

        return order
                .get("status")
                .getAsString();
    }
}