package com.guo.pizza.service;

import com.springinaction.pizza.domain.Order;

public interface PricingEngine {
  public float calculateOrderTotal(Order order);
}
