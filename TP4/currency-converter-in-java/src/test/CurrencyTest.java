package test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import currencyConverter.*;

public class CurrencyTest {
    @Test
    public void testBlackBoxConvert(){
        //Test pour -9000
        assertThrows("Conversion of negative value should throw an error", Exception.class, () -> {
            currencyConverter.Currency.convert(-9000., 5.);
        });
        
        //Test pour -1
        assertThrows("Conversion of negative value should throw an error", Exception.class, () -> {
            currencyConverter.Currency.convert(-1., 5.);
        });

        //Test pour 0
        assertEquals("Conversion of 0.0 to another currency should be 0.0",
        0.0, currencyConverter.Currency.convert(0., 5.), 0);

        //Test pour 1
        assertEquals("Conversion of 1.0 to another currency should be 5.0",
        5.0, currencyConverter.Currency.convert(1., 5.), 0);

        //Test pour 500 000
        assertEquals("Conversion of 500 000.5 to another currency should be 250 000.25",
        250000.25, currencyConverter.Currency.convert(500000.5, .5), 0);

        //Test pour 1 000 000
        assertEquals("Conversion of 1 000 000.0 to another currency should be 500 000.0",
        500000, currencyConverter.Currency.convert(1000000., .5), 0);

        //Test pour 1 000 001
        assertThrows("Conversion of value above 1 000 000.0 should fail", Exception.class, () -> {
            currencyConverter.Currency.convert(1000001., 5.);
        });

        //Test pour 9 000 000
        assertThrows("Conversion of value above 1 000 000.0 should fail", Exception.class, () -> {
            currencyConverter.Currency.convert(9000000., 5.);
        });
    }
}
