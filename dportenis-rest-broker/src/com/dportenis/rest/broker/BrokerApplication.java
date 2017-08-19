package com.dportenis.rest.broker;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.dportenis.rest.module.OrdenCompra;

public class BrokerApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(OrdenCompra.class);
		return classes;
	}
}
