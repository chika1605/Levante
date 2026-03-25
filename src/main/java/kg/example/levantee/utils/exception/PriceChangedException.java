package kg.example.levantee.utils.exception;

import lombok.Getter;

@Getter
public class PriceChangedException extends RuntimeException {

    private final double oldPrice;
    private final double newPrice;

    public PriceChangedException(double oldPrice, double newPrice) {
        super("Цена доставки изменилась");
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }
}