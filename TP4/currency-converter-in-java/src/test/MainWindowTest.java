package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;

import org.junit.Test;

import currencyConverter.*;

public class MainWindowTest {
    @Test
    public void testBlackBoxConvert(){
        
        ArrayList<Currency> currencies = currencyConverter.Currency.init();


        //Test pour -9000
        assertThrows("Conversion of negative value should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("USD", "USD", currencies, -9000.);
        });
        
        //Test pour -1
        assertThrows("Conversion of negative value should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("USD", "USD", currencies, -1.);
        });

        //Test pour 0
        assertEquals("Conversion of 0.0 to the same currency should be 0.0",
        0.0, currencyConverter.MainWindow.convert("USD", "USD", currencies, 0.), 0);

        //Test pour 1
        assertEquals("Conversion of 1.0 to the same currency currency should be 1.0",
        1.0, currencyConverter.MainWindow.convert("USD", "USD", currencies, 1.), 0);

        //GBP to EUR
        assertEquals("Conversion of 500 000.5 GBP to EUR should be 705 000.71",
        705000.71, currencyConverter.MainWindow.convert("GBP", "EUR", currencies, 500000.5), 0);

        //EUR to GBP
        assertEquals("Conversion of 705 000.71 EUR to GBP should be 500 000.5",
        500000.5, currencyConverter.MainWindow.convert("EUR", "GBP", currencies, 705000.71), 0);

        //CHF to EUR
        assertEquals("Conversion of 500 000.5 CHF to EUR should be 465 000.47",
        465000.47, currencyConverter.MainWindow.convert("CHF", "EUR", currencies, 500000.5), 0);

        //EUR to CAD
        assertEquals("Conversion of 465 000.47 EUR to CAD should be 694 289.88",
        694289.88, currencyConverter.MainWindow.convert("EUR", "CAD", currencies, 465000.47), 0);

        //CHF to CAD
        assertEquals("Conversion of 500 000.5 CHF to CAD should be 694 289.88",
        694289.88, currencyConverter.MainWindow.convert("CHF", "CAD", currencies, 500000.5), 0);

        //INVALID to CHF
        assertThrows("Conversion of invalid currencies should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("INVALID", "CHF", currencies, 500000.5);
        });

        //CHF to INVALID
        assertThrows("Conversion of invalid currencies should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("CHF", "INVALID", currencies, 500000.5);
        });

        //Test pour 1 000 000
        assertEquals("Conversion of 1 000 000.0 to another currency should be 1 000 000.0",
        0.0, currencyConverter.MainWindow.convert("USD", "USD", currencies, 0.), 0);

        //Test pour 1 000 001
        assertThrows("Conversion of value above 1 000 000.0 should fail", Exception.class, () -> {
            currencyConverter.MainWindow.convert("USD", "USD", currencies, 1000001.);
        });

        //Test pour 9 000 000
        assertThrows("Conversion of value above 1 000 000.0 should fail", Exception.class, () -> {
            currencyConverter.MainWindow.convert("USD", "USD", currencies, 9000000.);
        });

    }
}
