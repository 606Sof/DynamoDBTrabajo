package es.aad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import java.util.HashMap;
import java.util.Map;

public class Programa {
	private static Logger LOG = LoggerFactory.getLogger(Programa.class);
	
	public static void main(String[] args) {
		insertItem("12");
		//insertItem("36");
		insertItem("2");
		retrieveItem("85");
		deleteItem("2");
	}
	private static boolean retrieveItem(String id) {
		Map<String, AttributeValue> item = Map.of("id_Cliente", AttributeValue.builder().s(id).build()); 	
		DynamoDbClient cliente = Manager.client();
        	
		try {
        	GetItemRequest request = GetItemRequest.builder()
        			.tableName("Prueba")
        			.key(item)
        			.build();
        	GetItemResponse response = cliente.getItem(request);
        	if(response != null) {
        		LOG.info("Encontrado el objeto con id [" + id + "]");
        		return true;
        	} else {
        		LOG.warn("No se ha encontrado el objeto con id [" + id + "]");
        	}
        } catch (Exception e) {
            LOG.error("Error en la busqueda del objeto con ID [" + id + "]");
        }
        return false;
    }
	
	private static void deleteItem(String id) {
		Map<String, AttributeValue> item = Map.of("id_Cliente", AttributeValue.builder().s(id).build()); 	
		DynamoDbClient cliente = Manager.client();
        try {
        	DeleteItemRequest request = DeleteItemRequest.builder()
        			.tableName("Prueba")
        			.key(item)
        			.build();
        	cliente.deleteItem(request);
        	
        	if (retrieveItem(id)) {
        		LOG.info("Se ha borrado el ítem: [" + id + "]");
            } else {
            	LOG.warn("El ítem con ID {" + id +"} no existía en la tabla.");
            }
        } catch (Exception e) {
        	LOG.error("Error en el borrado del objeto con ID [" + id + "]");
        } finally {
        	cliente.close();
        }
    }
	public static void insertItem(String id) {
		DynamoDbClient cliente = Manager.client();
	    Map<String, AttributeValue> item = new HashMap<>();
        item.put("id_Cliente", AttributeValue.builder().s(id).build());
        item.put("Nombre", AttributeValue.builder().s("Tapi").build());
        item.put("Num", AttributeValue.builder().n("456789").build());
        
        PutItemRequest request = PutItemRequest.builder()
                .tableName("Prueba") 
                .item(item)
                .build();
        
		cliente.putItem(request);
		LOG.info("Se ha insetado el objeto");
	}
	public static void updateItem(String id, String nuevoNombre) {
		DynamoDbClient cliente = Manager.client();
	    Map<String, AttributeValue> item = Map.of("id", AttributeValue.builder().s(id).build());

	    // Primero, obtenemos el valor actual de "Nombre" para saber si ya es el que queremos cambiar
	    GetItemRequest getRequest = GetItemRequest.builder()
	            .tableName("Prueba")
	            .key(item)
	            .build();
	    
	    try {
	        // Obtener el item antes de la actualización
	        GetItemResponse getResponse = cliente.getItem(getRequest);
	        String nombreActual = null;
	        
	        if (getResponse.hasItem() && getResponse.item().containsKey("Nombre")) {
	            nombreActual = getResponse.item().get("Nombre").s();
	        }

	        // Si el valor ya es el que queremos cambiar, no necesitamos hacer la actualización
	        if (nombreActual != null && !nombreActual.equals(nuevoNombre)) {
	            // Realizar la actualización solo si el nombre no es igual al que queremos actualizar
	            String nombreNuevo = "SET Nombre = :Nombre";
	            Map<String, AttributeValue> cambio = Map.of(":Nombre", AttributeValue.builder().s(nuevoNombre).build());

	            UpdateItemRequest updateRequest = UpdateItemRequest.builder()
	                    .tableName("Prueba")
	                    .key(item)
	                    .updateExpression(nombreNuevo)
	                    .expressionAttributeValues(cambio)
	                    .build();

	            // Ejecutar la actualización
	            UpdateItemResponse updateResponse = cliente.updateItem(updateRequest);

	            // Verificamos si la actualización fue exitosa comparando el valor anterior con el nuevo
	            if (nombreActual != null && !nombreActual.equals(nuevoNombre)) {
	                LOG.info("Se ha modificado correctamente el atributo");
	            } else {
	                LOG.info("El atributo ya estaba en el valor esperado ("+nuevoNombre+"). No se ha realizado ningún cambio.");
	            }
	        } else {
	            LOG.info("El atributo ya estaba en el valor esperado ("+nuevoNombre+"). No se ha realizado ningún cambio.");
	        }
	    } catch (Exception e) {
	        LOG.error("Error durante la operación: " + e.getMessage());
	    }
	}
}
