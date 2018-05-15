package es.sudokusolver.client;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

public class testAsyncSudoku {
	
	

	public static void main(String[] args) throws Exception {

		String inputStr = "{"+
				"\"sizeX\":\"9\","+
				"\"sizeY\":\"9\","+
				"\"data\":\"093080240500047091640910500714000380000438702308701900436000075050174003001603029\","+
				"\"res\":\"\","+
				"\"errMsg\":\"\""+
			    "}";

		
		final String uriTest_3 = "http://localhost:8080/resolveAsyncSudoku";
				
		System.out.println("*****************************************************");
		System.out.println("RequestData POST ASYNC+NON-BLOCKING asyncSudoku");
		
		
		final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setTaskExecutor(new SimpleAsyncTaskExecutor());
		requestFactory.setConnectTimeout(50000000);
		requestFactory.setReadTimeout(50000000);
		
		AsyncRestTemplate asyncTemplate = new AsyncRestTemplate(); 
		asyncTemplate.setAsyncRequestFactory(requestFactory);
		
		HttpHeaders headers_1 = new HttpHeaders();
		headers_1.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request_1 = new HttpEntity<>(inputStr, headers_1);
				
		ListenableFuture<ResponseEntity<String>> data  = asyncTemplate.exchange(uriTest_3, HttpMethod.POST ,request_1, String.class);
		
		while (!(data.isDone())) {
		    Thread.sleep(10); //millisecond pause between each check
		}
		
			
		ResponseEntity<String> res = data.get();
		if (res != null){
			if (res.getBody() != null){
						System.out.println("responseData POST Async response (" + res.getBody() + ")");
			}
		}
		System.out.println("*****************************************************");
		
		
		

	}

}
