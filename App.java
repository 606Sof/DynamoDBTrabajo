package prueba.aws.PruebaAws2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    public static void main(String... args) {
        logger.info("Application starts");

//        Handler handler = new Handler();
//        handler.sendRequest();
        
        DynamoDbClient dynamoDbClient = DependencyFactory.dynamoDbClient();
        
        for(String s : dynamoDbClient.listTables().tableNames()) {
        	System.out.println(s);
        }
        
        getItemFromDynamoDB(dynamoDbClient, "prueba", "id", "id#1");
        
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
        
        DynamoDbTable<Cliente> tablaCliente = enhancedClient.table("cliente", TableSchema.fromBean(Cliente.class));
        
        ArrayList<String> amigos = new ArrayList<String>();
        amigos.add("yo");
        amigos.add("mi madre");
        
        Direccion direc = new Direccion();
        direc.setCalle("Calle sin fin");
        direc.setNum(12);
        
        Cliente c = new Cliente();
        c.setId(4);
        c.setNombre("jorge");
        c.setEmail("jorge@email.com");
        c.setDirecc(direc);
        c.setAmigos(amigos);
        
        tablaCliente.putItem(c);
        
        Cliente clienteBuscado = tablaCliente.getItem(Key.builder().partitionValue(AttributeValue.fromN("1")).build());
        
        System.out.println(clienteBuscado.getNombre());

        logger.info("Application ends");
    }
    
    private static void getItemFromDynamoDB(DynamoDbClient dynamoDbClient, String tableName, String key, String keyValue) {
        try {
        	Map<String, AttributeValue> keyMap = new HashMap<String, AttributeValue>();
        	keyMap.put(key, AttributeValue.builder().s(keyValue).build());
            // Crear la clave primaria para la consulta
            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(keyMap) // Usamos la clave primaria (UserID)
                    .build();
            // Ejecutar la consulta
            GetItemResponse response = dynamoDbClient.getItem(getItemRequest);

            // Mostrar el resultado
            if (response.hasItem()) {
                System.out.println("Item encontrado: " + response.item());
            } else {
                System.out.println("No se encontró el ítem con el valor de " + key + " = " + keyValue);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener el ítem: " + e.getMessage());
        }
    }
}
