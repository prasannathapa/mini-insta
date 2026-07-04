package com.miniinsta;

import com.miniinsta.app.AppContext;
import com.miniinsta.app.InstagramService;
import com.miniinsta.ui.ConsoleGateway;
import com.miniinsta.ui.SeedData;

import java.util.Scanner;

/**
 * Mini Instagram - console application.
 *
 * <p>STEP 12 - THE COMPLETE APP. Everything comes together: the composition root
 * wires the app, seed data gives it content, and the console gateway drives it
 * through the one {@link InstagramService} facade.</p>
 *
 * <p>Run it with {@code mvnw exec:java} (in-memory) or
 * {@code mvnw exec:java -Dmini.store=sqlite} (persisted to SQLite).</p>
 */
public class Main {

    public static void main(String[] args) {
        InstagramService app = AppContext.get().instagram();
        SeedData.seed(app);
        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleGateway(app, scanner).run();
        }
    }
}
