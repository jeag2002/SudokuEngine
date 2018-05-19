package es.sudokusolver.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.AsyncClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRequestCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.ResponseExtractor;

public class testAsyncSudoku {
	
	

	public static void main(String[] args) throws Exception {

		String inputStr = "{"+
				"\"sizeX\":\"9\","+
				"\"sizeY\":\"9\","+
				"\"data\":\"100000040980004200005007908000060090009508100030010000506800700002400015010000009\","+
				"\"res\":\"\","+
				"\"errMsg\":\"\""+
			    "}";
		
		final String uriTest_3 = "http://localhost:8080/resolveAsyncSudoku";
		
		System.out.println("*****************************************************");
		System.out.println("RequestData POST ASYNC+NON-BLOCKING asyncSudoku");
		
		
		HttpHeaders headers_1 = new HttpHeaders();
		headers_1.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request_1 = new HttpEntity<>(inputStr, headers_1);
		
		/*
		final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setTaskExecutor(new SimpleAsyncTaskExecutor());
		requestFactory.setConnectTimeout(50000000);
		requestFactory.setReadTimeout(50000000);
		*/
		
		/*
		HttpComponentsAsyncClientHttpRequestFactory httpRequestFactory = new HttpComponentsAsyncClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(50000000);
        httpRequestFactory.setConnectTimeout(50000000);
        httpRequestFactory.setReadTimeout(50000000);
		*/
		
		PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT));
		connectionManager.setMaxTotal(20000);
		connectionManager.setDefaultMaxPerRoute(20000);

		RequestConfig config = RequestConfig.custom().setConnectTimeout(300000).setConnectionRequestTimeout(300000)
				.build();

		CloseableHttpAsyncClient httpClient = HttpAsyncClientBuilder.create()
				.setConnectionManager(connectionManager)
				.setDefaultRequestConfig(config).build();

		HttpComponentsAsyncClientHttpRequestFactory requestFactory = new HttpComponentsAsyncClientHttpRequestFactory(
				httpClient);
		//client = new AsyncRestTemplate(requestFactory);
		
		
        //AsyncRestTemplate asyncTemplate = new AsyncRestTemplate(httpRequestFactory); 
		//AsyncRestTemplate asyncTemplate = new AsyncRestTemplate(); 
		AsyncRestTemplate asyncTemplate = new AsyncRestTemplate(requestFactory);
		ListenableFuture<ResponseEntity<String>> data  = asyncTemplate.exchange(uriTest_3, HttpMethod.POST ,request_1, String.class);
		
		data.addCallback(
				new ListenableFutureCallback<ResponseEntity<String>>() {
	                @Override
	                public void onSuccess(ResponseEntity<String> result) {
	                    System.out.println("[RESULT ASYNC CLIENT]" + result.getBody());
	                }
	                @Override
	                public void onFailure(Throwable t) {
	                	System.out.println("[ERROR ASYNC CLIENT]" + t.getMessage());
	          
	                }
	            }		
		);
		
		
		/*
		*/
		
		//data.get(timeout, unit)
		
		
		System.out.println("*****************************************************");
		
		
		
		/*
		final String uriTest_3 = "http://localhost:8080/resolveAsyncSudoku";
		
		final String uriTest_4 = "http://localhost:8080";
		
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
		
		
		AsyncRequestCallback requestCallback = new AsyncRequestCallback (){
			@Override
			public void doWithRequest(AsyncClientHttpRequest arg0)
					throws IOException {
				System.out.println(arg0.getURI());
			}
		};
		
		ResponseExtractor<String> responseExtractor = new ResponseExtractor<String>(){
				@Override
				public String extractData(ClientHttpResponse arg0)
						throws IOException {
					return arg0.getStatusText();
				}
		};
		
		Map<String,String> urlVariable = new HashMap<String, String>();
		ListenableFuture<String> data = asyncTemplate.execute(uriTest_3, HttpMethod.POST, requestCallback, responseExtractor, urlVariable);
		
		//ListenableFuture<ResponseEntity<String>> data  = asyncTemplate.exchange(uriTest_3, HttpMethod.POST ,request_1, String.class);
		
		while (!(data.isDone())) {
		    Thread.sleep(10); //millisecond pause between each check
		}
		
		String res = data.get();
			
		
		//ResponseEntity<String> res = data.get();
		//if (res != null){
		//	if (res.getBody() != null){
		//				System.out.println("responseData POST Async response (" + res.getBody() + ")");
		//	}
		//}
		 */
		
		
		
		
		

	}

}
