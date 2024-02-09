package com.devesh.springbatchdemo.config;

import com.devesh.springbatchdemo.model.Customer;
import org.springframework.batch.item.ItemProcessor;

//This is ItemProcessor Implementation
public class CustomerProcessor implements ItemProcessor<Customer, Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
        if (customer.getCountry().equals("United States")){
            return customer;
        }
        else {
            return null;
        }
    }
}
