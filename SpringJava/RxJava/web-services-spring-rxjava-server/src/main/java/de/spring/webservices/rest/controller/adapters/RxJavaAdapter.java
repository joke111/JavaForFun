package de.spring.webservices.rest.controller.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * 
 * Instead of using this class, you could create you own implementation of
 * org.springframework.web.servlet.mvc.method.annotation.DeferredResultAdapter
 * for Observable and Single.
 *
 */
public class RxJavaAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(RxJavaAdapter.class);
	
	// With no value, we depend on the Tomcat/Jboss/Jetty/etc timeout value for asynchronous requests.
	// Spring will answer after 60 secs with an empty response (by default) and HTTP 503 status (by default) when timeout.

	// You'd rather better rely on the server values. IMHO this is something to be controlled
	// by the server administrator no the developer.
	// private static final long ASYNC_TIMEOUT = 60000;  /* milliseconds */

	private RxJavaAdapter() {
		
	}
	
	public static final <T> DeferredResult<T> deferredAdapter(Observable<T> observable) {

    	DeferredResult<T> deferredResult = new DeferredResult<>(/** ASYNC_TIMEOUT **/);
    	
    	observable
    		.subscribeOn(Schedulers.io())
    		.subscribe(deferredResult::setResult, exception -> {
    			Throwable realException = launderException(exception);

    			LOGGER.error("error: ", realException);

    			deferredResult.setErrorResult(realException);
			});

        return deferredResult;	
	}
	
	public static final <T> DeferredResult<T> deferredAdapter(Single<T> single) {

    	DeferredResult<T> deferredResult = new DeferredResult<>(/** ASYNC_TIMEOUT **/);

    	single
    		.subscribeOn(Schedulers.io())
    		.subscribe(deferredResult::setResult, exception -> {
				Throwable realException = launderException(exception);
	
				LOGGER.error("error: ", realException);

			deferredResult.setErrorResult(realException);
		});

        return deferredResult;
	}
	
	private static final Throwable launderException(Throwable exception) {
		return exception.getCause() != null
			   ? exception.getCause()
			   : exception;
	}
}