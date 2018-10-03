package de.spring.example.rest.filter;

import java.util.function.Function;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import de.spring.example.context.UsernameContext;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class SubscriberContext {

	public static Mono<Void> contextSubscriber(Function<Context, Context> applyContext, ServerWebExchange exchange, WebFilterChain chain) {
		return chain
				.filter(exchange)
				.compose(function -> function
						.then(Mono.subscriberContext())
						.flatMap(context -> {
							Mono<Void> continuation = Mono.empty();
							return continuation;
						})
						.subscriberContext(context -> {
							Context updatedContext = context;
							if (!context.hasKey(UsernameContext.class)) {
								updatedContext = applyContext.apply(context);
							}

							return updatedContext;
						})
					);
	}

}
