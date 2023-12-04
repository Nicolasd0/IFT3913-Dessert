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
            currencyConverter.MainWindow.convert("US Dollar", "US Dollar", currencies, -9000.);
        });
        
        //Test pour -1
        assertThrows("Conversion of negative value should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("US Dollar", "US Dollar", currencies, -1.);
        });

        //Test pour 0
        assertEquals("Conversion of 0.0 to the same currency should be 0.0",
        0.0, currencyConverter.MainWindow.convert("USD", "USD", currencies, 0.), 0);

        //Test pour 1
        assertEquals("Conversion of 1.0 to the same currency currency should be 1.0",
        1.0, currencyConverter.MainWindow.convert("USD", "USD", currencies, 1.), 0);

        //GBP to Euro
        assertEquals("Conversion of 500 000.5 GBP to Euro should be 705 000.71",
        705000.71, currencyConverter.MainWindow.convert("GBP", "EUR", currencies, 500000.5), 0);

        //Euro to GBP
        assertEquals("Conversion of 705 000.71 Euro to GBP should be 500 000.5",
        500000.5, currencyConverter.MainWindow.convert("EUR", "GBP", currencies, 705000.71), 0);

        //CHF to Euro
        assertEquals("Conversion of 500 000.5 CHF to Euro should be 465 000.47",
        465000.47, currencyConverter.MainWindow.convert("CHF", "EUR", currencies, 500000.5), 0);

        //Euro to CAD
        assertEquals("Conversion of 465 000.47 Euro to CAD should be 694 289.88",
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

    @Test
     public void testWhiteBoxConvert(){
        //Création des tableaux de currencies
        ArrayList<Currency> currencies1 = new ArrayList<Currency>();
				
		currencies1.add( new currencyConverter.Currency("US Dollar", "USD") );
		currencies1.add( new currencyConverter.Currency("Euro", "EUR") );

		for (Integer i = 0; i < currencies1.size(); i++) {
			currencies1.get(i).defaultValues();
		}


        ArrayList<Currency> currencies2 = new ArrayList<Currency>();

		currencies2.add( new currencyConverter.Currency("British Pound", "GBP") );
		currencies2.add( new currencyConverter.Currency("US Dollar", "USD") );
		currencies2.add( new currencyConverter.Currency("Euro", "EUR") );
		

		for (Integer i = 0; i < currencies2.size(); i++) {
			currencies2.get(i).defaultValues();
		}

        ArrayList<Currency> currencies3 = new ArrayList<Currency>();

		currencies3.add( new currencyConverter.Currency("Euro", "EUR") );
		

		for (Integer i = 0; i < currencies3.size(); i++) {
			currencies3.get(i).defaultValues();
		}

        ArrayList<Currency> currencies4 = new ArrayList<Currency>();

		currencies4.add( new currencyConverter.Currency("Euro", null) );
		

		for (Integer i = 0; i < currencies4.size(); i++) {
			currencies4.get(i).defaultValues();
		}

        ArrayList<Currency> currencies5 = new ArrayList<Currency>();

        currencies5.add( new currencyConverter.Currency("US Dollar", "USD") );
		currencies5.add( new currencyConverter.Currency("British Pound", "GBP") );
		currencies5.add( new currencyConverter.Currency("Euro", "EUR") );
		

		for (Integer i = 0; i < currencies5.size(); i++) {
			currencies5.get(i).defaultValues();
		}

        ArrayList<Currency> currencies6 = new ArrayList<Currency>();

        currencies6.add( new currencyConverter.Currency("US Dollar", "USD") );
		currencies6.add( new currencyConverter.Currency("British Pound", "GBP") );
		currencies6.add( new currencyConverter.Currency("Euro", "EUR") );
		currencies6.add( new currencyConverter.Currency("Swiss Franc", "CHF") );
		

		for (Integer i = 0; i < currencies6.size(); i++) {
			currencies6.get(i).defaultValues();
		}

        ArrayList<Currency> currencies7 = new ArrayList<Currency>();

        currencies7.add( new currencyConverter.Currency("US Dollar", "USD") );
		currencies7.add( new currencyConverter.Currency("British Pound", "GBP") );
		currencies7.add( new currencyConverter.Currency("Euro", "EUR") );
        currencies7.add( new currencyConverter.Currency("Swiss Franc", "CHF") );
        currencies7.add( new currencyConverter.Currency("Chinese Yuan Renminbi", "CNY") );
		

		for (Integer i = 0; i < currencies7.size(); i++) {
			currencies7.get(i).defaultValues();
		}

        //Critère de couverture des instructions ET Critère de couverture des chemins indépendants ET Critère de couverture des i-chemins (Boucle 1)
        assertEquals("Conversion of 50 US Dollar to Euro should be 46.5",
        46.5, currencyConverter.MainWindow.convert("US Dollar", "Euro", currencies1, 50.0), 0);


        //Critère de couverture du graphe de flux de contrôle ET critère de couverture des chemins indépendants ET critère de couverture des i-chemins (Boucle 1)
        assertThrows("Conversion of currencies that aren't found in the list should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("US Dollar", "Euro", new ArrayList<Currency>(), 50.0);
        });

        

        //Critère de couverture du graphe de flux de contrôle ET Critère de couverture des chemins indépendants
        assertEquals("Conversion of 50 US Dollar to Euro should be 46.5",
        46.5, currencyConverter.MainWindow.convert("US Dollar", "Euro", currencies2, 50.0), 0);


        //Critère de couverture du graphe de flux de contrôle ET Critère de couverture des i-chemins (Boucle 1)
        assertThrows("Conversion of currencies that aren't found in the list should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("US Dollar", "Euro", currencies3, 50.0);
        });
        
        //Critère de couverture des chemins indépendants
        assertThrows("Conversion of currencies that aren't found in the list should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("US Dollar", "Euro", currencies4, 50.0);
        });


        //Critère de couverture des i-chemins (Boucle 1)
        assertEquals("Conversion of 50 US Dollar to Euro should be 46.5",
        46.5, currencyConverter.MainWindow.convert("US Dollar", "Euro", currencies5, 50.0), 0);

        //Critère de couverture des i-chemins (Boucle 1)
        assertEquals("Conversion of 50 US Dollar to Euro should be 46.5",
        46.5, currencyConverter.MainWindow.convert("US Dollar", "Euro", currencies6, 50.0), 0);


        //Critère de couverture des i-chemins (Boucle 1)
        assertEquals("Conversion of 50 US Dollar to Euro should be 46.5",
        46.5, currencyConverter.MainWindow.convert("US Dollar", "Euro", currencies7, 50.0), 0);


        //Critère de couverture des i-chemins (Boucle 2)
        assertThrows("Conversion of currencies that aren't found in the list should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("Euro", "US Dollar", new ArrayList<Currency>(), 50.0);
        });

        //Critère de couverture des i-chemins (Boucle 2)
        assertThrows("Conversion of currencies that aren't found in the list should throw an error", Exception.class, () -> {
            currencyConverter.MainWindow.convert("Euro", "US Dollar", currencies3, 50.0);
        });

        //Critère de couverture des i-chemins (Boucle 2)
        assertEquals("Conversion of 50 Euro to US Dollar should be 53.65",
        53.65, currencyConverter.MainWindow.convert("Euro", "US Dollar", currencies1, 50.0), 0);

        //Critère de couverture des i-chemins (Boucle 2)
        assertEquals("Conversion of 50 Euro to US Dollar should be 53.65",
        53.65, currencyConverter.MainWindow.convert("Euro", "US Dollar", currencies5, 50.0), 0);

        //Critère de couverture des i-chemins (Boucle 2)
        assertEquals("Conversion of 50 Euro to US Dollar should be 53.65",
        53.65, currencyConverter.MainWindow.convert("Euro", "US Dollar", currencies6, 50.0), 0);

        //Critère de couverture des i-chemins (Boucle 2)
        assertEquals("Conversion of 50 Euro to US Dollar should be 53.65",
        53.65, currencyConverter.MainWindow.convert("Euro", "US Dollar", currencies7, 50.0), 0);
    }
}
