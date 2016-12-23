package de.spring.webservices.rest.business.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import de.spring.webservices.domain.Car;
import io.reactivex.Observable;


public interface RxJavaBusinessLogic {

	public Observable<Page<Car>> findAll(Pageable pageRequest);
	
	public Observable<Car> findById(long id);
	
	public Observable<Car> create(Car car);
	
	public Observable<Car> createThrowable(Car car);
	
}