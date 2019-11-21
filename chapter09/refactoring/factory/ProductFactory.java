package io.wisoft.seminar.daewon.javainaction.chapter09.refactoring.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ProductFactory {


  public static Product createProduct(String name) {
    switch (name) {
      case "loan":
        return new Loan();
      case "stock":
        return new Stock();
      case "bond":
        return new Bond();
      default:
        throw new RuntimeException("No such product " + name);
    }
  }

  //
  public static Product createProductUseLambda(String name) {
    Supplier<Product> p = map.get(name);
    if (p != null) return p.get();
    throw new IllegalArgumentException("No such product " + name);
  }

  private final static Map<String, Supplier<Product>> map = new HashMap<>();

  static {
    map.put("loan", Loan::new);
    map.put("stock", Stock::new);
    map.put("bond", Bond::new);
  }


  public static void main(String[] args) {
    Product p = ProductFactory.createProduct("loan");
    System.out.println(p);

    Product pl = ProductFactory.createProductUseLambda("stock");
    System.out.println(pl);
  }

}
