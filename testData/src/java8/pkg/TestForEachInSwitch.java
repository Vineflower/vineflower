package pkg;

import java.util.ArrayList;
import java.util.HashSet;

public class TestForEachInSwitch {
  /**
   * Reported by nitram84 (issue #543)
   */
  private static final int GUEST_CHECKOUT = 1;
  private static final int REGISTERED_USER = 2;
  private static final int SUBSCRIPTION_RENEWAL = 3;
  private static final int EXPRESS_DELIVERY = 4;
  private static final int INTERNATIONAL_SHIPPING = 5;

  public void processTransactionDetails(final int status, final ArrayList<OrderData> batch) {
    for (final OrderData transaction : batch) {
      switch (status) {
        case GUEST_CHECKOUT:
        case REGISTERED_USER: {
          final HashSet<ProductItem> items = new HashSet<>();
          for (final ProductItem item : items) {
            updateInventoryLog(item);
          }
          break;
        }
        case SUBSCRIPTION_RENEWAL: {
          for (final ProductItem item : transaction.getPurchasedItems()) {
            updateInventoryLog(item);
          }
        }
        case EXPRESS_DELIVERY:


        case INTERNATIONAL_SHIPPING: {
          for (final ProductItem item : transaction.getPurchasedItems()) {
            updateInventoryLog(item);
          }
        }
      }
    }
  }

  private void updateInventoryLog(final ProductItem item) {}

  static class ProductItem {
  }

  static class OrderData {
    public ProductItem[] getPurchasedItems() {
      return null;
    }
  }
}
