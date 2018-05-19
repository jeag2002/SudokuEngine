package es.sudokuclient;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import es.sudokuclient.bean.BWrapper;

public class Main {
	
	private static String url = "http://localhost:8080/resolveAsyncSudoku";
	
	private static String inputStr = 
			"{"+
			"\"sizeX\":\"9\","+
			"\"sizeY\":\"9\","+
			"\"data\":\"100000040980004200005007908000060090009508100030010000506800700002400015010000009\","+
			"\"res\":\"\","+
			"\"errMsg\":\"\""+
		    "}";
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("*****************************************************");
		
		ClientConfig configuration = new ClientConfig();
		configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, 50000000);
		configuration = configuration.property(ClientProperties.READ_TIMEOUT, 50000000);
		
		
		//Client client = ClientBuilder.newBuilder().build();
		Client client = ClientBuilder.newClient(configuration);
		
		
		BWrapper BW = new BWrapper();
		BW.setSizeX("9");
		BW.setSizeY("9");
		BW.setData("100000040980004200005007908000060090009508100030010000506800700002400015010000009");
		BW.setRes("");
		BW.setErrMsg("");
		
		Form form = new Form();
		form.param("sizeX", BW.getSizeX());
		form.param("sizeY", BW.getSizeY());
		form.param("data", BW.getData());
		form.param("res", BW.getRes());
		form.param("errMsg", BW.getErrMsg());
		
		
		client.target(url).request(MediaType.APPLICATION_JSON).async()
		.post(Entity.entity(inputStr, MediaType.APPLICATION_JSON),
				new InvocationCallback<Response>() {
					@Override
					public void completed(Response complete) {
						String responseString = complete.readEntity(String.class);
						System.out.println("[RESULT] " + responseString );
					}
	
					@Override
					public void failed(Throwable ex) {
						ex.printStackTrace();
					}
				});
		
		
		/*
		Future<BWrapper> futureResponse = 
		     client.target(url)
		     .request(MediaType.APPLICATION_JSON)
		     .async()
		     .post(Entity.entity(inputStr, MediaType.APPLICATION_JSON),BWrapper.class);
		     
		    
		BWrapper result = futureResponse.get(5, TimeUnit.MINUTES);
		System.out.println("[RESULT] " + result.toString());
		*/
		
		System.out.println("*****************************************************");
	}
	

}

/*
.async().get(
		   new InvocationCallback<Response>() {
			   @Override
			   public void completed(Response response) {
				   String responseString = response.readEntity(String.class);
				   System.out.println("[Response code]"+ response.getStatus() );
				   System.out.println("[Response data]" + responseString);
			   }
			   @Override
			   public void failed(Throwable throwable) {
				   System.out.println("Failed");
				   throwable.printStackTrace();
			   }
		});
*/
