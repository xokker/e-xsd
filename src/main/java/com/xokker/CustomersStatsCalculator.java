package com.xokker;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;

/**
 * @author Ernest Sadykov
 * @since 26.09.2015
 */
public class CustomersStatsCalculator {

    public static CustomersStats calculateStats(Customers customers0) {
        List<Customers.Customer> customers = customers0.getCustomer();

        BigDecimal totalOverall = customers.stream()
                .map(CustomersStatsCalculator::customerTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int biggestClient = customers.stream()
                .sorted((c1, c2) -> customerTotal(c1).compareTo(customerTotal(c2)))
                .findFirst().get()
                .getId().intValue();

        BigDecimal totalOfBiggestOrder = sortedOrders(customers, naturalOrder()).findFirst().get();

        BigDecimal totalOfSmallestOrder = sortedOrders(customers, reverseOrder()).findFirst().get();

        int numberOfOrders = customers.stream()
                .mapToInt(c -> c.getOrders().getOrder().size())
                .sum();

        // TODO: maybe simpler solution?
        Pair<Integer, BigDecimal> countAndSum = customers.stream()
                .flatMap(c -> c.getOrders().getOrder().stream())
                .map(CustomersStatsCalculator::orderTotal)
                .reduce(Pair.of(0, BigDecimal.ZERO),
                        (p, d) -> Pair.of(p.getLeft() + 1, p.getRight().add(d)),
                        (p1, p2) -> Pair.of(p1.getLeft() + p2.getLeft(), p1.getRight().add(p2.getRight())));
        BigDecimal count = BigDecimal.valueOf(countAndSum.getLeft());
        BigDecimal avgTotalOfOrders = countAndSum.getRight().divide(count, 2, RoundingMode.HALF_UP);

        return new CustomersStats(totalOverall, biggestClient, totalOfBiggestOrder,
                totalOfSmallestOrder, numberOfOrders, avgTotalOfOrders);
    }

    private static Stream<BigDecimal> sortedOrders(List<Customers.Customer> customers,
                                                   Comparator<BigDecimal> comparator) {
        return customers.stream()
                .flatMap(c -> c.getOrders().getOrder().stream())
                .map(CustomersStatsCalculator::orderTotal)
                .sorted(comparator);
    }

    private static BigDecimal orderTotal(Customers.Customer.Orders.Order order) {
        return order.getPositions().getPosition().stream()
                .map(p -> p.getPrice().multiply(new BigDecimal(p.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal customerTotal(Customers.Customer customer) {
        return customer.getOrders().getOrder().stream()
                .flatMap(o -> o.getPositions().getPosition().stream())
                .map(p -> p.getPrice().multiply(new BigDecimal(p.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}